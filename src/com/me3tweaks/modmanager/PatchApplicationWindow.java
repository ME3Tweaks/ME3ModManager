package com.me3tweaks.modmanager;

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingWorker;

import com.me3tweaks.modmanager.objects.Mod;
import com.me3tweaks.modmanager.objects.Patch;

public class PatchApplicationWindow extends JDialog {
	private JLabel statusLabel;
	private JLabel operationLabel;
	private JDialog callingDialog;
	private JFrame callingFrame;

	private ArrayList<Patch> patches;
	private ArrayList<Patch> failedPatches;
	private Mod mod;
	private PatchApplicationTask pat;

	public PatchApplicationWindow(JDialog callingDialog, ArrayList<Patch> patches, Mod mod) {
		ModManager.debugLogger.writeMessage("Starting mix-in applier.");
		this.callingDialog = callingDialog;
		this.patches = patches;
		this.mod = mod;
		setupWindow();
		pat = new PatchApplicationTask();
		pat.execute();
		setVisible(true);
	}
	
	public PatchApplicationWindow(JFrame callingFrame, ArrayList<Patch> patches, Mod mod) {
		ModManager.debugLogger.writeMessage("Starting mix-in applier.");
		this.callingFrame = callingFrame;
		this.patches = patches;
		this.mod = mod;
		setupWindow();
		pat = new PatchApplicationTask();
		pat.execute();
		setVisible(true);
	}
	
	public ArrayList<Patch> getFailedPatches() {
		return failedPatches;
	}

	private void setupWindow() {
		failedPatches = new ArrayList<Patch>();
		this.setTitle("Mix-in Installer");
		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		this.setPreferredSize(new Dimension(320, 70));
		this.setResizable(false);
		this.setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
		this.setIconImages(ModManager.ICONS);
		this.pack();
		if (callingDialog == null && callingDialog == null) {
			this.setLocationRelativeTo(null);
		} else {
			this.setLocationRelativeTo(callingDialog == null ? callingFrame : callingDialog);
		}
		JPanel panel = new JPanel(new BorderLayout());

		operationLabel = new JLabel("Applying mix-ins to " + mod.getModName());
		statusLabel = new JLabel("Applying: ");

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
				System.out.println("Exit Mix-in Application");
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
				if (!isCancelled()) {
					publish(patch.getPatchName());
					boolean applied = patch.applyPatch(mod);
					if (!applied) {
						failedPatches.add(patch);
					}
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
			try {
				get();
			} catch (Exception e) {
				ModManager.debugLogger.writeErrorWithException("Error running patchapplicationwindow thread:", e);
			}
			dispose();
		}
	}
}
