package de.thoffbauer.pwmg.test;

import java.io.File;

import de.thoffbauer.pwmg.manager.Safe;

public class TestSafeApi {

	public static void main(String[] args) {
		char[] pw = new char[]{'m', 'y', 'p', 'w'};
		File file = new File("test");
		
		Safe crypt = new Safe();
		
		crypt.initialize(pw);
		crypt.getEntries().add(new char[]{'e', '1', '\n', 'e', 'n', 't', 'r', 'y', ' ', '1'});
		crypt.getEntries().add(new char[]{'e', '2', '\n', 'e', 'n', 't', 'r', 'y', ' ', '2'});
		crypt.saveFile(file);
		System.out.println("saved");

		char[] pw2 = new char[]{'m', 'y', 'p', 'w'};
//		char[] pw2 = new char[]{'s', 't', 'h'};
		crypt.readFile(file, pw2);
		int i = 0;
		System.out.println("Entries:");
		for(char[] entry : crypt.getEntries()) {
			i++;
			System.out.println("Entry #" + i + ":");
			for(char c : entry) {
				System.out.print(c);
			}
			System.out.println();
		}
		System.out.println("read");
		crypt.clearMemory();
		System.out.println("exit");
	}

}
