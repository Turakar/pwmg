package de.thoffbauer.pwmg.util;

import java.security.SecureRandom;

public class CharHelper {

	public static byte[] charToBytes(char[] buffer) {
		byte[] b = new byte[buffer.length << 1];
		for (int i = 0; i < buffer.length; i++) {
			int bpos = i << 1;
			b[bpos] = (byte) ((buffer[i] & 0xFF00) >> 8);
			b[bpos + 1] = (byte) (buffer[i] & 0x00FF);
		}
		return b;
	}

	public static char[] bytesToChar(byte[] bytes) {
		char[] buffer = new char[bytes.length >> 1];
		for (int i = 0; i < buffer.length; i++) {
			int bpos = i << 1;
			char c = (char) (((bytes[bpos] & 0x00FF) << 8) + (bytes[bpos + 1] & 0x00FF));
			buffer[i] = c;
		}
		return buffer;
	}
	
	public static void clear(char[] c) {
		if(c == null) {
			return;
		}
		SecureRandom random = new SecureRandom();
		for(int i = 0; i < c.length; i++) {
			c[i] = (char) random.nextInt(Character.MAX_VALUE);
		}
	}
	
	public static void clear(byte[] b) {
		if(b == null) {
			return;
		}
		SecureRandom random = new SecureRandom();
		random.nextBytes(b);
	}
	
	public static String getFirstLine(char[] c) {
		StringBuilder sb = new StringBuilder();
		for(int i = 0; i < c.length; i++) {
			if(c[i] != '\n') {
				sb.append(c[i]);
			} else {
				return sb.toString();
			}
		}
		return sb.toString();
	}
	
	public static boolean contains(char[] text, char[] pattern) {
		if(pattern.length > text.length) {
			throw new IllegalArgumentException("Pattern must have a length lower or equal to the length of the text!");
		}
		if(pattern.length == 0) {
			return true;
		}
		for(int i = 0; i < text.length - pattern.length + 1; i++) {
			boolean different = false;
			for(int j = 0; j < pattern.length; j++) {
				char t = text[i + j];
				char p = pattern[j];
				if(t != p) {
					different = true;
					break;
				}
			}
			if(!different) {
				return true;
			}
		}
		return false;
	}

}
