package com.me3tweaks.modmanager.modmaker;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;

import org.ini4j.InvalidFileFormatException;
import org.ini4j.Wini;

import com.me3tweaks.modmanager.ModManager;
import com.me3tweaks.modmanager.ModManagerWindow;
import com.me3tweaks.modmanager.utilities.ResourceUtils;

@SuppressWarnings("serial")
public class ModMakerEntryWindow extends JDialog implements ActionListener {
	private static final String ALL_LANG = "All languages";
	private static final String ENGLISH = "English";
	private static final String RUSSIAN = "Russian";
	private static final String SPANISH = "Spanish";
	private static final String POLISH = "Polish";
	private static final String FRENCH = "French";
	private static final String ITALIAN = "Italian";
	private static final String GERMAN = "German";
	private static final String JAPANESE = "Japanese";
	JLabel infoLabel;
	JButton downloadButton;
	JTextField codeField;
	String biogameDir;
	private JComboBox<String> languageChoices;
	private String[] languages = { ALL_LANG, ENGLISH, RUSSIAN, SPANISH, POLISH, FRENCH, ITALIAN, GERMAN, JAPANESE };
	boolean hasDLCBypass = false;
	private JButton makeModButton;
	private JButton browseModsButton;
	private JButton sideloadButton;

	public ModMakerEntryWindow() {
		this.biogameDir = ModManagerWindow.GetBioGameDir();
		this.setTitle("ME3Tweaks ModMaker");
		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);

