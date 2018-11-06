package com.me3tweaks.modmanager.modupdater;

import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Window;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CancellationException;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingWorker;

import org.ini4j.InvalidFileFormatException;
import org.ini4j.Wini;
import org.jdesktop.swingx.VerticalLayout;

import com.me3tweaks.modmanager.ModManager;
import com.me3tweaks.modmanager.ModManagerWindow;
import com.me3tweaks.modmanager.objects.Mod;
import com.me3tweaks.modmanager.objects.ThreadCommand;
import com.me3tweaks.modmanager.utilities.ResourceUtils;

@SuppressWarnings("serial")
public class AllModsUpdateWindow extends JDialog {
	boolean error = false;
	int userChose = 0; // -1 no, 0 hasn't picked, 1 yes
	JButton cancelButton;
	JLabel statusLabel;
	private JFrame callingWindow;
	private ArrayList<Mod> updatableMods;
	private ArrayList<UpdatePackage> upackages;
	private AllModsDownloadTask amdt;
	private JLabel operationLabel;
	private boolean showUI;

	public AllModsUpdateWindow(JFrame callingWindow, boolean showUI, ArrayList<Mod> updatableMods) {
		this.updatableMods = updatableMods;
		this.callingWindow = callingWindow;
		this.showUI = showUI;
		setupWindow();
		amdt = new AllModsDownloadTask();
		amdt.execute();
		if (showUI) {
			setVisible(true);
		}
	}

	public AllModsDownloadTask getAmdt() {
		return amdt;
	}

	public void setAmdt(AllModsDownloadTask amdt) {
		this.amdt = amdt;
	}

