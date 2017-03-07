package com.me3tweaks.modmanager;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;

import com.me3tweaks.modmanager.modmaker.ME3TweaksUtils;
import com.me3tweaks.modmanager.objects.CustomDLC;
import com.me3tweaks.modmanager.objects.ModType;
import com.me3tweaks.modmanager.objects.MountFile;
import com.me3tweaks.modmanager.objects.ThirdPartyModInfo;
import com.me3tweaks.modmanager.objects.ThreadCommand;
import com.me3tweaks.modmanager.ui.HintTextFieldUI;
import com.me3tweaks.modmanager.utilities.EXEFileInfo;
import com.me3tweaks.modmanager.utilities.MD5Checksum;

@SuppressWarnings("serial")
public class LogOptionsWindow extends JDialog {
	JCheckBox sessionoption;
	JTextField fdescription;
	private JCheckBox installeddlcoption, filetreeoption, dlcbypassinformation, customdlcconflictsoption, me3logfile;
	JCheckBox[] options;
	private JButton shareViaFile;
	private JButton shareViaPastebin;

	public LogOptionsWindow(JFrame callingWindow) {
		setupWindow();
		this.setLocationRelativeTo(callingWindow);
		this.setVisible(true);
	}

	private void setupWindow() {
		this.setTitle("Mod Manager Debugging Log Options");
		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		this.setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
		this.setIconImages(ModManager.ICONS);
		this.setResizable(false);

		JPanel optionsPanel = new JPanel();
		//optionsPanel.setLayout(new BoxLayout(optionsPanel, BoxLayout.PAGE_AXIS));
		optionsPanel.setLayout(new BoxLayout(optionsPanel, BoxLayout.PAGE_AXIS));
		optionsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		JLabel infoLabel = new JLabel("Select what information to include in this log file.");
		JTextField fname = new JTextField();
		fname.setUI(new HintTextFieldUI("Enter a custom file name for saving (optional)"));
		fdescription = new JTextField();
		fdescription.setUI(new HintTextFieldUI("Enter a short description why this log is being made"));
		sessionoption = new JCheckBox("Mod Manager Session Log");
		sessionoption.setToolTipText("<html>Include messages outputted from Mod Manager during this session. Includes system information, ASI, and mod manager activity</html>");
		sessionoption.setSelected(true);

		installeddlcoption = new JCheckBox("Installed DLC Information");
		installeddlcoption.setToolTipText("<html>Include a list of all installed DLC. This is almost always recommended as its is very helpful when troubleshooting</html>");
		installeddlcoption.setSelected(true);

		dlcbypassinformation = new JCheckBox("DLC Bypass Information");
		dlcbypassinformation.setToolTipText("<html>Include information about the installed DLC bypass, if any. Used for loading modified DLC</html>");
		dlcbypassinformation.setSelected(true);

		filetreeoption = new JCheckBox("Game File Tree (will take time to generate)");
		filetreeoption.setToolTipText("<html>Generates a file tree that is useful to find missing files</html>");
		filetreeoption.setSelected(false);

		customdlcconflictsoption = new JCheckBox("Custom DLC Conflicts");
		customdlcconflictsoption.setToolTipText("<html>Generates a list of files that conflict in Custom DLC. Useful for developers to troubleshoot mod compatibility</html>");
		customdlcconflictsoption.setSelected(false);
		customdlcconflictsoption.setSelected(true);


		me3logfile = new JCheckBox("ME3Logger Contents (Game crash info)");
		me3logfile.setToolTipText("<html>Imports the ME3Logger contents into the log. If this bypass/ASI was not installed (or the file does not exist), this does nothing</html>");
		me3logfile.setSelected(true);

		shareViaFile = new JButton("Save to disk");
		shareViaFile.setToolTipText("Saves this log to the logs directory.");

		shareViaPastebin = new JButton("Upload to pastebin");
		shareViaPastebin.setToolTipText(
				"Uploads the log to ME3Tweaks which is then posted to pastebin (unlisted). You will receive a link to your log and it will be stored in the logs folder");
		shareViaPastebin.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				boolean somethingChecked = false;
				for (JCheckBox cb : options) {
					if (cb.isSelected()) {
						somethingChecked = true;
						break;
					}
				}
				if (!somethingChecked) {
					JOptionPane.showMessageDialog(LogOptionsWindow.this, "You must check at least one box to generate a log.", "No log options selected",
							JOptionPane.ERROR_MESSAGE);
					return;
				}
				shareViaPastebin.setEnabled(false);
				shareViaFile.setEnabled(false);
				// TODO Auto-generated method stub
				String log = generateLog();
				new PasteBinUploaderDialog(log, fname.getText(), LogOptionsWindow.this);
			}
		});
		shareViaFile.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				boolean somethingChecked = false;
				for (JCheckBox cb : options) {
					if (cb.isSelected()) {
						somethingChecked = true;
						break;
					}
				}
				if (!somethingChecked) {
					JOptionPane.showMessageDialog(LogOptionsWindow.this, "You must check at least one box to generate a log.", "No log options selected",
							JOptionPane.ERROR_MESSAGE);
					return;
				}
				// TODO Auto-generated method stub
				shareViaPastebin.setEnabled(false);
				shareViaFile.setEnabled(false);
				// TODO Auto-generated method stub
				saveLogToDisk(fname.getText().trim(), true);
				dispose();
			}
		});

		options = new JCheckBox[] { installeddlcoption, filetreeoption, dlcbypassinformation, customdlcconflictsoption, me3logfile };

		sessionoption.setAlignmentX(Component.LEFT_ALIGNMENT);
		installeddlcoption.setAlignmentX(Component.LEFT_ALIGNMENT);
		sessionoption.setAlignmentX(Component.LEFT_ALIGNMENT);
		dlcbypassinformation.setAlignmentX(Component.LEFT_ALIGNMENT);
		customdlcconflictsoption.setAlignmentX(Component.LEFT_ALIGNMENT);
		me3logfile.setAlignmentX(Component.LEFT_ALIGNMENT);
		filetreeoption.setAlignmentX(Component.LEFT_ALIGNMENT);
		fname.setAlignmentX(Component.LEFT_ALIGNMENT);
		fdescription.setAlignmentX(Component.LEFT_ALIGNMENT);
		infoLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
		fname.setAlignmentX(Component.LEFT_ALIGNMENT);
		shareViaFile.setAlignmentX(Component.LEFT_ALIGNMENT);
		shareViaPastebin.setAlignmentX(Component.LEFT_ALIGNMENT);

		optionsPanel.add(infoLabel);
		optionsPanel.add(sessionoption);
		optionsPanel.add(installeddlcoption);
		optionsPanel.add(dlcbypassinformation);
		optionsPanel.add(new JSeparator(SwingConstants.HORIZONTAL));
		optionsPanel.add(customdlcconflictsoption);
		optionsPanel.add(me3logfile);
		optionsPanel.add(filetreeoption);
		optionsPanel.add(fname);
		optionsPanel.add(fdescription);

		JLabel privacyLabel = new JLabel(
				"<html><div style='width: 300px'>The Mod Manager log will contain minor identifying information such as your current logon username, directory paths and system environment variables. PasteBin posts are unlisted and are *never* shared to third parties by ME3Tweaks.</div></html>");
		privacyLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
		optionsPanel.add(privacyLabel);

		JPanel actionPanel = new JPanel(new BorderLayout());
		actionPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		actionPanel.add(shareViaPastebin, BorderLayout.WEST);
		actionPanel.add(shareViaFile, BorderLayout.EAST);
		optionsPanel.add(actionPanel);

		optionsPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		this.getContentPane().add(optionsPanel);
		this.pack();
	}

	protected String saveLogToDisk(String fname, boolean showWinExp) {
		String log = generateLog();
		try {
			File logfile = ModManager.getNewLogFile(fname);
			FileUtils.writeStringToFile(logfile, log);
			//dispose();
			if (showWinExp) {
				Process p = new ProcessBuilder("explorer.exe", "/select,", logfile.getAbsolutePath()).start();
			}
			ModManagerWindow.ACTIVE_WINDOW.labelStatus.setText("Log file written to disk");
			return logfile.getAbsolutePath();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			ModManager.debugLogger.writeErrorWithException("Failed to write log file to disk:", e1);
			ModManagerWindow.ACTIVE_WINDOW.labelStatus.setText("Failed to write log file to disk, view live log to see why");
		}
		return null;
	}

	private String generateLog() {
		String desc = "User did not enter a description of this log.";
		if (!fdescription.getText().equals("")) {
			desc = fdescription.getText();
		}
		String log = desc + "\n\n";

		ArrayList<String> acceptableHashes = new ArrayList<String>();
		acceptableHashes.add("1d09c01c94f01b305f8c25bb56ce9ab4"); //1.5
		acceptableHashes.add("598bf934e0f4d269f5b1657002f453ce"); //1.6

		if (installeddlcoption.isSelected()) {
			log += "=========[INSTALLEDDLC] Installed DLC =============\n";
			log += getInstalledDLC();
			File gamedir = new File(ModManagerWindow.GetBioGameDir()).getParentFile();

			File executable = new File(gamedir.toString() + "\\Binaries\\Win32\\MassEffect3.exe");
			if (executable.exists()) {
				int minorBuildNum = -1;
				try {
					minorBuildNum = EXEFileInfo.getMinorVersionOfProgram(executable.getAbsolutePath());
					log += "Game version is 1.0" + minorBuildNum + ". ";
					if (minorBuildNum < 5) {
						log += "Game version is below minimum supported version (1.0" + minorBuildNum + ").\n";
					} else if (minorBuildNum > 5) {
						log += "Game is likely from the UK as it's using the UK specific game version 1.06. User may consider downgrading to Mass Effect 3 1.05 via this URL if issues occur: https://me3tweaks.com/forums/viewtopic.php?f=5&t=20\n";
					} else {
						log += "This is the standard patch version for this game.\n";
					}
				} catch (Exception e) {
					ModManager.debugLogger.writeErrorWithException("Error checking game version:", e);
				}

				if (minorBuildNum == 5 || minorBuildNum == -1) {
					try {
						String hash = MD5Checksum.getMD5Checksum(executable.getAbsolutePath());
						if (acceptableHashes.contains(hash.toLowerCase())) {
							log += "EXE passed hash check. Hash of EXE is " + hash + "\n";
							ModManager.debugLogger.writeMessage("EXE has passed hash check: "+hash);
						} else {
							log += "EXE did not pass hash check. Hash of EXE is " + hash + "\n";
							ModManager.debugLogger.writeError("EXE has failed hash check: "+hash);
						}
					} catch (Exception e) {
						log += "Error checking game executable. Skipping check.\n";
						ModManager.debugLogger.writeErrorWithException("EXE was unable to be checked due to some error:",e);
					}
				}
				log += "\n";
			}
		}

		if (me3logfile.isSelected()) {
			log += "=========[ME3LOGGER] ME3Logger Contents =============\n";
			if (ModManagerWindow.validateBIOGameDir()) {
				File gamedir = new File(ModManagerWindow.GetBioGameDir()).getParentFile();
				File logfile = new File(gamedir.getAbsolutePath() + File.separator + "binaries" + File.separator + "win32" + File.separator + "ME3Log.txt");
				if (logfile.exists()) {
					SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
					String lastmodified = sdf.format(logfile.lastModified());
					log += "The following ME3Logger contents were in a file that has a modification date of " + lastmodified + ".\n\n";
					try {
						log += FileUtils.readFileToString(logfile, Charset.defaultCharset()); //MAY CAUSE ISSUES...
						log += "\n";
					} catch (IOException e) {
						log += "Error reading ME3Logger file. This shouldn't happen though...\n";
					}

				} else {
					log += "No ME3Logger file was found.\n";
				}

			} else {
				log += "Invalid BIOGame Directory: " + ModManagerWindow.GetBioGameDir() + "\n";
			}
			log += "\n";
		}

		if (dlcbypassinformation.isSelected()) {
			log += "=========[DLCBYPASS] DLC Bypass Information =============\n";
			if (ModManagerWindow.validateBIOGameDir()) {
				File gamedir = new File(ModManagerWindow.GetBioGameDir()).getParentFile();
				File Launcher_WV = new File(gamedir.toString() + "\\Binaries\\Win32\\Launcher_WV.exe");
				File LauncherWV = new File(gamedir.toString() + "\\Binaries\\Win32\\LauncherWV.exe");
				if (ModManager.checkIfASIBinkBypassIsInstalled(ModManagerWindow.GetBioGameDir())) {
					log += "Binkw32.dll Bypass is installed (ASI Version)\n";
				} else if (ModManager.checkIfBinkBypassIsInstalled(ModManagerWindow.GetBioGameDir())) {
					log += "Binkw32.dll Bypass is installed (binkw32.dll is modified and binkw23.dll exists, NOT ASI VERSION)\n";
				} else if (Launcher_WV.exists() || LauncherWV.exists()) {
					log += "Launcher WV Bypass is installed\n";
				} else {
					log += "No DLC bypass is installed. Modified and Custom DLC will not load.\n";
				}
			} else {
				log += "Invalid BIOGame Directory: " + ModManagerWindow.GetBioGameDir() + "\n";
			}
			log += "\n";
		}

		if (customdlcconflictsoption.isSelected()) {
			log += "=========[CUSTOMDLCCONFLICTS] Custom DLC Conflicts =============\n"
					+ "The following files have superceding conflicts. The files are listed in order of mount priority, highest to lowest. The first listed DLC will contain the file the game will use.\nOnly DLCs with valid mount.dlc files and folders that start with DLC_ are listed.\nGUI compatibilty mods generated by Mod Manager have a priority of 6000 and inject their interfaces into the next highest priority version of the file.\n\n";
			log += getDLCConflicts();
			log += "\n";
		}

		if (filetreeoption.isSelected()) {
			log += "=========[GAMEDIRECTORYTREE] Game Directory Tree =============\n";
			log += getPrettyFiletree();
			log += "\n";
		}

		if (sessionoption.isSelected()) {
			log += "=========[MMSESSION] Mod Manager Session Log=============\n";
			log += ModManager.debugLogger.getLog();
			log += "\n";
		}

		return log;
	}

	private String getDLCConflicts() {
		String conflictstr = "";
		ModManager.debugLogger.writeMessage("Getting CustomDLC conflicts information...");
		if (ModManagerWindow.validateBIOGameDir()) {
			ArrayList<String> installedDLCs = ModManager.getInstalledDLC(ModManagerWindow.GetBioGameDir());
			ArrayList<CustomDLC> customDLCs = new ArrayList<CustomDLC>();
			for (String dlc : installedDLCs) {
				File mountFile = new File(ModManager.appendSlash(ModManagerWindow.GetBioGameDir()) + "DLC/" + dlc + File.separator + "CookedPCConsole/Mount.dlc");
				if (!ModType.isKnownDLCFolder(dlc) && dlc.startsWith("DLC_") && mountFile.exists()) {
					customDLCs.add(new CustomDLC(new MountFile(mountFile.getAbsolutePath()), dlc));
				}
			}

			HashMap<String, ArrayList<CustomDLC>> conflicts = ModManager.getCustomDLCConflicts(customDLCs, ModManager.appendSlash(ModManagerWindow.GetBioGameDir()) + "DLC/");
			HashMap<String, ArrayList<CustomDLC>> mountpriorityconflicts = new HashMap<String, ArrayList<CustomDLC>>();
			for (Map.Entry<String, ArrayList<CustomDLC>> entry : conflicts.entrySet()) {
				if (entry.getValue().size() <= 1) {
					continue; //not priority conflict
				}
				mountpriorityconflicts.put(entry.getKey(), entry.getValue());
			}

			for (Map.Entry<String, ArrayList<CustomDLC>> entry : mountpriorityconflicts.entrySet()) {
				String filename = entry.getKey();
				ArrayList<CustomDLC> value = entry.getValue();

				conflictstr += filename + "\n";
				for (int x = value.size() - 1; x >= 0; x--) {
					conflictstr += " - " + value.get(x).getDlcName() + " (Priority " + value.get(x).getMountFile().getMountPriority() + ")\n";
				}
				conflictstr += "\n";
			}

			if (conflictstr.equals("")) {
				return "No conflicting Custom DLC files were found.\n";
			}
			return conflictstr + "\n";
		} else {
			return "Invalid BIOGame Directory: " + ModManagerWindow.GetBioGameDir() + "\n";
		}
	}

	private String getPrettyFiletree() {
		ModManager.debugLogger.writeMessage("Getting pretty game tree...");
		if (ModManagerWindow.validateBIOGameDir()) {
			File gamedir = new File(ModManagerWindow.GetBioGameDir()).getParentFile();
			return printDirectoryTree(gamedir) + "\n";
		} else {
			return "Invalid BIOGame Directory: " + ModManagerWindow.GetBioGameDir() + "\n";
		}
	}

	private String getInstalledDLC() {
		ModManager.debugLogger.writeMessage("Getting list of installed DLC...");
		String installeddlcstr = "";
		if (ModManagerWindow.validateBIOGameDir()) {
			File mainDlcDir = new File(ModManager.appendSlash(ModManagerWindow.GetBioGameDir()) + "DLC" + File.separator);
			File testpatch = new File(ModManager.appendSlash(ModManagerWindow.GetBioGameDir()) + "Patches" + File.separator + "PCConsole" + File.separator + "Patch_001.sfar");

			String[] directories = mainDlcDir.list(new FilenameFilter() {
				@Override
				public boolean accept(File current, String name) {
					return new File(current, name).isDirectory();
				}
			});
			installeddlcstr += "Installed DLC:\n";
			for (String dir : directories) {
				//add to list
				File metacmm = new File(mainDlcDir + File.separator + dir + File.separator + "_metacmm.txt");
				if (dir.startsWith("DLC_")) {
					if (ModType.isKnownDLCFolder(dir)) {
						installeddlcstr += dir + " (Offical BioWare DLC)\n";
						continue;
					}
					if (metacmm.exists()) {
						String metaname = "";
						try {
							metaname = FileUtils.readFileToString(metacmm, Charset.defaultCharset());
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
							metaname = "[Can't read metacmm.txt]";
						}
						installeddlcstr += dir + " (" + metaname + ", installed by Mod Manager)\n";
					} else {
						ThirdPartyModInfo tpmi = ME3TweaksUtils.getThirdPartyModInfo(dir);
						if (tpmi != null) {
							installeddlcstr += dir + " (" + tpmi.getModname() + ")";
							installeddlcstr += "\n";
						} else {
							installeddlcstr += dir + " (Unknown Mod)";
							installeddlcstr += "\n";
						}
					}
				}
			}
			if (testpatch.exists()) {
				installeddlcstr += "Patch_001.sfar (TESTPATCH) is installed.\n";
			} else {
				installeddlcstr += "Patch_001.sfar (TESTPATCH) is not installed. This is usually missing if the game is not up to date or is a cracked version\n";
			}
		} else {
			return "Invalid BIOGame Directory: " + ModManagerWindow.GetBioGameDir() + "\n";
		}
		return installeddlcstr;
	}

	public String printDirectoryTree(File folder) {
		if (!folder.isDirectory()) {
			throw new IllegalArgumentException("folder is not a Directory");
		}
		int indent = 0;
		StringBuilder sb = new StringBuilder();
		printDirectoryTree(folder, indent, sb);
		return sb.toString();
	}

	private void printDirectoryTree(File folder, int indent, StringBuilder sb) {
		if (!folder.isDirectory()) {
			throw new IllegalArgumentException("folder is not a Directory");
		}
		sb.append(getIndentString(indent));
		sb.append("+--");
		sb.append(folder.getName());
		sb.append("/");
		sb.append("\n");
		for (File file : folder.listFiles()) {
			if (file.isDirectory()) {
				printDirectoryTree(file, indent + 1, sb);
			} else {
				printFile(file, indent + 1, sb);
			}
		}

	}

	private void printFile(File file, int indent, StringBuilder sb) {
		sb.append(getIndentString(indent));
		sb.append("+--");
		sb.append(file.getName());
		sb.append("\n");
	}

	private static String getIndentString(int indent) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < indent; i++) {
			sb.append("|  ");
		}
		return sb.toString();
	}

	private class PasteBinUploaderDialog extends JDialog {

		public PasteBinUploaderDialog(String log, String fname, LogOptionsWindow low) {
			setupAutomatedWindow(low);
			new PastebinUploaderThread(log, fname, low).execute();
			setVisible(true);
		}

		private void setupAutomatedWindow(JDialog callingDialog) {
			this.setTitle("Uploading log");
			this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
			this.setResizable(false);
			this.setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
			this.setIconImages(ModManager.ICONS);
			JPanel panel = new JPanel(new BorderLayout());

			JLabel operationLabel = new JLabel("Uploading log to PasteBin...");

			panel.add(operationLabel, BorderLayout.CENTER);
			panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
			this.getContentPane().add(panel);
			pack();
			this.setLocationRelativeTo(callingDialog);
		}

		class PastebinUploaderThread extends SwingWorker<Boolean, Void> {
			private String log, fname;
			private LogOptionsWindow low;
			private String pastebinlink;

			public PastebinUploaderThread(String log, String fname, LogOptionsWindow low) {
				this.log = log;
				this.low = low;
				this.fname = fname;
			}

			public Boolean doInBackground() {
				boolean OK = false;
				try {
					//compress with lzma
					String logfile = saveLogToDisk(fname, false);
					String outputFile = logfile + ".lzma";
					String[] procargs = { ModManager.getToolsDir() + "lzma.exe", "e", logfile, outputFile, "-d26", "-mt" + Runtime.getRuntime().availableProcessors() };
					ProcessBuilder p = new ProcessBuilder(procargs);
					ModManager.runProcess(p);
					String log = Base64.getEncoder().encodeToString(FileUtils.readFileToByteArray(new File(outputFile)));
					HttpClient httpclient = HttpClients.createDefault();
					HttpPost httppost = new HttpPost("https://me3tweaks.com/modmanager/tools/loguploader-compress");
					System.out.println(log);
					// Request parameters and other properties.
					List<NameValuePair> params = new ArrayList<NameValuePair>(2);
					params.add(new BasicNameValuePair("MMVersion", ModManager.VERSION));
					params.add(new BasicNameValuePair("MMBuild", Long.toString(ModManager.BUILD_NUMBER)));
					params.add(new BasicNameValuePair("LogData", log));

					httppost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
					//Execute and get the response.
					HttpResponse response = httpclient.execute(httppost);
					HttpEntity entity = response.getEntity();
					new File(outputFile).delete();
					if (entity != null) {
						InputStream instream = entity.getContent();
						try {
							// do something useful
							pastebinlink = IOUtils.toString(instream);
							OK = ModManager.openWebpage(new URL(pastebinlink));
							if (OK) {
								Clipboard clpbrd = Toolkit.getDefaultToolkit().getSystemClipboard();
								ModManager.debugLogger.writeMessage("Server responded with " + pastebinlink);
								StringSelection stringSelection = new StringSelection(pastebinlink);
								clpbrd.setContents(stringSelection, null);
							}

						} catch (MalformedURLException e) {
							OK = false;
							ModManager.debugLogger.writeError("Malformed URL from server, likely an error...");
							ModManager.debugLogger.writeError("Response was " + pastebinlink);
						} finally {
							instream.close();
						}
					}
				} catch (UnsupportedEncodingException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (ClientProtocolException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (IOException e1) {
					ModManager.debugLogger.writeErrorWithException("Error uploading log: ", e1);
				}
				return OK;
			}

			public void done() {
				try {
					boolean ok = get();
					if (ok) {
						ModManagerWindow.ACTIVE_WINDOW.labelStatus.setText("Uploaded log to pastebin, link copied to clipboard");
						low.dispose();
					} else {
						shareViaPastebin.setEnabled(true);
						shareViaFile.setEnabled(true);
						ModManager.debugLogger.writeError("Pastebin thread NOT OK!");
						if (pastebinlink != null && pastebinlink.equals("")) {
							JOptionPane.showMessageDialog(LogOptionsWindow.this,
									"An error occured uploading the log to the server.\nThe server responded, but there was nothing in the message. You should contact femshep as this shouldn't happen.\n\nYou can use the save to disk option and upload that as a backup for log sharing.",
									"Upload Error", JOptionPane.ERROR_MESSAGE);
						} else {
							JOptionPane
									.showMessageDialog(LogOptionsWindow.this,
											"An error occured uploading the log to the server.\nThe server responded with the following:\n" + pastebinlink
													+ "\n\nYou can use the save to disk option and upload that as a backup for log sharing.",
											"Upload Error", JOptionPane.ERROR_MESSAGE);
						}
					}
				} catch (ExecutionException e) {
					ModManager.debugLogger.writeErrorWithException("Execution exception in pastebin thread: ", e);
					JOptionPane.showMessageDialog(LogOptionsWindow.this, "An error occured uploading the log to the server:\n" + e.getMessage()
							+ "\n\nYou can use the save to disk option and upload that as a backup for log sharing.", "Upload Error", JOptionPane.ERROR_MESSAGE);
				} catch (InterruptedException e) {
					ModManager.debugLogger.writeErrorWithException("Execution exception in pastebin thread: ", e);
				}
				dispose();
			}
		}
	}
}
