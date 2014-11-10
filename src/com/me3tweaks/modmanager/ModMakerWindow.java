package com.me3tweaks.modmanager;

import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.plaf.basic.BasicScrollPaneUI.HSBChangeListener;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;

@SuppressWarnings("serial")
public class ModMakerWindow extends JDialog implements ActionListener{
	JLabel infoLabel;
	JButton downloadButton;
	JTextField codeField;
	String biogameDir;
	boolean hasDLCBypass = false;
	ModManagerWindow callingWindow;

	public ModMakerWindow(JFrame callingWindow, String biogameDir) {
		this.biogameDir = biogameDir;
		this.setTitle("ME3Tweaks Mod Maker");
		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		this.setPreferredSize(new Dimension(420, 228));
		this.setResizable(false);
		this.setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/resource/icon32.png")));
		this.pack();
		this.setLocationRelativeTo(callingWindow);
		this.callingWindow = (ModManagerWindow) callingWindow;
		validateModMakerPrereqs();
		setupWindow();
		this.setVisible(true);
		this.setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
	}

	private void setupWindow() {
		JPanel modMakerPanel = new JPanel();
		modMakerPanel.setLayout(new BoxLayout(modMakerPanel, BoxLayout.Y_AXIS));
		infoLabel = new JLabel("<html><body>ME3Tweaks ModMaker allows you to easily create Mass Effect 3 mods in a simple to use interface.<br>Enter a download code to download and compile a mod.</body></html>");
		infoLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
		modMakerPanel.add(infoLabel);
		JPanel codeDownloadPanel = new JPanel(new FlowLayout());
		
		
		codeField = new JTextField(6);
		// validationLabel = new JLabel("");
		//codeField.setInputVerifier(validator);
		//validation
		((AbstractDocument)codeField.getDocument()).setDocumentFilter(new DocumentFilter(){
		    Pattern pattern = Pattern.compile("-{0,1}\\d+");

		    @Override
		    public void replace(FilterBypass arg0, int arg1, int arg2, String arg3, AttributeSet arg4) throws BadLocationException {
		        String text = arg0.getDocument().getText(0, arg0.getDocument().getLength())+arg3;
		        Matcher matcher = pattern.matcher(text);
		        if(!matcher.matches()){
		            return;
		        }
		        if(text.length()>7){
		            return;
		        }
		        super.replace(arg0, arg1, arg2, arg3, arg4);
		    }
		});
		codeDownloadPanel.add(codeField);
		downloadButton = new JButton("Download & Compile");
		downloadButton.setPreferredSize(new Dimension(185, 19));
		codeDownloadPanel.add(downloadButton);
		codeDownloadPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		modMakerPanel.add(codeDownloadPanel);
		modMakerPanel.add(new JLabel("<html>Don't have a code? Create a mod at http://me3tweaks.com/modmaker or pick a mod others have made.</html>"));
		
		if (!hasDLCBypass) {
			modMakerPanel.add(new JLabel("<html>The binkw32.dll DLC bypass will be installed so your mod will work. Tab and ` will open the console in game.</html>"));
		}
		codeField.addActionListener(this);
		downloadButton.addActionListener(this);
		
		modMakerPanel.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
		this.getContentPane().add(modMakerPanel);
	}
	