	private void setupWindow() {
		setTitle("Mod Updater");
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		setResizable(false);
		setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
		setIconImages(ModManager.ICONS);
		JPanel panel = new JPanel(new VerticalLayout());

		statusLabel = new JLabel("Downloading server manifest");
		operationLabel = new JLabel("Downloading mod manifests from ME3Tweaks.com");
		panel.add(operationLabel);
		panel.add(statusLabel);
		// updatePanel.add(actionPanel);
		// updatePanel.add(sizeLabel);

		// aboutPanel.add(loggingMode, BorderLayout.SOUTH);
		panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		this.getContentPane().add(panel);

		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosed(WindowEvent e) {
				if (amdt != null) {
					amdt.cancel(false);
				}
			}
		});
		pack();
		setLocationRelativeTo(callingWindow);
	}

	void setStatusText(String text) {
		statusLabel.setText(text);
	}

	/**
	 * Execute file download in a background thread and update the progress.
	 * 
	 * @author www.codejava.net
	 * 
	 */
	class AllModsDownloadTask extends SwingWorker<Void, ThreadCommand> {
		int numToGo = 0;
		boolean canceled = false;
		ArrayList<UpdatePackage> completedUpdates = new ArrayList<UpdatePackage>();
		int jobCode;

		public AllModsDownloadTask() {
			jobCode = ModManagerWindow.ACTIVE_WINDOW.submitBackgroundJob("AllModsUpdateCheck","Checking all mods for updates");
		}

		/**
		 * Executed in background thread
		 */
		@Override
		protected Void doInBackground() throws Exception {
			// Iterate through files to download and put them in the update
			// folder
			upackages = ModXMLTools.validateLatestAgainstServer(updatableMods, this);
			if (upackages.size() <= 0) {
				publish(new ThreadCommand("NO_UPDATES"));
				return null;
			}

			publish(new ThreadCommand("PROMPT_USER"));
			while (AllModsUpdateWindow.this.userChose == 0) {
				Thread.sleep(500); //make thread sleep until an answer on another thread comes back. this is such a hack.
			}

			if (AllModsUpdateWindow.this.userChose < 0) {
				return null; //declined
			}

			//user chose yes
			publish(new ThreadCommand("NOTIFY_START"));
			numToGo = upackages.size();
			for (UpdatePackage upackage : upackages) {
				if (canceled) {
					return null;
				}
				if (upackage.requiresSideload()) {
					numToGo--;
				}

				publish(new ThreadCommand("NUM_REMAINING", null, numToGo));
				ModManager.debugLogger.writeMessage("Processing: " + upackage.getMod().getModName());
				ModUpdateWindow muw = new ModUpdateWindow(upackage);
				boolean success = muw.startAllModsUpdate(AllModsUpdateWindow.this);
				ModManager.debugLogger.writeMessage("Update complete. The result of the task was " + (success ? "SUCCESS" : "FAILURE"));
				if (success) {
					completedUpdates.add(upackage);
				}
				while (muw.isShowing()) {
					Thread.sleep(350);
				}
				publish(new ThreadCommand("NUM_REMAINING", null, --numToGo));
			}

			return null;
		}

		@Override
		public void process(List<ThreadCommand> chunks) {
			for (ThreadCommand obj : chunks) {
				String command = obj.getCommand();
				switch (command) {
				case "NO_UPDATES":
					ModManagerWindow.ACTIVE_WINDOW.labelStatus.setText("All mods are up to date");
					break;
				case "PROMPT_USER":
					ModManagerWindow.ACTIVE_WINDOW.submitJobCompletion(jobCode);
					String statusMessage = "1 mod has an update available";
					if (upackages.size() > 1) {
						statusMessage = upackages.size() + " mods have updates available";
					}
					ModManagerWindow.ACTIVE_WINDOW.labelStatus.setText(statusMessage);
					//show sideload notice
					ArrayList<UpdatePackage> ignoredpackages = new ArrayList<>();
					for (UpdatePackage upackage : upackages) {
						if (upackage.requiresSideload()) {
							ignoredpackages.add(upackage);
							Window center = callingWindow;
							if (showUI) {
								center = AllModsUpdateWindow.this;
							}
							JOptionPane.showMessageDialog(center, upackage.getMod().getModName()
									+ " has an update available from ME3Tweaks, but requires a sideloaded update first.\nAfter this dialog is closed, a browser window will open where you can download this sideload update.\nDrag and drop this downloaded file onto Mod Manager to install it.\nAfter the sideloaded update is complete, Mod Manager will download the rest of the update.\n\nThis is to save on bandwidth costs for both ME3Tweaks and the developer of "
									+ upackage.getMod().getModName() + ".", "Sideload update required", JOptionPane.WARNING_MESSAGE);
							try {
								ResourceUtils.openWebpage(new URL(upackage.getSideloadURL()));
							} catch (MalformedURLException e) {
								ModManager.debugLogger.writeError("Invalid sideload URL: " + upackage.getSideloadURL());
								JOptionPane.showMessageDialog(AllModsUpdateWindow.this,
										upackage.getMod().getModName() + " specified an invalid URL for it's sideload upload:\n" + upackage.getSideloadURL(),
										"Invalid Sideload URL", JOptionPane.ERROR_MESSAGE);
							}
						}
					}
					upackages.removeAll(ignoredpackages);
					if (upackages.size() <= 0) {
						//abort updates since none are available
						userChose = -1;
						canceled = true;
						return;
					}
					String updatetext = upackages.size() + " mod" + (upackages.size() == 1 ? " has" : "s have") + " available updates on ME3Tweaks:\n";
					for (UpdatePackage upackage : upackages) {
						ModManager.debugLogger.writeMessage("Parsing upackage " + upackage.getServerModName() + "");
						updatetext += getVersionUpdateString(upackage);
						if (upackage.getChangeLog() != null && !upackage.getChangeLog().equals("")) {
							updatetext += "   Changelog: " + upackage.getChangeLog() + "\n";
						}
					}
					if (upackages.size() > 1) {
						updatetext += "Update these mods?";
					} else {
						updatetext += "Update this mod?";
					}
					ModManager.debugLogger.writeMessage("Prompting users for updates");
					Window center = callingWindow;
					if (showUI) {
						center = AllModsUpdateWindow.this;
					}
					int result = JOptionPane.showConfirmDialog(center, updatetext, "Mod updates available", JOptionPane.YES_NO_OPTION);
					switch (result) {
					case JOptionPane.YES_OPTION:
						userChose = 1;
						setVisible(true);
						break;
					case JOptionPane.NO_OPTION:
						userChose = -1;
						canceled = true;
						break;
					}
					break;
				case "NOTIFY_START":
					AllModsUpdateWindow.this.setLocation(getX(), (getY() - 160));
					operationLabel.setText("Updating mods from ME3Tweaks");
					break;
				case "MANIFEST_DOWNLOADED":
					statusLabel.setText("Calculating files for update");
					break;
				case "CALCULATING_PROGRESS":
					statusLabel.setText(obj.getMessage());
					break;
				case "NUM_REMAINING":
					Integer i = (Integer) obj.getData();
					statusLabel.setText(i + " mod" + (i == 1 ? "" : "s") + " out of date");
					break;
				case "DISPOSE":
					dispose();
					break;
				default:
					operationLabel.setText("Checking " + command);
					break;
				}
			}

		}

		/**
		 * Executed in Swing's event dispatching thread
		 */
		@Override
		protected void done() {
			ModManager.debugLogger.writeMessage("Auto-Updater thread: performing done()");
			ModManagerWindow.ACTIVE_WINDOW.submitJobCompletion(jobCode);
			try {
				get();
			} catch (CancellationException e) {
				dispose();
			} catch (Exception e) {
				dispose();
				ModManager.debugLogger.writeException(e);
				ModManager.debugLogger.writeMessage("Auto-Updater thread likely ended pre-maturely due to an exception.");
			}
			if (!showUI) {
				//autoupdate, update last check date
				Wini ini;
				try {
					File settings = new File(ModManager.SETTINGS_FILENAME);
					if (!settings.exists())
						settings.createNewFile();
					ini = new Wini(settings);
					ini.put("Settings", "lastautocheck", System.currentTimeMillis());
					ModManager.debugLogger.writeMessage("Updating last-autocheck date in ini");
					ini.store();
					ModManager.LAST_AUTOUPDATE_CHECK = System.currentTimeMillis();
				} catch (InvalidFileFormatException e) {
					e.printStackTrace();
				} catch (IOException e) {
					ModManager.debugLogger.writeErrorWithException("Settings file encountered an I/O error while attempting to write it. Settings not saved.", e);
				}
			}

			if (isCancelled() && !canceled) {
				canceled = true; //next canceled check will dispose of this window
				ModManagerWindow.ACTIVE_WINDOW.labelStatus.setText("Reload ME3CMM to see completed updates (if any)");
				return;
			}

			dispose();
			if (canceled) {
				return;
			}

			if (upackages == null || upackages.size() <= 0) {
				ModManagerWindow.ACTIVE_WINDOW.labelStatus.setText("Mods are up to date");
				if (AllModsUpdateWindow.this.showUI) {
					JOptionPane.showMessageDialog(callingWindow, "All updatable mods are up to date.", "Mods up to date", JOptionPane.INFORMATION_MESSAGE);
				}
				return;
			}

			if (AllModsUpdateWindow.this.userChose < 0) {
				return;
			}

			if (completedUpdates.size() == 0) {
				ModManagerWindow.ACTIVE_WINDOW.labelStatus.setText("Error applying updates");
				JOptionPane.showMessageDialog(callingWindow, "No mods successfully updated.\nCheck the Mod Manager log for more info or contact Mgamerz for help.",
						"Mods failed to update", JOptionPane.ERROR_MESSAGE);
			} else if (upackages.size() != completedUpdates.size()) {
				ModManagerWindow.ACTIVE_WINDOW.labelStatus.setText("Error applying some of the updates");
				//one error occured
				JOptionPane.showMessageDialog(callingWindow,
						completedUpdates.size() + " mod(s) successfully updated.\n" + (upackages.size() - completedUpdates.size())
								+ " failed to update.\nYou will need to apply updated mod(s) for them to take effect.\nMod Manager will now reload mods.",
						"Some mods were updated", JOptionPane.WARNING_MESSAGE);
			} else {
				ModManagerWindow.ACTIVE_WINDOW.labelStatus.setText("Updates downloaded");
				JOptionPane.showMessageDialog(callingWindow,
						upackages.size()
								+ " mod(s) have been successfully updated.\nYou will need to apply updated mod(s) for them to take effect.",
						"Mods updated", JOptionPane.INFORMATION_MESSAGE);
			}

			ModManagerWindow.ACTIVE_WINDOW.reloadModlist();
		}

		public void setManifestDownloaded() {
			publish(new ThreadCommand("MANIFEST_DOWNLOADED"));
		}

		public void setUpdateCalculationProgress(int done, int total) {
			publish(new ThreadCommand("CALCULATING_PROGRESS", "Calculating files for update [" + done + "/" + total + "]"));
		}

		public void publishUpdate(String update) {
			publish(new ThreadCommand(update));
		}
	}

	/**
	 * Returns a standard Verision X => new Version (size, num files deleted)
	 * string
	 * 
	 * @param upackage
	 *            update package
	 * @return nice string
	 */
	public static String getVersionUpdateString(UpdatePackage upackage) {
		if (upackage.getMod().getModMakerCode() > 0) {
			//modmaker mod
			return " - " + upackage.getMod().getModName() + " " + upackage.getMod().getVersion() + " => " + upackage.getVersion() + " (ModMaker mod)\n";
		} else {
			//classic
			int numFilesToDelete = 0;
			if (upackage.getFilesToDelete() != null) {
				numFilesToDelete = upackage.getFilesToDelete().size();
			}

			String updateSizeMB = upackage.getUpdateSizeMB();
			String filesToDeleteStr = numFilesToDelete > 0 ? ", " + numFilesToDelete + " item" + (numFilesToDelete != 1 ? "s" : "") + " to delete" : "";

			return " - " + upackage.getMod().getModName() + " " + upackage.getMod().getVersion() + " => " + upackage.getVersion()
					+ (upackage.isModmakerupdate() ? "" : " (" + updateSizeMB + " download" + filesToDeleteStr + ")") + "\n";
		}
	}
}
