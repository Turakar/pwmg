package de.thoffbauer.pwmg.util;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.plaf.basic.BasicTextFieldUI;
import javax.swing.text.JTextComponent;

/*
 * Taken from: https://stackoverflow.com/a/4962829
 * Author: culmat
 * added italic font
 */

public class HintTextFieldUI extends BasicTextFieldUI implements FocusListener {

	private String hint;
	private boolean hideOnFocus;
	private Color color;
	private Font fontOriginal;
	private Font fontHint;

	public HintTextFieldUI(String hint, Font fontOriginal, Font fontHint) {
		this(hint, false, fontOriginal, fontHint);
	}

	public HintTextFieldUI(String hint, boolean hideOnFocus, Font fontOriginal, Font fontHint) {
		this(hint, hideOnFocus, null, fontOriginal, fontHint);
	}

	public HintTextFieldUI(String hint, boolean hideOnFocus, Color color,
			Font fontOriginal, Font fontHint) {
		this.hint = hint;
		this.hideOnFocus = hideOnFocus;
		this.color = color;
		this.fontOriginal = fontOriginal;
		this.fontHint = fontHint;
	}

	public Color getColor() {
		return color;
	}

	public void setColor(Color color) {
		this.color = color;
		repaint();
	}

	private void repaint() {
		if (getComponent() != null) {
			getComponent().repaint();
		}
	}

	public boolean isHideOnFocus() {
		return hideOnFocus;
	}

	public void setHideOnFocus(boolean hideOnFocus) {
		this.hideOnFocus = hideOnFocus;
		repaint();
	}

	public String getHint() {
		return hint;
	}

	public void setHint(String hint) {
		this.hint = hint;
		repaint();
	}

	@Override
	protected void paintSafely(Graphics g) {
		super.paintSafely(g);
		JTextComponent comp = getComponent();

		if (hint != null && comp.getText().length() == 0
				&& (!(hideOnFocus && comp.hasFocus()))) {
			if (color != null) {
				g.setColor(color);
			} else {
				comp.setFont(fontHint);
				g.setColor(comp.getForeground().brighter().brighter().brighter());
			}
			int padding = (comp.getHeight() - comp.getFont().getSize()) / 2;
			g.drawString(hint, 2, comp.getHeight() - padding - 1);
		} else {
			comp.setFont(fontOriginal);
		}
	}

	@Override
	public void focusGained(FocusEvent e) {
		if (hideOnFocus)
			repaint();

	}

	@Override
	public void focusLost(FocusEvent e) {
		if (hideOnFocus)
			repaint();
	}

	@Override
	protected void installListeners() {
		super.installListeners();
		getComponent().addFocusListener(this);
	}

	@Override
	protected void uninstallListeners() {
		super.uninstallListeners();
		getComponent().removeFocusListener(this);
	}
}