		boolean shouldshow = validateModMakerPrereqs();
		if (shouldshow) {
			setupWindow();
			setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
			setVisible(true);
		} else {
			dispose();
		}

	}

	private void setupWindow() {
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		setResizable(false);
		setIconImages(ModManager.ICONS);
		
		JPanel modMakerPanel = new JPanel();
		modMakerPanel.setLayout(new BoxLayout(modMakerPanel, BoxLayout.Y_AXIS));
		JPanel infoPane = new JPanel();
		infoPane.setLayout(new BoxLayout(infoPane, BoxLayout.LINE_AXIS));
		BufferedImage modmakerLogo;
		JLabel modmakerLogoLabel = null;
		try {
			modmakerLogo = ImageIO.read(ModMakerEntryWindow.class.getResourceAsStream("/resource/modmaker.png"));
			modmakerLogo = ResourceUtils.getScaledInstance(modmakerLogo, 290, 109, RenderingHints.VALUE_INTERPOLATION_BILINEAR, true);
			modmakerLogoLabel = new JLabel(new ImageIcon(modmakerLogo));
			modmakerLogoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
		} catch (Exception e1) {
			ModManager.debugLogger.writeErrorWithException("Unable to load ModMaker logo image from jar:", e1);
		}
		infoLabel = new JLabel(
				"<html>ME3Tweaks ModMaker is an online mod creation tool that works with Mod Manager.<br>Enter a mod code below to download and compile a mod.</html>");
		infoPane.add(Box.createHorizontalGlue());
		infoPane.add(infoLabel);
		infoPane.add(Box.createHorizontalGlue());
		if (modmakerLogoLabel != null) {
			modMakerPanel.add(modmakerLogoLabel);
		}
		modMakerPanel.add(infoPane);

		JPanel languageChoicesPanel = new JPanel();
		languageChoicesPanel.setLayout(new BoxLayout(languageChoicesPanel, BoxLayout.LINE_AXIS));
		languageChoicesPanel.setMaximumSize(new Dimension(550, 30));
		TitledBorder languageBorder = BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), "Languages to compile");
		languageChoicesPanel.setBorder(languageBorder);
		languageChoices = new JComboBox<String>(languages);
		languageChoices.setToolTipText("Languages to compile. Choose your game's language to speed up compiling significantly.");
		languageChoicesPanel.add(languageChoices);
		modMakerPanel.add(languageChoicesPanel);

		//download panel
		JPanel codeDownloadPanel = new JPanel();
		codeDownloadPanel.setLayout(new BoxLayout(codeDownloadPanel, BoxLayout.LINE_AXIS));
		codeDownloadPanel.add(Box.createHorizontalGlue());

		codeField = new JTextField(6);
		//validation
		((AbstractDocument) codeField.getDocument()).setDocumentFilter(new DocumentFilter() {
			Pattern pattern = Pattern.compile("-{0,1}\\d+");

			@Override
			public void replace(FilterBypass arg0, int arg1, int arg2, String arg3, AttributeSet arg4) throws BadLocationException {
				String text = arg0.getDocument().getText(0, arg0.getDocument().getLength()) + arg3;
				Matcher matcher = pattern.matcher(text);
				if (!matcher.matches()) {
					return;
				}
				if (text.length() > 7) {
					return;
				}
				super.replace(arg0, arg1, arg2, arg3, arg4);
			}
		});
		codeField.setMaximumSize(new Dimension(60, 20));
		codeField.setToolTipText("ME3Tweaks ModMaker mod code to download. You can get these from ME3Tweaks.com/modmaker.");
		codeDownloadPanel.add(codeField);
		codeDownloadPanel.add(Box.createRigidArea(new Dimension(10, 10)));

		downloadButton = new JButton("Download & Compile");
		downloadButton.setPreferredSize(new Dimension(185, 22));
		downloadButton.setToolTipText("Download the mod with the specified ID from ME3Tweaks ModMaker and compile for installation");
		//codeDownloadPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		codeDownloadPanel.add(downloadButton);
		codeDownloadPanel.add(Box.createHorizontalGlue());
		modMakerPanel.add(codeDownloadPanel);
		modMakerPanel.add(Box.createVerticalGlue());

		JPanel getCodePane = new JPanel();
		getCodePane.setLayout(new BoxLayout(getCodePane, BoxLayout.LINE_AXIS));
		getCodePane.add(Box.createHorizontalGlue());

		makeModButton = new JButton("Create a mod");
		makeModButton.setToolTipText("Make a mod on ME3Tweaks Modmaker");

		browseModsButton = new JButton("Browse mods");
		browseModsButton.setToolTipText("View published mods you can download");

		sideloadButton = new JButton("Sideload mod");
		sideloadButton.setToolTipText("Compile a mod from a ME3Tweaks Modmaker XML file");

		sideloadButton.addActionListener(this);
		makeModButton.addActionListener(this);
		browseModsButton.addActionListener(this);
		getCodePane.add(makeModButton);
		getCodePane.add(Box.createRigidArea(new Dimension(10, 5)));
		getCodePane.add(browseModsButton);
		getCodePane.add(Box.createRigidArea(new Dimension(10, 5)));
		getCodePane.add(sideloadButton);
		getCodePane.add(Box.createHorizontalGlue());
		modMakerPanel.add(Box.createRigidArea(new Dimension(10, 5)));
		modMakerPanel.add(getCodePane);
		modMakerPanel.add(Box.createVerticalGlue());
		if (!hasDLCBypass) {
			JPanel launcherWVPanel = new JPanel();
			launcherWVPanel.setLayout(new BoxLayout(launcherWVPanel, BoxLayout.LINE_AXIS));
			launcherWVPanel.add(Box.createHorizontalGlue());
			launcherWVPanel.add(
					new JLabel(
							"<html>The Launcher_WV.exe DLC bypass will be installed so your mod will work.<br>To use mods you will need to use Start Game from Mod Manager.<br>Tab and ` will open the console in game.<br>Your game will not be modified by this file.</html>"),
					BorderLayout.CENTER);
			launcherWVPanel.add(Box.createHorizontalGlue());
			modMakerPanel.add(launcherWVPanel);
			//setPreferredSize(new Dimension(DIALOG_WIDTH, DIALOG_HEIGHT + 90));
		} else {
			//setPreferredSize(new Dimension(DIALOG_WIDTH, DIALOG_HEIGHT));
		}
		codeField.addActionListener(this);
		downloadButton.addActionListener(this);

		//set focus to codeField
		addWindowListener(new WindowAdapter() {
			public void windowOpened(WindowEvent e) {
				codeField.requestFocus();
			}
		});

		modMakerPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		add(modMakerPanel);
		
		pack();
		setLocationRelativeTo(ModManagerWindow.ACTIVE_WINDOW);

		//set combobox from settings
		Wini settingsini = ModManager.LoadSettingsINI();
		String modmakerLanguage = settingsini.get("Settings", "modmaker_language");
		if (modmakerLanguage != null && !modmakerLanguage.equals("")) {
			//language setting exists
			languageChoices.setSelectedItem(modmakerLanguage);
		} else {
			//Attempt lookup via registry.
			String locale = ModManager.LookupGameLanguageViaRegistryKey();
			if (locale != null) {
				String setlang = null;
				switch (locale) {
				case "en_US":
					setlang = "English";
					break;
				case "pl_PL":
					setlang = "Polish";
					break;
				case "fr_FR":
					setlang = "French";
					break;
				case "ru_RU":
					setlang = "Russian";
					break;
				case "it_IT":
					setlang = "Italian";
					break;
				case "ja":
					setlang = "Japanese";
					break;
				case "de_DE":
					setlang = "German";
					break;
				case "es_ES":
					setlang = "Spanish";
					break;
				}
				if (setlang != null) {
					languageChoices.setSelectedItem(setlang);
				}
			}
		}
	}

	/**
	 * Validates that all required components are available before starting a
	 * ModMaker session.
	 * 
	 * @return
	 */
	private boolean validateModMakerPrereqs() {
		try {
			ModManager.debugLogger.writeMessage("Verifying prereqs");
			hasDLCBypass = ModManager.hasKnownDLCBypass(biogameDir);
			File tankMasterCompiler = new File(ModManager.getTankMasterCompilerDir() + "MassEffect3.Coalesce.exe");
			if (!tankMasterCompiler.exists()) {
				dispose();
				ModManager.debugLogger.writeError("Tankmaster's compiler not detected. Abort. Searched at: " + tankMasterCompiler.toString());
				JOptionPane.showMessageDialog(null,
						"<html>You need TankMaster's Coalesced Compiler in order to use ModMaker.<br><br>It should have been bundled with Mod Manager in the data/tankmaster_coalesce folder.</html>",
						"Prerequesites Error", JOptionPane.ERROR_MESSAGE);

				return false;
			}
			ModManager.debugLogger.writeMessage("Detected TankMaster coalesced compiler");

			String commandlinetoolsdir = ModManager.getCommandLineToolsDir();
			File pccdecompress = new File(commandlinetoolsdir + "PCCDecompress.exe");
			File sfarextractor = new File(commandlinetoolsdir + "SFARTools-Extract.exe");
			if (!pccdecompress.exists() || !sfarextractor.exists()) {
				dispose();
				ModManager.debugLogger.writeError("Mod Manager Command Line tools not present, aborting.");

				JOptionPane.showMessageDialog(null,
						"<html>Mod Manager's Command Line library is not installed. Mod Manager should automatically download this on startup from Github.</html>",
						"Prerequesites Error", JOptionPane.ERROR_MESSAGE);

				return false;
			}
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null, "<html>An error occured while attempting to check prerequesites for ModMaker:<br>" + e.getMessage()
					+ "<br>Please report this to FemShep with the Mod Manager log at femshep@me3tweaks.com.</html>", "Prerequesites Error", JOptionPane.ERROR_MESSAGE);
			return false;
		}
		//All prereqs met.
		return true;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == sideloadButton) {
			sideloadModmaker(getLanguages(languageChoices.getSelectedItem().toString()));
		} else if (e.getSource() == downloadButton) {
			startModmaker(getLanguages(languageChoices.getSelectedItem().toString()));
		} else if (e.getSource() == codeField) {
			//enter button
			startModmaker(getLanguages(languageChoices.getSelectedItem().toString()));
		} else if (e.getSource() == makeModButton) {
			URI theURI;
			try {
				theURI = new URI("https://me3tweaks.com/modmaker");
				java.awt.Desktop.getDesktop().browse(theURI);
				dispose();
			} catch (URISyntaxException ex) {
				// TODO Auto-generated catch block
				ex.printStackTrace();
			} catch (IOException ex) {
				// TODO Auto-generated catch block
				ex.printStackTrace();
			}
		} else if (e.getSource() == browseModsButton) {
			URI theURI;
			try {
				theURI = new URI("https://me3tweaks.com/modmaker/gallery/popular/1");
				java.awt.Desktop.getDesktop().browse(theURI);
				dispose();
			} catch (URISyntaxException ex) {
				// TODO Auto-generated catch block
				ex.printStackTrace();
			} catch (IOException ex) {
				// TODO Auto-generated catch block
				ex.printStackTrace();
			}
		}

	}

	/**
	 * Sideloads a ModMaker XML file rather than downloading one to memory.
	 * 
	 * @param languages
	 */
	private void sideloadModmaker(ArrayList<String> languages) {
		JFileChooser dirChooser = new JFileChooser();
		File file = new File(ModManager.getModmakerCacheDir());
		dirChooser.setCurrentDirectory(file);
		dirChooser.setDialogTitle("Select ModMaker XML file");
		dirChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		dirChooser.setAcceptAllFileFilterUsed(false);
		dirChooser.setFileFilter(new FileNameExtensionFilter("ModMaker Mod Delta Files (.xml)", "xml"));

		String chosenFile = null;
		if (dirChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
			chosenFile = dirChooser.getSelectedFile().getAbsolutePath();
			boolean shouldContinue = true;
			if (!hasDLCBypass) {
				shouldContinue = installBypass();
			}
			if (shouldContinue) {
				Wini ini = ModManager.LoadSettingsINI();
				try {
					ini.put("Settings", "modmaker_language", languageChoices.getSelectedItem());
					ini.store();
				} catch (InvalidFileFormatException e) {
					ModManager.debugLogger.writeErrorWithException("Invalid file format exception writing settings ini: ", e);
				} catch (IOException e) {
					ModManager.debugLogger.writeErrorWithException("Settings file encountered an I/O error while attempting to write it. Settings not saved.", e);
				}
				this.setModalityType(Dialog.ModalityType.MODELESS);
				dispose();
				ModManagerWindow.ACTIVE_WINDOW.startModMaker(chosenFile, languages);
			}
		}
	}

	private static ArrayList<String> getLanguages(String chosenLang) {
		ArrayList<String> languagesToCompile = new ArrayList<String>();
		switch (chosenLang) {
		case ALL_LANG:
			languagesToCompile.add("INT");
			languagesToCompile.add("RUS");
			languagesToCompile.add("ESN");
			languagesToCompile.add("POL");
			languagesToCompile.add("FRA");
			languagesToCompile.add("ITA");
			languagesToCompile.add("DEU");
			languagesToCompile.add("JPN");
			break;
		case ENGLISH:
			languagesToCompile.add("INT");
			break;
		case RUSSIAN:
			languagesToCompile.add("RUS");
			break;
		case SPANISH:
			languagesToCompile.add("ESN");
			break;
		case POLISH:
			languagesToCompile.add("POL");
			break;
		case FRENCH:
			languagesToCompile.add("FRA");
			break;
		case ITALIAN:
			languagesToCompile.add("ITA");
			break;
		case GERMAN:
			languagesToCompile.add("DEU");
			break;
		case JAPANESE:
			languagesToCompile.add("JPN");
		default:
			break;
		}
		return languagesToCompile;
	}

	/**
	 * Gets an arraylist of languages to compile by default.
	 * 
	 * @return
	 */
	public static ArrayList<String> getDefaultLanguages() {
		String defaultLang = ALL_LANG;
		Wini settingsini = ModManager.LoadSettingsINI();
		String modmakerLanguage = settingsini.get("Settings", "modmaker_language");
		if (modmakerLanguage != null && !modmakerLanguage.equals("")) {
			//language setting exists
			defaultLang = modmakerLanguage;
		}

		return getLanguages(defaultLang);
	}

	/**
	 * Starts modmaker in download mode.
	 * 
	 * @param languages
	 *            Languages to compile
	 */
	private void startModmaker(ArrayList<String> languages) {
		dispose();
		boolean shouldContinue = true;
		if (!hasDLCBypass) {
			shouldContinue = installBypass();
		}
		if (shouldContinue) {
			Wini ini;
			try {
				File settings = new File(ModManager.SETTINGS_FILENAME);
				if (!settings.exists())
					settings.createNewFile();
				ini = new Wini(settings);
				ini.put("Settings", "modmaker_language", languageChoices.getSelectedItem());
				ini.store();
			} catch (InvalidFileFormatException e) {
				e.printStackTrace();
			} catch (IOException e) {
				ModManager.debugLogger.writeErrorWithException("Settings file encountered an I/O error while attempting to write it. Settings not saved.", e);
			}
			ModManagerWindow.ACTIVE_WINDOW.startModMaker(codeField.getText().toString(), languages);
		}
	}

	/**
	 * Installs the LauncherWV bypass.
	 * 
	 * @return
	 */
	private boolean installBypass() {
		return ModManager.installBinkw32Bypass(biogameDir, false);
	}
}
