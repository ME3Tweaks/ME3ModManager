package com.me3tweaks.modmanager;

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingWorker;

import org.ini4j.InvalidFileFormatException;
import org.ini4j.Wini;

import com.me3tweaks.modmanager.modupdater.AllModsUpdateWindow;
import com.me3tweaks.modmanager.modupdater.ModUpdateWindow;
import com.me3tweaks.modmanager.modupdater.ModXMLTools;
import com.me3tweaks.modmanager.modupdater.UpdatePackage;
import com.me3tweaks.modmanager.objects.Mod;
import com.me3tweaks.modmanager.objects.Patch;

public class PatchApplicationWindow extends JDialog {
	private JLabel statusLabel;
	private JLabel operationLabel;
	private JDialog callingWindow;
	private ArrayList<Patch> patches;
	private Mod mod;
	private PatchApplicationTask pat;

	public PatchApplicationWindow(JDialog callingWindow, ArrayList<Patch> patches, Mod mod) {
		this.callingWindow = callingWindow;
		this.patches = patches;
		this.mod = mod;
		setupWindow();
		pat = new PatchApplicationTask();
		pat.execute();
		setVisible(true);
	}

	private void setupWindow() {
		this.setTitle("Mod Updater");
		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		this.setPreferredSize(new Dimension(320, 70));
		this.setResizable(false);
		this.setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
		this.setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/resource/icon32.png")));
		this.pack();
		this.setLocationRelativeTo(callingWindow);

		JPanel panel = new JPanel(new BorderLayout());

		statusLabel = new JLabel("Applying patches to " + mod.getModName());
		operationLabel = new JLabel("Applying: ");
		panel.add(operationLabel, BorderLayout.NORTH);
		panel.add(statusLabel, BorderLayout.SOUTH);
		// updatePanel.add(actionPanel);
		// updatePanel.add(sizeLabel);

		// aboutPanel.add(loggingMode, BorderLayout.SOUTH);
		panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		this.getContentPane().add(panel);

		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosed(WindowEvent e) {
				System.out.println("Exit Patch Application");
				if (pat != null) {
					pat.cancel(false);
				}
			}
		});
	}

	class PatchApplicationTask extends SwingWorker<Void, Object> {
		int numToGo = 0;
		boolean canceled = false;

		/**
		 * Executed in background thread
		 */
		@Override
		protected Void doInBackground() throws Exception {
			for (Patch patch : patches) {
				if (isCancelled()) {
					publish(patch.getPatchName());
					patch.applyPatch(mod);
				}
			}
			return null;
		}

		@Override
		public void process(List<Object> chunks) {
			for (Object obj : chunks) {
				if (obj instanceof String) {
					//its a command
					String command = (String) obj;
					switch (command) {
					case "PROMPT_USER":
						break;
					case "NOTIFY_START":
						break;
					default:
						statusLabel.setText("Applying: " + command);
						break;
					}
					return;
				}
			}
		}

		/**
		 * Executed in Swing's event dispatching thread
		 */
		@Override
		protected void done() {
			dispose();
		}
	}
}
