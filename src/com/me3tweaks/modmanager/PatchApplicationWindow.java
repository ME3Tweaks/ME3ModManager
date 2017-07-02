package com.me3tweaks.modmanager;

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingWorker;

import org.apache.commons.io.FileUtils;

import com.me3tweaks.modmanager.ModManager.Lock;
import com.me3tweaks.modmanager.objects.Mod;
import com.me3tweaks.modmanager.objects.Patch;
import com.me3tweaks.modmanager.objects.PatchModBundle;
import com.me3tweaks.modmanager.objects.ThreadCommand;

public class PatchApplicationWindow extends JDialog {
	private JLabel statusLabel;
	private JLabel operationLabel;
	private JDialog callingDialog;
	private JFrame callingFrame;

	private ArrayList<Patch> patches;
	private ArrayList<Patch> failedPatches;
	private Mod mod;
	private PatchApplicationTask pat;
	public final Object lock = new Lock(); //threading wait() and notifyall();

	public PatchApplicationWindow(JDialog callingDialog, ArrayList<Patch> patches, Mod mod) {
		ModManager.debugLogger.writeMessage("Starting mix-in applier.");
		this.callingDialog = callingDialog;
		this.mod = mod;
		this.patches = prioritizePatches(patches);
		setupWindow();
		setLocationRelativeTo(callingDialog);
		pat = new PatchApplicationTask();
		pat.execute();
		setVisible(true);
	}

	/**
	 * Prioritizes non-finalizers before finalizers
	 * 
	 * @param patches
	 *            non-sorted patch list
	 * @return arraylist of non-finalizers then finalizers
	 */
	private ArrayList<Patch> prioritizePatches(ArrayList<Patch> patches) {
		ArrayList<Patch> sortedList = new ArrayList<Patch>();
		for (Patch patch : patches) {
			if (patch.isFinalizer()) {
				continue;
			}
			sortedList.add(patch);
		}

		//run again, this time only adding finalizers
		for (Patch patch : patches) {
			if (!patch.isFinalizer()) {
				continue;
			}
			sortedList.add(patch);
		}
		return sortedList;
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
		setTitle("MixIn Installer");
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		setPreferredSize(new Dimension(320, 70));
		setResizable(false);
		setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
		setIconImages(ModManager.ICONS);
		if (callingDialog == null && callingDialog == null) {
			setLocationRelativeTo(null);
		} else {
			setLocationRelativeTo(callingDialog == null ? callingFrame : callingDialog);
		}
		JPanel panel = new JPanel(new BorderLayout());

		operationLabel = new JLabel("Applying mixins to " + mod.getModName() + "[0/0]");
		statusLabel = new JLabel("Applying: ");
		panel.add(operationLabel, BorderLayout.NORTH);
		panel.add(statusLabel, BorderLayout.SOUTH);
		panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		getContentPane().add(panel);

		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosed(WindowEvent e) {
				System.out.println("Exit Mixin Application");
				if (pat != null) {
					pat.cancel(false);
				}
			}
		});

		pack();
		if (callingDialog == null && callingDialog == null) {
			this.setLocationRelativeTo(null);
		} else {
			this.setLocationRelativeTo(callingDialog == null ? callingFrame : callingDialog);
		}
	}

	class PatchApplicationTask extends SwingWorker<Void, Object> {
		int numToGo = 0;
		boolean canceled = false;

		/**
		 * Executed in background thread
		 */
		@Override
		protected Void doInBackground() throws Exception {
			int numcompleted = 0;
			for (Patch patch : patches) {
				if (!isCancelled()) {
					publish(new ThreadCommand("UPDATE_OPERATION_LABEL", null, numcompleted));
					publish(patch.getPatchName());
					int applyResult = patch.applyPatch(mod);
					if (applyResult != Patch.APPLY_SUCCESS) {
						failedPatches.add(patch);
						publish(new ThreadCommand("PATCH_FAILED", Integer.toString(applyResult), new PatchModBundle(patch, mod)));
					}
					if (patch.isDynamic()) {
						FileUtils.deleteQuietly(new File(patch.getPatchPath()));
					}
					numcompleted++;
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
					statusLabel.setText("Applying: " + command);
					return;
				}
				if (obj instanceof ThreadCommand) {
					ThreadCommand tc = (ThreadCommand) obj;
					boolean isSecondFailure = false;
					switch (tc.getCommand()) {
					case "UPDATE_OPERATION_LABEL":
						operationLabel.setText("Applying mixins to " + mod.getModName() + " [" + (int) tc.getData() + "/" + patches.size() + "]");
						break;
					case "PATCH_FAILED_AGAIN":
						isSecondFailure = true;
					case "PATCH_FAILED":
						int reason = Integer.parseInt(tc.getMessage());
						PatchModBundle bundle = (PatchModBundle) tc.getData();
						Mod mod = bundle.getMod();
						Patch patch = (Patch) bundle.getPatch();
						if (reason == Patch.APPLY_FAILED_SOURCE_FILE_WRONG_SIZE) {
							if (isSecondFailure) {
								JOptionPane.showMessageDialog(PatchApplicationWindow.this,
										patch.getPatchName()
												+ " failed to apply even after deleting the extracted copy.\nThe file installed in the game does not work with this MixIn because the file sizes are different.",
										"MixIn cannot be installed", JOptionPane.ERROR_MESSAGE);
							} else {
								if (patch != null) {
									int result = JOptionPane.showConfirmDialog(PatchApplicationWindow.this,
											patch.getPatchName()
													+ " failed to apply because the source file to patch was not the right size.\nThis means the file was likely modified in the game directory before it was extracted for patching or had a finalizer applied to it.\n\nMod Manager can delete this file and try to extract a new copy on the next application of this MixIn.\nIf this fails, make sure you restore to vanilla before applying this MixIn again.\n\nDelete source file? Files in the game are not modified by this operation.",
											"MixIn failed to apply", JOptionPane.YES_NO_OPTION);
									if (result == JOptionPane.YES_OPTION) {
										FileUtils.deleteQuietly(new File(patch.getSourceFilePath(mod)));
									}
								}
							}
						} else {
							//not fixable
							switch (reason) {
							case Patch.APPLY_FAILED_MODDESC_NOT_UPDATED:
								JOptionPane.showMessageDialog(PatchApplicationWindow.this,
										patch.getPatchName()
												+ " failed to apply because the moddesc.ini file was not updated.\nThis is an error in Mod Manager, please report it with a Mod Manager diagnostics log.",
										"MixIn failed to apply", JOptionPane.ERROR_MESSAGE);
								break;
							case Patch.APPLY_FAILED_NO_SOURCE_FILE:
								JOptionPane.showMessageDialog(PatchApplicationWindow.this,
										patch.getPatchName()
												+ " failed to apply because a valid source file could not be acquired.\nCheck the logs for more information.",
										"MixIn failed to apply", JOptionPane.ERROR_MESSAGE);
								break;
							case Patch.APPLY_FAILED_OTHERERROR:
								JOptionPane.showMessageDialog(PatchApplicationWindow.this,
										patch.getPatchName() + " failed to apply because a generic error occured. Report this to FemShep if you keep having this issue.",
										"MixIn failed to apply", JOptionPane.ERROR_MESSAGE);
								break;
							case Patch.APPLY_FAILED_SIZE_CHANGED:
								JOptionPane.showMessageDialog(PatchApplicationWindow.this,
										patch.getPatchName() + " was applied but the filesize of the output file changed,\nbut this MixIn was not marked as a finalizer.",
										"MixIn incorrectly marked", JOptionPane.ERROR_MESSAGE);
								break;
							}
						}
					}
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
