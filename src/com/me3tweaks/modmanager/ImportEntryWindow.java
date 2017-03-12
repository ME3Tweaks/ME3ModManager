package com.me3tweaks.modmanager;

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;

import com.me3tweaks.modmanager.modmaker.ME3TweaksUtils;
import com.me3tweaks.modmanager.objects.Mod;
import com.me3tweaks.modmanager.objects.ModJob;
import com.me3tweaks.modmanager.objects.ModType;
import com.me3tweaks.modmanager.objects.MountFile;
import com.me3tweaks.modmanager.objects.ThirdPartyModInfo;
import com.me3tweaks.modmanager.objects.ThreadCommand;
import com.me3tweaks.modmanager.ui.HintTextFieldUI;
import com.me3tweaks.modmanager.utilities.ResourceUtils;

public class ImportEntryWindow extends JDialog {

	public static final int NO_ANSWER = 0;
	public static final int OK = 1;
	public static final int CANCELED = 2;
	public static final int ERROR = 3;

	private String importPath;
	private JTextField modNameField, modSiteField, modAuthorField;
	private JTextArea modDescField;
	private JButton importButton;
	private JCheckBox telemetryCheckbox;
	private String dlcModName;
	private int result = NO_ANSWER;
	private JDialog callingWindow;
	private String importDisplayStr;
	private JProgressBar progressBar;

	public ImportEntryWindow(JDialog modImportWindow, String diplayname, String importPath) {
		ModManager.debugLogger.writeMessage("Opening Mod Import Window (Data Entry)");
		this.importDisplayStr = diplayname;
		this.importPath = importPath;
		if (importPath.endsWith(File.separator)) {
			importPath = importPath.substring(0, importPath.length() - 2);
		}
		this.callingWindow = modImportWindow;
		setupWindow(modImportWindow);
		setVisible(true);
	}

	public int getResult() {
		return result;
	}

	private void setupWindow(JDialog callingDialog) {
		setIconImages(ModManager.ICONS);
		dlcModName = FilenameUtils.getBaseName(importPath);
		if (dlcModName.equals("")) {
			ModManager.debugLogger.writeError("Importing Mod Name is empty! Importing will fail.");
			return;
		}
		setTitle("Importing " + dlcModName);
		setMinimumSize(new Dimension(480, 440));
		setModalityType(Dialog.ModalityType.DOCUMENT_MODAL);

		JPanel panel = new JPanel(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		JLabel importHeader = new JLabel("<html><div style='text-align: center;'>Importing DLC Mod<br>" + importDisplayStr + "</div></html>", SwingConstants.CENTER);
		int row = 0;
		c.gridx = 0;
		c.gridy = row;
		c.weighty = 0;
		c.weightx = 1;
		c.gridwidth = 2;
		c.fill = GridBagConstraints.HORIZONTAL;
		panel.add(importHeader, c);

		//ROW 1
		c.gridy = ++row;

		modNameField = new JTextField();
		modNameField.setUI(new HintTextFieldUI("Enter a mod name"));
		JPanel namePanel = new JPanel(new BorderLayout());
		namePanel.setBorder(new TitledBorder(new EtchedBorder(), "Mod Name"));
		namePanel.add(modNameField);
		panel.add(namePanel, c);

		//ROW 2
		c.gridx = 0;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridy = ++row;

		modAuthorField = new JTextField();
		modAuthorField.setUI(new HintTextFieldUI("Enter mod author"));
		JPanel authorPanel = new JPanel(new BorderLayout());
		authorPanel.setBorder(new TitledBorder(new EtchedBorder(), "Mod Author"));
		authorPanel.add(modAuthorField);
		panel.add(authorPanel, c);

		//ROW 3
		c.gridx = 0;
		c.gridwidth = 2;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridy = ++row;

		modSiteField = new JTextField();
		modSiteField.setUI(new HintTextFieldUI("Enter mod's website"));
		JPanel webPanel = new JPanel(new BorderLayout());
		webPanel.setBorder(new TitledBorder(new EtchedBorder(), "Mod Web Site"));
		webPanel.add(modSiteField);
		panel.add(webPanel, c);

		//ROW 5
		c.weighty = 1;
		c.weightx = 1;
		c.gridwidth = 2;
		c.gridy = ++row;
		c.gridx = 0;
		c.fill = GridBagConstraints.BOTH;
		modDescField = new JTextArea();
		modDescField.setLineWrap(true);
		modDescField.setWrapStyleWord(true);
		JScrollPane modDescFieldScroller = new JScrollPane(modDescField, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		JPanel descPanel = new JPanel(new BorderLayout());
		descPanel.setBorder(new TitledBorder(new EtchedBorder(), "Mod Description"));
		descPanel.add(modDescFieldScroller);
		panel.add(descPanel, c);

		telemetryCheckbox = new JCheckBox("Send this info to ME3Tweaks");
		telemetryCheckbox.setSelected(true);
		telemetryCheckbox.setToolTipText(
				"<html><div style='width: 250px'>Sending this information to ME3Tweaks helps build a database of Non-Mod Manager mods that will automatically fill this information out in the Mod Manager interface for users.<br>Nothing except the above information and the Mount.dlc mount information is submitted.</div></html>");
		importButton = new JButton("Import into Mod Manager");
		importButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				if (inputValidate()) {
					ModManager.debugLogger.writeMessage("Importing mod: " + dlcModName + " as " + modNameField.getText());
					importButton.setEnabled(false);
					importButton.setText("Importing...");
					progressBar.setVisible(true);
					modAuthorField.setEnabled(false);
					modNameField.setEnabled(false);
					modDescField.setEnabled(false);
					modSiteField.setEnabled(false);
					telemetryCheckbox.setEnabled(false);
					new ImportWorker().execute();
				}
			}
		});
		c.weighty = 0;
		c.gridx = 0;
		c.gridwidth = 2;
		c.gridy = ++row;
		panel.add(telemetryCheckbox, c);

