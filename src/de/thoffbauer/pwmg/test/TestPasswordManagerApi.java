package de.thoffbauer.pwmg.test;

import java.io.File;

import de.thoffbauer.pwmg.manager.PasswordManager;

public class TestPasswordManagerApi {
	
	public static void main(String[] args) {
		char[] pw = new char[]{'m', 'y', 'p', 'w'};
		char[] pw2 = new char[]{'m', 'y', 'p', 'w'};
		File file = new File("test.pws");

		PasswordManager manager = PasswordManager.getInstance();
		
		manager.create(pw);
		manager.getEntries().add(new char[]{'e', 'n', 't', 'r', 'y', ' ', '1'});
		manager.getEntries().add(new char[]{'e', 'n', 't', 'r', 'y', ' ', '2'});
		manager.save(file);
		
		manager.load(file, pw2);
		for(char[] entry : manager.getEntries()) {
			System.out.println(String.valueOf(entry));
		}
		manager.clear();
	}

}
