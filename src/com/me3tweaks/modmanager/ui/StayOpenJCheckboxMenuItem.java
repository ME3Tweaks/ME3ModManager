package com.me3tweaks.modmanager.ui;

import java.awt.event.MouseEvent;

import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JCheckBoxMenuItem;

public class StayOpenJCheckboxMenuItem extends JCheckBoxMenuItem {

	public StayOpenJCheckboxMenuItem() {
	}

	public StayOpenJCheckboxMenuItem(Icon icon) {
		super(icon);
	}

	public StayOpenJCheckboxMenuItem(String text) {
		super(text);
	}

	public StayOpenJCheckboxMenuItem(Action a) {
		super(a);
	}

	public StayOpenJCheckboxMenuItem(String text, Icon icon) {
		super(text, icon);
	}

	public StayOpenJCheckboxMenuItem(String text, boolean b) {
		super(text, b);
	}

	public StayOpenJCheckboxMenuItem(String text, Icon icon, boolean b) {
		super(text, icon, b);
	}

	@Override
	protected void processMouseEvent(MouseEvent evt) {
		if (evt.getID() == MouseEvent.MOUSE_RELEASED && contains(evt.getPoint())) {
			doClick();
			setArmed(true);
		} else {
			super.processMouseEvent(evt);
		}
	}
}
