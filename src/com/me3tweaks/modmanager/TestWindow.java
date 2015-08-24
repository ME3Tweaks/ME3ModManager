package com.me3tweaks.modmanager;

import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

public class TestWindow extends JFrame implements ListSelectionListener {
	public TestWindow() {
		String[] items = {"item1", "item2", "item3"};
		JList<String> list = new JList<String>(items);
		list.addListSelectionListener(this);
		add(list);
		pack();
		setVisible(true);
	}

	@Override
	public void valueChanged(ListSelectionEvent e) {
		if (e.getValueIsAdjusting() == false) {
			System.out.println("value not adjusting");
		} else {
			System.out.println("value adjusting");
		}
	}

	public static void main(String[] args) {
		new TestWindow().setVisible(true);
	}
}
