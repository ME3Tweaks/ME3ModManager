package com.me3tweaks.modmanager;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Scanner;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.border.EmptyBorder;
import javax.swing.text.Element;

import org.apache.commons.io.input.Tailer;
import org.apache.commons.io.input.TailerListener;
import org.apache.commons.io.input.TailerListenerAdapter;

import com.me3tweaks.modmanager.help.HelpMenu;
import com.me3tweaks.modmanager.utilities.DebugLogger;
import com.me3tweaks.modmanager.utilities.ResourceUtils;

public class LogWindow extends JFrame {

	private JTextPane logArea;
	private Tailer tailer;
	private int caretline = 0;
	private int caretpos = 0;

	public LogWindow() {
		ModManager.debugLogger.writeMessage("Opening Logging Window");
		setupWindow();
		setupTailer();
		setLocationRelativeTo(ModManagerWindow.ACTIVE_WINDOW);
		setVisible(true);
	}

	private void setupTailer() {
		TailerListener listener = new LogTailer();
		File tailFile = new File(DebugLogger.LOGGING_FILENAME);
		tailer = Tailer.create(tailFile, new LogTailer(), 500, true);
	}

	class LogTailer extends TailerListenerAdapter {
		@Override
		public void handle(String line) {
			if (line.startsWith(DebugLogger.ERROR_PREFIX) || line.startsWith(DebugLogger.EN_EXCEPTION_PREFIX)) {
				ResourceUtils.appendToPane(logArea, line, Color.RED);
			} else {
				ResourceUtils.appendToPane(logArea, line, Color.BLACK);
			}
		}
	}

	private void shutdownTailer() {
		if (tailer != null) {
			tailer.stop();
		}
		ModManager.debugLogger.writeMessage("Log tailer shutting down");
	}

	public void setupWindow() {
		setTitle("Mod Manager Mod Manager log");
		setPreferredSize(new Dimension(720, 480));
		setIconImages(ModManager.ICONS);
		addWindowListener(new java.awt.event.WindowAdapter() {
			@Override
			public void windowClosing(java.awt.event.WindowEvent windowEvent) {
				shutdownTailer();
			}

		});
		JPanel mainPanel = new JPanel(new BorderLayout());
		mainPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		logArea = new JTextPane();
		JButton copyLog, findNextError;

		copyLog = new JButton("Copy log to clipboard");
		findNextError = new JButton("Find Next Error");

		copyLog.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				HelpMenu.copyLogToClipboard();
			}
		});

		findNextError.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String[] lines = logArea.getText().split("\\n");
				for (int i = caretline; i < lines.length; i++) {
					String line = lines[i];
					if (line.startsWith(DebugLogger.ERROR_PREFIX) || line.startsWith(DebugLogger.EN_EXCEPTION_PREFIX)) {
						logArea.setCaretPosition(caretpos);
						System.out.println("Set caret pos to "+caretpos);
						caretpos += line.length();
						caretline++;
						break;
					}
					caretpos += line.length();
					caretline++;
				}
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
			System.out.println("Parsing log line " + line);
			if (line.startsWith(DebugLogger.ERROR_PREFIX) || line.startsWith(DebugLogger.EN_EXCEPTION_PREFIX)) {
				ResourceUtils.appendToPane(logArea, line, Color.RED);
			} else {
				ResourceUtils.appendToPane(logArea, line, Color.BLACK);
			}
		}
		logArea.setEditable(false);
		scanner.close();
	}
}