		c.gridy = ++row;
		panel.add(importButton, c);
		c.gridy = ++row;
		progressBar = new JProgressBar();
		panel.add(progressBar, c);

		panel.setBorder(new EmptyBorder(5, 5, 5, 5));

		//check if we already got this info via 3rdparty telemetry
		ThirdPartyModInfo tpmi = ME3TweaksUtils.getThirdPartyModInfo(new File(importPath).getName());
		if (tpmi != null) {
			modNameField.setText(tpmi.getModname());
			modAuthorField.setText(tpmi.getModauthor());
			modSiteField.setText(tpmi.getModsite());
			modDescField.setText(tpmi.getModdescription());
			setTitle("Importing " + tpmi.getModname());
			//Check if an update telemetry is required
			MountFile mf = new MountFile(ModManager.appendSlash(importPath) + "CookedPCConsole" + File.separator + "Mount.dlc");
			if (mf.getMountPriority() != tpmi.getMountPriority()) {
				//TELEMETRY UPDATE
				telemetryCheckbox.setText("Send updated mod info to ME3Tweaks");
				telemetryCheckbox.setToolTipText(
						"<html>ME3Tweaks Mod Identification Service has outdated information about this mod.<br>Please consider sending it in so we can update our database of mods.</html>");
			} else {
				//SAME DATA

				telemetryCheckbox.setEnabled(false);
				telemetryCheckbox.setSelected(false);
				telemetryCheckbox.setText("Info for this mod already on ME3Tweaks");
				telemetryCheckbox
						.setToolTipText("<html>The ME3Tweaks Mod Identification Service has identified this mod and has prefilled information for you from our database.</html>");

			}
		}

