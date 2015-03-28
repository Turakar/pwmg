package de.thoffbauer.pwmg.gui;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;

import javax.swing.AbstractAction;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileFilter;

import de.thoffbauer.pwmg.manager.ExceptionListener;
import de.thoffbauer.pwmg.manager.PasswordManager;
import de.thoffbauer.pwmg.util.AdvancedJOptionPane;
import de.thoffbauer.pwmg.util.HintTextFieldUI;
import de.thoffbauer.pwmg.util.ListAction;

@SuppressWarnings("serial")
public class MainFrame extends JFrame implements ExceptionListener, DocumentListener {
	
	private final MainFrame instance;
	
	private JPanel contentPane;
	private JTextField searchField;
	private EntryListModel listModel;
	
	private JFileChooser chooser;
	
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				MainFrame frame = new MainFrame();
				frame.setVisible(true);
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public MainFrame() {
		instance = this;
		PasswordManager.getInstance().setExceptionListener(this);
		
		setTitle("Password Manager");
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		setBounds(100, 100, 450, 300);

		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				if(PasswordManager.getInstance().isModified()) {
					int option = JOptionPane.showConfirmDialog(instance, "There are unsaved changes! Do you want to save them?", "Unsaved changes", JOptionPane.YES_NO_CANCEL_OPTION);
					if(option == JOptionPane.YES_OPTION) {
						save();
					} else if(option == JOptionPane.NO_OPTION) {
						
					} else {
						return;
					}
				}
				PasswordManager.getInstance().clear();
				instance.dispose();
			}
		});
		
		JMenuBar menuBar = new JMenuBar();
		setJMenuBar(menuBar);
		
		JMenu mnFile = new JMenu("File");
		menuBar.add(mnFile);
		
		JMenuItem mntmNew = new JMenuItem("New");
		mntmNew.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				newFile();
			}
		});
		mnFile.add(mntmNew);
		
		JMenuItem mntmOpen = new JMenuItem("Open");
		mntmOpen.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				open();
			}
		});
		mnFile.add(mntmOpen);
		
		JMenuItem mntmSave = new JMenuItem("Save");
		mntmSave.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				save();
			}
		});
		mnFile.add(mntmSave);
		
		JMenu mnEntry = new JMenu("Entry");
		menuBar.add(mnEntry);
		
		JMenuItem mntmNewEntry = new JMenuItem("New");
		mntmNewEntry.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				newEntry();
			}
		});
		mnEntry.add(mntmNewEntry);
		
		JMenuItem mntmDelete = new JMenuItem("Delete");
		mntmDelete.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				deleteEntry();
			}
		});
		mnEntry.add(mntmDelete);
		
		JMenuItem mntmEdit = new JMenuItem("Edit");
		mntmEdit.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				editEntry();
			}
		});
		mnEntry.add(mntmEdit);
		
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(contentPane);
		
		JList<String> list = new JList<String>();
		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		list.setLayoutOrientation(JList.VERTICAL);
		list.setVisibleRowCount(-1);
		new ListAction(list, new AbstractAction("ListDefaultAction") {
			@Override
			public void actionPerformed(ActionEvent e) {
				editEntry();
			}
		});
		listModel = new EntryListModel();
		list.setModel(listModel);
		listModel.setList(list);
		contentPane.add(list, BorderLayout.CENTER);
		
		searchField = new JTextField();
		searchField.getDocument().addDocumentListener(this);
		searchField.setColumns(10);
		Font fontOriginal = searchField.getFont();
		Font fontHint = new Font(fontOriginal.getName(), Font.ITALIC, fontOriginal.getSize());
		searchField.setUI(new HintTextFieldUI("search...", fontOriginal, fontHint));
		
		contentPane.add(searchField, BorderLayout.NORTH);
	}
	
	private void newFile() {
		char[] pw = requestPassword();
		PasswordManager.getInstance().create(pw);
		listModel.update();
	}
	
	private void open() {
		File file = requestFile("Open file", "Open");
		if(file == null) {
			return;
		}
		if(!file.exists()) {
			JOptionPane.showMessageDialog(instance, "File does not exist!", "Not found!", JOptionPane.ERROR_MESSAGE);
			return;
		}
		char[] pw = requestPassword();
		if(pw == null) {
			return;
		}
		PasswordManager.getInstance().load(file, pw);
		listModel.update();
	}

	private void save() {
		File file = requestFile("Save file", "Save");
		if(file == null) {
			return;
		}
		if(file.exists()) {
			int option = JOptionPane.showConfirmDialog(instance, "This file already exist. Do you want to overwrite it?", "File exists", JOptionPane.INFORMATION_MESSAGE);
			if(option != JOptionPane.OK_OPTION) {
				return;
			}
		}
		PasswordManager.getInstance().save(file);
	}
	
	private char[] requestPassword() {
		final JPasswordField passwordField = new JPasswordField(10);
        passwordField.setEchoChar('\u2022'); // Unicode bullet character
        JOptionPane.showMessageDialog(
                this,
                passwordField,
                "Enter password",
                JOptionPane.DEFAULT_OPTION);
        return passwordField.getPassword();
	}
	
	private File requestFile(String title, String buttonText) {
		if(chooser == null) {
			chooser = new JFileChooser();
			FileFilter ff = new FileFilter() {
				@Override
				public boolean accept(File file) {
					if(file.isDirectory())
						return true;
					
					return file.getName().toLowerCase().endsWith(".pws");
				}
				@Override
				public String getDescription() {
					return "Password Safes (.pws)";
				}
			};
			chooser.addChoosableFileFilter(ff);
			chooser.setFileFilter(ff);
			
		}
		chooser.setDialogTitle(title);
		int status = chooser.showDialog(this, buttonText);
		if(status == JFileChooser.APPROVE_OPTION) {
			return chooser.getSelectedFile();
		} else {
			return null;
		}
	}
	
	private void newEntry() {
		String input = AdvancedJOptionPane.showMultipleLineInputDialog("New entry: ");
		if(input != null) {
			PasswordManager.getInstance().newEntry(input.trim().toCharArray());
			listModel.update();
		}
	}
	
	private void deleteEntry() {
		int index = listModel.getSelectedEntryIndex();
		if(index == -1) {
			JOptionPane.showMessageDialog(this, "You have to choose an entry to delete!", "No entry selected", JOptionPane.ERROR_MESSAGE);
			return;
		}
		if(JOptionPane.showConfirmDialog(this, "Are you sure you want to delete this entry?", "Comfirm delete", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
			PasswordManager.getInstance().deleteEntry(index);
			listModel.update();
		}
	}
	
	private void editEntry() {
		int index = listModel.getSelectedEntryIndex();
		if(index == -1) {
			JOptionPane.showMessageDialog(this, "You have to choose an entry to edit!", "No entry selected", JOptionPane.ERROR_MESSAGE);
			return;
		}
		String entry = String.valueOf(PasswordManager.getInstance().getEntries().get(index));				
		String input = AdvancedJOptionPane.showMultipleLineInputDialog("Edit entry: ", entry);
		if(input != null) {
			PasswordManager.getInstance().editEntry(index, input.trim().toCharArray());
			listModel.update();
		}
	}

	@Override
	public void onException(String source, Exception e) {
		JOptionPane.showMessageDialog(this, e.getMessage(), "Exception while: " + source, JOptionPane.ERROR_MESSAGE);
	}

	@Override
	public void changedUpdate(DocumentEvent e) {
		listModel.setSearchPattern(searchField.getText());
	}

	@Override
	public void insertUpdate(DocumentEvent e) {
		listModel.setSearchPattern(searchField.getText());
	}

	@Override
	public void removeUpdate(DocumentEvent e) {
		listModel.setSearchPattern(searchField.getText());
	}
}
