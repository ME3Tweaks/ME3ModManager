package com.me3tweaks.modmanager.modupdater;

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingWorker;

import org.apache.commons.io.FileUtils;

import com.me3tweaks.modmanager.Mod;
import com.me3tweaks.modmanager.ModManager;
import com.me3tweaks.modmanager.ModManagerWindow;
import com.me3tweaks.modmanager.ResourceUtils;
import com.me3tweaks.modmanager.modupdater.ModUpdateWindow.HTTPDownloadUtil;

@SuppressWarnings("serial")
public class AllModsUpdateWindow extends JDialog {
	boolean error = false;
	int userChose = 0; // -1 no, 0 hasn't picked, 1 yes
	JButton cancelButton;
	JLabel statusLabel;
	private JFrame callingWindow;
	private ArrayList<Mod> updateableMods;
	private ArrayList<UpdatePackage> upackages;
	private AllModsDownloadTask amdt;
	private JLabel operationLabel;

	public AllModsUpdateWindow(JFrame callingWindow, ArrayList<Mod> updateableMods) {
		this.updateableMods = updateableMods;
		this.callingWindow = callingWindow;

		setupWindow();
		amdt = new AllModsDownloadTask();
		amdt.execute();
		setVisible(true);
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

		statusLabel = new JLabel("0 mods out of date");
		operationLabel = new JLabel("Obtaining latest mod information from ME3Tweaks...");
		panel.add(operationLabel, BorderLayout.NORTH);
		panel.add(statusLabel, BorderLayout.SOUTH);
		// updatePanel.add(actionPanel);
		// updatePanel.add(sizeLabel);

		// aboutPanel.add(loggingMode, BorderLayout.SOUTH);
		panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		this.getContentPane().add(panel);
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
			for (Mod mod : updateableMods) {
				UpdatePackage upackage = ModXMLTools.validateLatestAgainstServer(mod);
				if (upackage != null) {
					// update available
					upackages.add(upackage);
					publish(new Integer(upackages.size()));
				} else {
					ModManager.debugLogger.writeMessage(mod.getModName() + " is up to date/not eligible");
				}
			}

			if (upackages.size() <= 0) {
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
			int numToGo = upackages.size();
			for (UpdatePackage upackage : upackages) {
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
						String updatetext = upackages.size()+" mod"+(upackages.size()==1?" has":"s have")+" available updates on ME3Tweaks:\n";
						for (UpdatePackage upackage : upackages) {
							updatetext += " - "+upackage.getMod().getModName()+" "+upackage.getMod().getVersion()+" => "+upackage.getVersion()+"\n";
						}
						updatetext += "Update these mods?";
						int result = JOptionPane.showConfirmDialog(AllModsUpdateWindow.this, updatetext, "Mod updates available", JOptionPane.YES_NO_OPTION);
						switch (result) {
						case JOptionPane.YES_OPTION:
							userChose = 1;
							break;
						case JOptionPane.NO_OPTION:
							userChose = -1;
							break;
						}
						break;
					case "NOTIFY_START":
						AllModsUpdateWindow.this.setLocation(getX(), (getY() - 160));
						operationLabel.setText("Updating mods from ME3Tweaks.com");
						break;
					}
					return;
				}
				if (obj instanceof Integer) {
					//its a progress update
					Integer i = (Integer) obj;
					statusLabel.setText(i+" mod"+(i==1?"":"s")+" out of date");
				}
			}
			
		}

		/**
		 * Executed in Swing's event dispatching thread
		 */
		@Override
		protected void done() {
			dispose();

			if (upackages.size() <= 0) {
				JOptionPane.showMessageDialog(callingWindow, "All updatable mods are up to date.");
				return;
			}

			if (AllModsUpdateWindow.this.userChose < 0) {
				return;
			}

			JOptionPane.showMessageDialog(callingWindow, upackages.size() + " mod(s) have been successfully updated.\nMod Manager will now reload mods.");
			callingWindow.dispose();
			new ModManagerWindow(false);
		}
	}

	public void continueUpdating() {
		ModManager.debugLogger.writeMessage("Resuming all-mods update thread");
		amdt.resume();

	}
}