		add(panel);
		pack();
		setLocationRelativeTo(callingDialog);
	}

	class ImportWorker extends SwingWorker<Void, ThreadCommand> {
		private String modDesc;
		private String modAuthor;
		private String modSite;
		private String modName;
		private boolean sendTelemetry;
		double numFiles = 0;
		int numCompletedFiles = 0;

		protected ImportWorker() {
			//calculate number of files
			modName = modNameField.getText();
			modDesc = ResourceUtils.convertNewlineToBr(modDescField.getText());
			modSite = modSiteField.getText();
			modAuthor = modAuthorField.getText();
			sendTelemetry = telemetryCheckbox.isSelected();
		}

		@Override
		protected Void doInBackground() throws Exception {
			File importF = new File(importPath);
			numFiles = 0;
			File mountFile = null;

			Collection<File> allfiles = FileUtils.listFiles(importF, TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE);
			numFiles = allfiles.size();
			for (File f : allfiles) {
				if (f.getName().equalsIgnoreCase("mount.dlc")) {
					mountFile = f;
					ModManager.debugLogger.writeMessage("Telemetry using mount file: " + mountFile.getAbsolutePath());
					break;
				}
			}
			if (sendTelemetry) {
				try {
					ModManager.debugLogger
							.writeMessage("Sending DLC mod telemetry to ME3Tweaks. This information will be used to help build a database of what DLC content mods exist.");

					// Request parameters and other properties.
					List<NameValuePair> params = new ArrayList<NameValuePair>(2);
					params.add(new BasicNameValuePair("dlc_folder_name", dlcModName));
					params.add(new BasicNameValuePair("mod_name", modName));
					params.add(new BasicNameValuePair("mod_author", modAuthor));
					params.add(new BasicNameValuePair("mod_site", modSite));
					if (mountFile != null) {
						MountFile mf = new MountFile(mountFile.getAbsolutePath());
						params.add(new BasicNameValuePair("mod_mount_priority", Integer.toString(mf.getMountPriority())));
						params.add(new BasicNameValuePair("mod_mount_tlk1", Integer.toString(mf.getTlkId1())));
						params.add(new BasicNameValuePair("mod_mount_flag", Byte.toString(mf.getMountFlag())));
					}

					progressBar.setIndeterminate(true);
					//UrlEncodedFormEntity uri = new UrlEncodedFormEntity(params, "UTF-8");
					//httppost.setParams(params);
					URIBuilder urib = new URIBuilder("https://me3tweaks.com/mods/dlc_mods/telemetry");
					urib.setParameters(params);
					HttpClient httpClient = HttpClientBuilder.create().build();
					URI uri = urib.build();
					System.out.println("Sending telemetry via GET: " + uri.toString());
					//Execute and get the response.
					HttpGet get = new HttpGet(uri);
					HttpResponse response = httpClient.execute(get);
					HttpEntity entity = response.getEntity();
				} catch (Exception e) {
					ModManager.debugLogger.writeErrorWithException("Error sending telemetry. Since this is optional we will ignore this error: ", e);
				}
			}

			String localModPath = ModManager.getModsDir() + modName;
			File localModPathFile = new File(localModPath);
			localModPathFile.mkdirs();
			ModManager.debugLogger.writeMessage("Importing mod: " + dlcModName + " to " + localModPathFile.getAbsolutePath());

			File exportF = new File(localModPathFile + File.separator + dlcModName);
			FileUtils.deleteQuietly(exportF);
			ModManager.debugLogger.writeMessage("Copying DLC folder: " + importF + " to " + exportF);
			//FileUtils.copyDirectory(importF, exportF);
			progressBar.setIndeterminate(false);

			publish(new ThreadCommand("PROGRESSBAR_DETERMINATE"));
			/*
			 * try (Stream<Path> stream = Files.walk(importF.toPath())) {
			 * stream.forEach(path -> {
			 * 
			 * try { Path destinationPath =
			 * Paths.get(path.toString().replace(importF.toString(),
			 * importF.toString())); Files.copy(path, destinationPath);
			 * publish(new ThreadCommand("SINGLE_FILE_COPIED")); } catch
			 * (Exception e) { ModManager.debugLogger.
			 * writeErrorWithException("EXCEPTION COPYING FILE " +
			 * path.toString(), e); } });
			 * 
			 * } catch (IOException e1) { // TODO Auto-generated catch block
			 * ModManager.debugLogger.
			 * writeErrorWithException("EXCEPTION IMPORTING MOD:", e1); }
			 */
			final Path targetPath = exportF.toPath(); // target
			final Path sourcePath = importF.toPath();// source
			Files.walkFileTree(sourcePath, new SimpleFileVisitor<Path>() {
				@Override
				public FileVisitResult preVisitDirectory(final Path dir, final BasicFileAttributes attrs) throws IOException {
					Files.createDirectories(targetPath.resolve(sourcePath.relativize(dir)));
					return FileVisitResult.CONTINUE;
				}

				@Override
				public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) throws IOException {
					ModManager.debugLogger.writeMessage("Importing file: " + targetPath.toString());
					Files.copy(file, targetPath.resolve(sourcePath.relativize(file)));
					publish(new ThreadCommand("SINGLE_FILE_COPIED"));
					return FileVisitResult.CONTINUE;
				}
			});

			//files copied, now make moddesc.ini
			Mod mod = new Mod();
			mod.setModName(modName);
			mod.setModDescription(modDesc);
			mod.setVersion(1.0); //might consider including this.
			mod.setAuthor(modAuthor);
			mod.setSite(modSite);

			ModJob dlcJob = new ModJob();
			dlcJob.setJobName(ModType.CUSTOMDLC); //backwards, it appears...
			dlcJob.setJobType(ModJob.CUSTOMDLC);
			dlcJob.setSourceFolders(new ArrayList<String>());
			dlcJob.setDestFolders(new ArrayList<String>());
			dlcJob.getSourceFolders().add(dlcModName);
			dlcJob.getDestFolders().add(dlcModName);
			mod.addTask(ModType.CUSTOMDLC, dlcJob);

			String desc = mod.createModDescIni(false, ModManager.MODDESC_VERSION_SUPPORT);
			File descFile = new File(localModPathFile + File.separator + "moddesc.ini");
			FileUtils.writeStringToFile(descFile, desc, StandardCharsets.UTF_8);

			//VERIFY
			mod = new Mod(descFile.getAbsolutePath());
			if (mod.isValidMod()) {
				ModManager.debugLogger.writeMessage("Mod imported OK.");
			} else {
				ModManager.debugLogger.writeMessage("Mod failed import.");
			}
			return null;
		}

		protected void process(List<ThreadCommand> commands) {
			for (ThreadCommand command : commands) {
				String commandStr = command.getCommand();
				switch (commandStr) {
				case "PROGRESSBAR_DETERMINATE":
					progressBar.setIndeterminate(false);
					break;
				case "PROGRESSBAR_INDETERMINATE":
					progressBar.setIndeterminate(true);
					break;
				case "SINGLE_FILE_COPIED":
					numCompletedFiles++;
					if (numFiles > 0) {
						int progress = (int) (numCompletedFiles * 100.0 / numFiles);
						progressBar.setValue(progress);
					}
					break;
				}
			}
		}

		@Override
		public void done() {
			try {
				get();
				result = OK;
				JOptionPane.showMessageDialog(ImportEntryWindow.this, modName + " has been imported into Mod Manager.", "Mod Imported", JOptionPane.INFORMATION_MESSAGE);
			} catch (Exception e) {
				result = ERROR;
				ModManager.debugLogger.writeErrorWithException("Exception importing DLC:", e);
				JOptionPane.showMessageDialog(ImportEntryWindow.this, "Error occured importing this mod:\n" + (e.getCause() != null ? e.getCause().getMessage() : e.getMessage()),
						"Import Error", JOptionPane.ERROR_MESSAGE);
			}
			ModManager.debugLogger.writeMessage("Import of mod complete. Result code: " + result);
			dispose();
			if (result == OK) {
				callingWindow.dispose();
				new ModManagerWindow(false);
			}
		}

	}

	/**
	 * Validates input into the Mod Import Window
	 * 
	 * @return
	 */
	public boolean inputValidate() {
		if (modNameField.getText().equals("")) {
			JOptionPane.showMessageDialog(this, "You must set a Mod Name.", "Mod Name Required", JOptionPane.ERROR_MESSAGE);
			return false;
		}
		try {
			File.createTempFile(modNameField.getText(), "tmp");
		} catch (IOException e) {
			//illegal characters in name likely
			JOptionPane.showMessageDialog(this, "Illegal characters in the mod name.\nThis OS cannot make a folder with the mod name you specified, please change it.",
					"Illegal characters in name", JOptionPane.ERROR_MESSAGE);
			return false;
		} catch (Exception e) {
			JOptionPane.showMessageDialog(this, "This OS cannot make a folder with the mod name you specified, please change it.", "Could not create mod folder",
					JOptionPane.ERROR_MESSAGE);
			return false;
		}
		return true;
	}
}