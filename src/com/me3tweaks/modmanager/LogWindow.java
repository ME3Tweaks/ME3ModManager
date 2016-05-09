package com.me3tweaks.modmanager;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Scanner;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.border.EmptyBorder;
import javax.swing.text.AttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;

import com.me3tweaks.modmanager.help.HelpMenu;
import com.me3tweaks.modmanager.utilities.DebugLogger;

public class LogWindow extends JFrame {

	public LogWindow() {
		ModManager.debugLogger.writeMessage("Opening Logging Window");
		setupWindow();
		setLocationRelativeTo(ModManagerWindow.ACTIVE_WINDOW);
		setVisible(true);
	}

	public void setupWindow() {
		setTitle("Mod Manager Debugging Log");
		setPreferredSize(new Dimension(720, 480));
		setIconImages(ModManager.ICONS);

		JPanel mainPanel = new JPanel(new BorderLayout());
		mainPanel.setBorder(new EmptyBorder(5,5,5,5));
		JTextPane logArea = new JTextPane();
		JButton copyLog, findNextError;

		copyLog = new JButton("Copy log to clipboard");
		findNextError = new JButton("Find Next Error");

		copyLog.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				HelpMenu.copyLogToClipboard();
			}
		});
		
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
		buttonPanel.add(Box.createHorizontalGlue());
		buttonPanel.add(findNextError);
		buttonPanel.add(Box.createHorizontalGlue());
		buttonPanel.add(copyLog);
		buttonPanel.add(Box.createHorizontalGlue());

		JScrollPane scrollPane = new JScrollPane(logArea, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		mainPanel.add(scrollPane, BorderLayout.CENTER);
		mainPanel.add(buttonPanel, BorderLayout.SOUTH);
		add(mainPanel);
		pack();

		//get log
		String log = ModManager.debugLogger.getLog();
		Scanner scanner = new Scanner(log);
		while (scanner.hasNextLine()) {
			String line = scanner.nextLine();
			System.out.println("Parsing log line "+line);
			if (line.startsWith(DebugLogger.ERROR_PREFIX) || line.startsWith(DebugLogger.EN_EXCEPTION_PREFIX)) {
				appendToPane(logArea,line,Color.RED);
			} else {
				appendToPane(logArea,line,Color.BLACK);
			}
		}
		logArea.setEditable(false);
		scanner.close();
	}

	private void appendToPane(JTextPane tp, String msg, Color c) {
		StyleContext sc = StyleContext.getDefaultStyleContext();
		AttributeSet aset = sc.addAttribute(SimpleAttributeSet.EMPTY, StyleConstants.Foreground, c);

		aset = sc.addAttribute(aset, StyleConstants.FontFamily, "Lucida Console");
		aset = sc.addAttribute(aset, StyleConstants.Alignment, StyleConstants.ALIGN_JUSTIFIED);

		int len = tp.getDocument().getLength();
		tp.setCaretPosition(len);
		tp.setCharacterAttributes(aset, false);
		tp.replaceSelection(msg+"\n");
	}
}
