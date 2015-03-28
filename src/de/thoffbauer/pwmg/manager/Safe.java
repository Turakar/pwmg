package de.thoffbauer.pwmg.manager;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.security.AlgorithmParameters;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Permission;
import java.security.PermissionCollection;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.InvalidParameterSpecException;
import java.security.spec.KeySpec;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import de.thoffbauer.pwmg.util.CharHelper;

public class Safe {
	
	private static final int MAX_HASH_LENGTH = 32;
	
	static {
		removeCryptographyRestrictions();
	}
	private static void removeCryptographyRestrictions() {
	    if (!isRestrictedCryptography()) {
	        return;
	    }
	    try {
	        /*
	         * Do the following, but with reflection to bypass access checks:
	         *
	         * JceSecurity.isRestricted = false;
	         * JceSecurity.defaultPolicy.perms.clear();
	         * JceSecurity.defaultPolicy.add(CryptoAllPermission.INSTANCE);
	         */
	        final Class<?> jceSecurity = Class.forName("javax.crypto.JceSecurity");
	        final Class<?> cryptoPermissions = Class.forName("javax.crypto.CryptoPermissions");
	        final Class<?> cryptoAllPermission = Class.forName("javax.crypto.CryptoAllPermission");

	        final Field isRestrictedField = jceSecurity.getDeclaredField("isRestricted");
	        isRestrictedField.setAccessible(true);
	        isRestrictedField.set(null, false);

	        final Field defaultPolicyField = jceSecurity.getDeclaredField("defaultPolicy");
	        defaultPolicyField.setAccessible(true);
	        final PermissionCollection defaultPolicy = (PermissionCollection) defaultPolicyField.get(null);

	        final Field perms = cryptoPermissions.getDeclaredField("perms");
	        perms.setAccessible(true);
	        ((Map<?, ?>) perms.get(defaultPolicy)).clear();

	        final Field instance = cryptoAllPermission.getDeclaredField("INSTANCE");
	        instance.setAccessible(true);
	        defaultPolicy.add((Permission) instance.get(null));
	    } catch (final Exception e) {
	    	e.printStackTrace();
	    }
	}
	private static boolean isRestrictedCryptography() {
	    // This simply matches the Oracle JRE, but not OpenJDK.
	    return "Java(TM) SE Runtime Environment".equals(System.getProperty("java.runtime.name"));
	}

	private byte[] salt;
	private byte[] iv;
	private byte[] hash;
	
	private SecretKey key;
	private Cipher cipher;
	
	private DataInputStream rawInStream;
	private DataInputStream cipherInStream;
	
	private DataOutputStream rawOutStream;
	private DataOutputStream cipherOutStream;
	
	private ArrayList<char[]> entries;
	private File file;
	
	public Safe() {
		entries = new ArrayList<char[]>();
	}
	
	public void initialize(char[] pw) {
		try {
			generateSalt();
			generateHash(pw);
			deriveKey(pw);
			CharHelper.clear(pw);
			entries.clear();
		} catch(Exception e) {
			throw new RuntimeException("An exception occured during initialisation!", e);
		}
	}
	private void generateSalt() {
		salt = new byte[8];
		CharHelper.clear(salt);
	}
	private void generateHash(char[] pw) throws NoSuchAlgorithmException {
		MessageDigest md = MessageDigest.getInstance("SHA-256");
        md.update(salt);
        hash = md.digest(CharHelper.charToBytes(pw));
	}
	private void deriveKey(char[] pw) throws NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException {
		SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
		KeySpec spec = new PBEKeySpec(pw, salt, 65536, 256);
		SecretKey tmp = factory.generateSecret(spec);
		key = new SecretKeySpec(tmp.getEncoded(), "AES");
		cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
	}
	
