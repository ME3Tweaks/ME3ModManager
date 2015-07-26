package com.me3tweaks.modmanager.modupdater;

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
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingWorker;

import org.ini4j.InvalidFileFormatException;
import org.ini4j.Wini;

import com.me3tweaks.modmanager.ModManager;
import com.me3tweaks.modmanager.ModManagerWindow;
import com.me3tweaks.modmanager.objects.Mod;

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
		this.setTitle("Mod Updater");
		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		this.setPreferredSize(new Dimension(320, 70));
		this.setResizable(false);
		this.setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
		this.setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/resource/icon32.png")));
		this.pack();
		this.setLocationRelativeTo(callingWindow);

		JPanel panel = new JPanel(new BorderLayout());

		statusLabel = new JLabel("Connecting");
		operationLabel = new JLabel("Obtaining latest mod information from ME3Tweaks...");
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
				System.out.println("Dialog is closing");
				if (amdt != null) {
					amdt.cancel(false);
				}
			}
		});
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
	class AllModsDownloadTask extends SwingWorker<Void, Object> {
		int numToGo = 0;
		boolean canceled = false;

		public void pause() {
			try {
				this.wait();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		public synchronized void resume() {
			this.notify();
			ModManager.debugLogger.writeMessage("Sent resume wakeup notification");
		}

		/**
		 * Executed in background thread
		 */
		@Override
		protected Void doInBackground() throws Exception {
			// Iterate through files to download and put them in the update
			// folder
			upackages = new ArrayList<UpdatePackage>();
			for (Mod mod : updatableMods) {
				if (canceled) {
					return null;
				}
				publish(mod.getModName());
				UpdatePackage upackage = ModXMLTools.validateLatestAgainstServer(mod);
				if (upackage != null) {
					// update available
					upackages.add(upackage);
				} else {
					ModManager.debugLogger.writeMessage(mod.getModName() + " is up to date/not eligible");
				}
			}

			if (upackages.size() <= 0) {
				return null;
			}

			if (canceled) {
				return null;
			}

			publish("PROMPT_USER");
			while (AllModsUpdateWindow.this.userChose == 0) {
				Thread.sleep(500);
			}

			if (AllModsUpdateWindow.this.userChose < 0) {
				return null;
			}

			//user chose yes
			publish("NOTIFY_START");
			numToGo = upackages.size();
			for (UpdatePackage upackage : upackages) {
				if (canceled) {
					return null;
				}
				publish(new Integer(numToGo));
				ModManager.debugLogger.writeMessage("Processing: " + upackage.getMod().getModName());
				ModUpdateWindow muw = new ModUpdateWindow(upackage);
				muw.startAllModsUpdate(AllModsUpdateWindow.this);
				while (muw.isShowing()) {
					Thread.sleep(500);
				}
				publish(new Integer(--numToGo));
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
						String updatetext = upackages.size() + " mod" + (upackages.size() == 1 ? " has" : "s have") + " available updates on ME3Tweaks:\n";
						for (UpdatePackage upackage : upackages) {
							updatetext += " - " + upackage.getMod().getModName() + " " + upackage.getMod().getVersion() + " => " + upackage.getVersion() + "\n";
						}
						updatetext += "Update these mods?";
						int result = JOptionPane.showConfirmDialog(AllModsUpdateWindow.this, updatetext, "Mod updates available", JOptionPane.YES_NO_OPTION);
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
						operationLabel.setText("Updating mods from ME3Tweaks.com");
						break;
					default:
						statusLabel.setText("Checking "+command);
						break;
					}
					return;
				}
				if (obj instanceof Integer) {
					//its a progress update
					Integer i = (Integer) obj;
					statusLabel.setText(i + " mod" + (i == 1 ? "" : "s") + " out of date");
				}
			}

		}

		/**
		 * Executed in Swing's event dispatching thread
		 */
		@Override
		protected void done() {
			if (!showUI) {
				ModManager.debugLogger.writeMessage("Auto-Updater thread: performing done()");
				//autoupdate, update last check date
				Wini ini;
				try {
					File settings = new File(ModManager.settingsFilename);
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
					System.err.println("Settings file encountered an I/O error while attempting to write it. Settings not saved.");
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

			if (upackages.size() <= 0) {
				if (AllModsUpdateWindow.this.showUI) {
					JOptionPane.showMessageDialog(callingWindow, "All updatable mods are up to date.", "Mods up to date", JOptionPane.INFORMATION_MESSAGE);
				} else {
					ModManagerWindow.ACTIVE_WINDOW.labelStatus.setText("Auto updater: Mods are up to date");
				}
				return;
			}

			if (AllModsUpdateWindow.this.userChose < 0) {
				return;
			}

			JOptionPane.showMessageDialog(callingWindow, upackages.size() + " mod(s) have been successfully updated.\nMod Manager will now reload mods.","Mods updated", JOptionPane.INFORMATION_MESSAGE);
			new ModManagerWindow(false);
		}
	}

	public void continueUpdating() {
		ModManager.debugLogger.writeMessage("Resuming all-mods update thread");
		amdt.resume();

	}
}
