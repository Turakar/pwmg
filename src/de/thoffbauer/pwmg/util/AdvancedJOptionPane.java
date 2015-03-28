package de.thoffbauer.pwmg.util;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.border.EmptyBorder;

/*
 * Taken from http://www.java-forums.org/awt-swing/542-multiple-line-input-dialog-box.html
 * removed test code, changed naming, added initial value support
 * Author: levent
 */
 
@SuppressWarnings("serial")
public class AdvancedJOptionPane extends JOptionPane {
	
	public static String showMultipleLineInputDialog(final String message) {
		return showMultipleLineInputDialog(message, null);
	}
	
	public static String showMultipleLineInputDialog(final String message, final String initial) {
		String data = null;
		
		class GetData extends JDialog implements ActionListener {
			JTextArea ta = new JTextArea(10, 40);
			JButton btnOK = new JButton("   OK   ");
			JButton btnCancel = new JButton("Cancel");
			String str = null;

			public GetData() {
				if(initial != null) {
					ta.setText(initial);
				}
				
				setModal(true);
				getContentPane().setLayout(new BorderLayout());
				((JComponent) getContentPane()).setBorder(new EmptyBorder(5, 5, 5, 5));
				setDefaultCloseOperation(DISPOSE_ON_CLOSE);
				setLocation(400, 300);
				getContentPane().add(new JLabel(message), BorderLayout.NORTH);
				getContentPane().add(ta, BorderLayout.CENTER);
				JPanel jp = new JPanel();
				btnOK.addActionListener(this);
				btnCancel.addActionListener(this);
				jp.add(btnOK);
				jp.add(btnCancel);
				getContentPane().add(jp, BorderLayout.SOUTH);
				pack();
				setVisible(true);
			}

			public void actionPerformed(ActionEvent ae) {
				if (ae.getSource() == btnOK)
					str = ta.getText();
				dispose();
			}

			public String getData() {
				return str;
			}
		}
		data = new GetData().getData();
		return data;
	}
}