	public void readFile(File file, char[] pw) {
		this.file = file;
		try {
			openInStream();
			readCipherData();
			deriveKey(pw);
			initCipherDecrypt(pw);
			generateHash(pw);
			checkHash();
			CharHelper.clear(pw);
			readPasswords();
			closeInStream();
		} catch (IOException | NoSuchAlgorithmException | InvalidKeySpecException | NoSuchPaddingException | InvalidKeyException | InvalidParameterSpecException | InvalidAlgorithmParameterException e) {
			throw new RuntimeException("An exception occured while reading from file!", e);
		}
		
	}
	private void openInStream() throws FileNotFoundException {
		rawInStream = new DataInputStream(new FileInputStream(file));
	}
	private void readCipherData() throws IOException {
		salt = new byte[8];
		iv = new byte[16];
		if(rawInStream.read(salt) != salt.length || rawInStream.read(iv) != iv.length) {
			throw new RuntimeException("The salt and/or iv arrays have an illegal size!");
		}
	}
	private void checkHash() throws NoSuchAlgorithmException, IOException {
		int hashLength = cipherInStream.readInt();
		
		if(hashLength <= 0 || hashLength > MAX_HASH_LENGTH) {
			throw new RuntimeException("Invalid password!");
		}
		
		byte[] readHash = new byte[hashLength];
		cipherInStream.read(readHash);
		
        if(!Arrays.equals(readHash, hash)) {
        	throw new RuntimeException("Invalid password!");
        }
	}
	private void initCipherDecrypt(char[] pw) throws NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException, InvalidKeyException, InvalidParameterSpecException, InvalidAlgorithmParameterException {
		cipher.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(iv));
		cipherInStream = new DataInputStream(new CipherInputStream(rawInStream, cipher));
	}
	private void readPasswords() throws IOException {
		entries = new ArrayList<char[]>();
		int entriesCount = cipherInStream.readInt();
		for(int i = 0; i < entriesCount; i++) {
			int entryLength = cipherInStream.readInt();
			char[] entry = new char[entryLength];
			for(int j = 0; j < entryLength; j++) {
				entry[j] = cipherInStream.readChar();
			}
			entries.add(entry);
		}
	}
	private void closeInStream() throws IOException {
		cipherInStream.close();
	}
	
	public void saveFile(File file) {
		this.file = file;
		try {
			openOutStream();
			initCipherEncrypt();
			writeCipherData();
			writeHash();
			writePasswords();
			closeOutStream();
		} catch(Exception e) {
			throw new RuntimeException("An exception occured while saving to file!", e);
		}
	}
	private void openOutStream() throws FileNotFoundException {
		if(file.exists())
			file.delete();
		rawOutStream = new DataOutputStream(new FileOutputStream(file));
	}
	private void initCipherEncrypt() throws InvalidKeyException, InvalidParameterSpecException {
		cipher.init(Cipher.ENCRYPT_MODE, key);
		AlgorithmParameters params = cipher.getParameters();
		iv = params.getParameterSpec(IvParameterSpec.class).getIV();
		cipherOutStream = new DataOutputStream(new CipherOutputStream(rawOutStream, cipher));
	}
	private void writeCipherData() throws IOException {
		rawOutStream.write(salt);
		rawOutStream.write(iv);
	}
	private void writeHash() throws IOException {
		cipherOutStream.writeInt(hash.length);
		cipherOutStream.write(hash);
	}
	private void writePasswords() throws IOException {
		cipherOutStream.writeInt(entries.size());
		for(char[] entry : entries) {
			cipherOutStream.writeInt(entry.length);
			for(int i = 0; i < entry.length; i++) {
				cipherOutStream.writeChar(entry[i]);
			}
		}
	}
	private void closeOutStream() throws IOException {
		cipherOutStream.flush();
		cipherOutStream.close();
	}
	
	public void clearMemory() {
		for(char[] entry : entries) {
			CharHelper.clear(entry);
		}
		CharHelper.clear(hash);
	}
	
	public ArrayList<char[]> getEntries() {
		return entries;
	}

}
