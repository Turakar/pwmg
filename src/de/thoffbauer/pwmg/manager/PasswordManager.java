package de.thoffbauer.pwmg.manager;

import java.io.File;
import java.util.Collections;
import java.util.List;

import de.thoffbauer.pwmg.util.CharHelper;

public class PasswordManager {
	
	private static PasswordManager instance;
	
	public static PasswordManager getInstance() {
		if(instance == null) {
			instance = new PasswordManager();
		}
		return instance;
	}
	
	private Safe safe;
	private ExceptionListener exceptionListener = null;
	private boolean modified = false;
	
	private PasswordManager() {
		safe = new Safe();
	}
	
	public void create(char[] pw) {
		try {
			safe.initialize(pw);
		} catch(Exception e) {
			throwException("Create", e);
		}
	}
	
	public void load(File file, char[] pw) {
		try {
			safe.readFile(file, pw);
		} catch(Exception e) {
			throwException("Load", e);
		}
	}
	
	public void save(File file) {
		try {
			safe.saveFile(file);
		} catch(Exception e) {
			throwException("Save", e);
		}
	}
	
	public void clear() {
		safe.clearMemory();
	}
	
	public void setExceptionListener(ExceptionListener listener) {
		this.exceptionListener = listener;
	}
	
	public void throwException(String source, Exception e) {
		if(exceptionListener == null) {
			System.err.println("WARNING: No exception listener registered!");
			System.err.println("Thrown Exception: ");
			e.printStackTrace();
		} else {
			exceptionListener.onException(source, e);
		}
	}
	
	public void newEntry(char[] entry) {
		safe.getEntries().add(entry);
		modified = true;
	}
	
	public void deleteEntry(int index) {
		CharHelper.clear(safe.getEntries().remove(index));
		modified = true;
	}
	
	public void editEntry(int index, char[] entry) {
		CharHelper.clear(safe.getEntries().set(index, entry));
		modified = true;
	}

	public List<char[]> getEntries() {
		return Collections.unmodifiableList(safe.getEntries());
	}
	
	public boolean isModified() {
		return modified;
	}

}
