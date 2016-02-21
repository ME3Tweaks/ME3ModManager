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
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
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
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;

import com.me3tweaks.modmanager.objects.Mod;
import com.me3tweaks.modmanager.objects.ModJob;
import com.me3tweaks.modmanager.objects.ModType;
import com.me3tweaks.modmanager.objects.MountFile;
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

	public ImportEntryWindow(JDialog modImportWindow, String importPath) {
		this.importPath = importPath;
		setupWindow(modImportWindow);
		setVisible(true);
	}

	public int getResult() {
		return result;
	}

	private void setupWindow(JDialog callingDialog) {
		setIconImages(ModManager.ICONS);
		dlcModName = FilenameUtils.getBaseName(importPath);
		setTitle("Importing " + dlcModName);
		setMinimumSize(new Dimension(275, 350));
		setModalityType(Dialog.ModalityType.DOCUMENT_MODAL);

		JPanel panel = new JPanel(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		JLabel importHeader = new JLabel("<html><div style='text-align: center;'>Importing DLC Mod<br>" + dlcModName + "</div></html>", SwingConstants.CENTER);
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
		JPanel descPanel = new JPanel(new BorderLayout());
		descPanel.setBorder(new TitledBorder(new EtchedBorder(), "Mod Description"));
		descPanel.add(modDescField);
		panel.add(descPanel, c);

		telemetryCheckbox = new JCheckBox("Send this info to ME3Tweaks");
		telemetryCheckbox.setSelected(true);
		telemetryCheckbox.setToolTipText(
				"<html><div style='width: 250px'>Sending this information to ME3Tweaks helps build a database of Non-Mod Manager mods that will automatically fill this information out when another user is importing it.<br>Nothing except the above information and the Mount.dlc mount priority value is submitted.</div></html>");
		importButton = new JButton("Import into Mod Manager");
		importButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				if (inputValidate()) {
					ModManager.debugLogger.writeMessage("Importing mod: " + dlcModName + " as " + modNameField.getText());
					importButton.setEnabled(false);
					importButton.setText("Importing...");
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

		panel.setBorder(new EmptyBorder(5, 5, 5, 5));

		add(panel);
		pack();
		setLocationRelativeTo(callingDialog);
	}

	class ImportWorker extends SwingWorker<Void, Void> {
		private String modDesc;
		private String modAuthor;
		private String modSite;
		private String modName;
		private boolean sendTelemetry;

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

			if (sendTelemetry) {
				try {
					Collection<File> allfiles = FileUtils.listFiles(importF, TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE);
					File mountFile = null;
					for (File f : allfiles) {
						if (f.getName().equalsIgnoreCase("mount.dlc")) {
							mountFile = f;
							ModManager.debugLogger.writeMessage("Telemetry using mount file: " + mountFile.getAbsolutePath());
							break;
						}
					}

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
					//UrlEncodedFormEntity uri = new UrlEncodedFormEntity(params, "UTF-8");
					//httppost.setParams(params);
					URIBuilder urib = new URIBuilder("https://me3tweaks.com/mods/dlc_mods/telemetry");
					urib.setParameters(params);
					HttpClient httpClient = HttpClientBuilder.create().build();
					URI uri = urib.build();
					System.out.println("Sending telemetry via GET: "+uri.toString());
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
			ModManager.debugLogger.writeMessage("Copying DLC folder: " + importF + " to " + exportF);
			FileUtils.copyDirectory(importF, exportF);

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
			FileUtils.writeStringToFile(new File(localModPathFile + File.separator + "moddesc.ini"), desc);
			return null;
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
		}
	}

	public boolean inputValidate() {
		try {
			File.createTempFile(modNameField.getText(), "tmp");
		} catch (IOException e) {
			//illegal characters in name likely
			JOptionPane.showMessageDialog(this, "Illegal characters in the mod name.\nThis OS cannot make a folder with the mod name you specified, please change it.",
					"Illegal characters in name", JOptionPane.ERROR_MESSAGE);
			return false;
		} catch (Exception e) {
			JOptionPane.showMessageDialog(this, "This OS cannot make a folder with the mod name you specified, please change it.",
					"Could not create mod folder", JOptionPane.ERROR_MESSAGE);
			return false;
		}
		return true;
	}
}