	/**
	 * Validates that all required components are available before starting a Mod Maker session.
	 */
	private void validateModMakerPrereqs(){
		String wvdlcBink32MD5 = "5a826dd66ad28f0099909d84b3b51ea4"; //Binkw32.dll that bypasses DLC check (WV) - from Private Server SVN
		String wvdlcBink32MD5_2 = "05540bee10d5e3985608c81e8b6c481a"; //Binkw32.dll that bypasses DLC check (WV) - from Private Server SVN

		
		
		File bgdir = new File(biogameDir);
		File gamedir = bgdir.getParentFile();
		ModManager.debugLogger.writeMessage("Game directory: "+gamedir.toString());
		File bink32 = new File(gamedir.toString()+"\\Binaries\\Win32\\binkw32.dll");
		try {
			String binkhash = MD5Checksum.getMD5Checksum(bink32.toString());
			if (binkhash.equals(wvdlcBink32MD5) || binkhash.equals(wvdlcBink32MD5_2)){
				ModManager.debugLogger.writeMessage("Binkw32 DLC bypass installed");
				hasDLCBypass = true;
			} else {
				// Check for LauncherWV.
				File Launcher_WV = new File(gamedir.toString() + "\\Binaries\\Win32\\Launcher_WV.exe");
				File LauncherWV = new File(gamedir.toString() + "\\Binaries\\Win32\\LauncherWV.exe");
				if (Launcher_WV.exists() || LauncherWV.exists()) {
					//does exist
					hasDLCBypass = true;
					ModManager.debugLogger.writeMessage("Launcher WV DLC bypass installed");
				} else {
					// it doesn't exist... extract our copy of binkw32.dll
					hasDLCBypass = false;
					//Failure
					/*dispose();
					JOptionPane.showMessageDialog(null,
						    "<html>You don't have a way to bypass the DLC check.<br>To satisfy the requirement you need one of the following:<br> - Binkw32.dll DLC bypass in the binaries folder<br> - LauncherWV.exe in the Binaries folder<br><br>Information on how to fulfill this requirement can be found on me3tweaks.com.</html>",
						    "Prerequesites Error",
						    JOptionPane.ERROR_MESSAGE);*/
					ModManager.debugLogger.writeMessage("Binkw32.dll bypass hash failed, hash is: "+binkhash);
					ModManager.debugLogger.writeMessage("LauncherWV was not found in Win32 as Launcher_WV or LauncherWV.");
					ModManager.debugLogger.writeMessage("Advertising the DLC bypass install.");
					return;
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		File tankMasterCompiler = new File("Tankmaster Compiler/MassEffect3.Coalesce.exe");
		
		if (!tankMasterCompiler.exists()){
			dispose();
			JOptionPane.showMessageDialog(null,
				    "<html>You need TankMaster's Coalesced Compiler in order to use Mod Maker.<br><br>It should have been bundled with Mod Manager 3 in the TankMaster Compiler folder.</html>",
				    "Prerequesites Error",
				    JOptionPane.ERROR_MESSAGE);
			ModManager.debugLogger.writeMessage("Tankmaster's compiler not detected. Abort. Searched at: "+tankMasterCompiler.toString());
			return;
		}
		ModManager.debugLogger.writeMessage("Detected TankMaster coalesced compiler");
		//All prereqs met.
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == downloadButton) {
			startModmaker();
		} else
		if (e.getSource() == codeField) {
			//enter was pressed
			startModmaker();
		}
	}
	
	private void startModmaker(){
		dispose();
		boolean shouldContinue = true;
		if (!hasDLCBypass) {
			shouldContinue =installBypass();
		}
		if (shouldContinue){
			callingWindow.startModMaker(codeField.getText().toString());
		}
	}
	
	private boolean installBypass(){
		ModManager.debugLogger.writeMessage("Installing binkw32.dll DLC authorizer. Will backup original to binkw32_orig.dll");
		//extract and install binkw32.dll
		//from http://stackoverflow.com/questions/7168747/java-creating-self-extracting-jar-that-can-extract-parts-of-itself-out-of-the-a
        //ClassLoader cl = ModManager.class.getClassLoader();
        
		File bgdir = new File(biogameDir);
		File gamedir = bgdir.getParentFile();
		ModManager.debugLogger.writeMessage("Set binkw32.dll game folder to: "+gamedir.toString());
		File bink32 = new File(gamedir.toString()+"\\Binaries\\Win32\\binkw32.dll");
		File bink32_orig = new File(gamedir.toString()+"\\Binaries\\Win32\\binkw32_orig.dll");

        //File bink32 = new File("dlcpatcher/binkw32.dll");
        if (bink32.exists()) {
        	//if we got here binkw32.dll should have failed the hash check
        	Path source = Paths.get(bink32.toString());
    		Path destination = Paths.get(bink32_orig.toString());
    		//create backup of original
    		try {
    			Files.copy(source, destination, StandardCopyOption.REPLACE_EXISTING);
    		} catch (IOException ex) {
    			ex.printStackTrace();
    			return false;
    		}
        }
        try {
			ModManager.ExportResource("/binkw32.dll", bink32.toString());
		} catch (Exception e1) {
			e1.printStackTrace();
			return false;
		}
	
	return true;
	}
}
