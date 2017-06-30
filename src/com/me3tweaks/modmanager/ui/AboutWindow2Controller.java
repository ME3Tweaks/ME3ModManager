package com.me3tweaks.modmanager.ui;

import com.me3tweaks.modmanager.utilities.ResourceUtils;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;

/**
 * Controller class for AboutWindow2
 * 
 * @author Mgamerz
 *
 */
public class AboutWindow2Controller {
	public AboutWindow2Controller() {

	}

	@FXML
	private void openDerbyPage(ActionEvent event) {
		ResourceUtils.openWebpage("https://db.apache.org/derby/");
	}

	@FXML
	private void open7ZipJBindingPage() {
		ResourceUtils.openWebpage("http://sevenzipjbind.sourceforge.net/");
	}

	@FXML
	private void openCommonsIOPage() {
		ResourceUtils.openWebpage("https://commons.apache.org/proper/commons-io/");
	}

	@FXML
	private void openCommonsLangPage() {
		ResourceUtils.openWebpage("https://commons.apache.org/proper/commons-lang/");
	}

	@FXML
	private void openCommonsValidatorPage() {
		ResourceUtils.openWebpage("https://commons.apache.org/proper/commons-validator/");
	}

	@FXML
	private void openHttpComponentsPage() {
		ResourceUtils.openWebpage("https://hc.apache.org/");
	}

	@FXML
	private void openME3ExplorerPage() {
		ResourceUtils.openWebpage("https://github.com/ME3Explorer/ME3Explorer");
	}

	@FXML
	private void openMassEffectModderPage() {
		ResourceUtils.openWebpage("https://github.com/MassEffectModder/MassEffectModder");
	}

	@FXML
	private void openjsonsimplePage() {
		ResourceUtils.openWebpage("https://github.com/fangyidong/json-simple");
	}

	@FXML
	private void openini4jPage() {
		ResourceUtils.openWebpage("http://ini4j.sourceforge.net/");
	}

	@FXML
	private void openFileDropPage() {
		ResourceUtils.openWebpage("http://iharder.sourceforge.net/current/java/filedrop/");
	}

	@FXML
	private void open7ZipPage() {
		ResourceUtils.openWebpage("http://www.7-zip.org/");
	}

	@FXML
	private void openJNAPage() {
		ResourceUtils.openWebpage("https://github.com/java-native-access/jna");
	}

	@FXML
	private void openBinkw32Page() {
		ResourceUtils.openWebpage("http://me3explorer.freeforums.org/me3logger-t1932.html");
	}

	@FXML
	private void openBinkw32ASIPage() {
		ResourceUtils.openWebpage("https://github.com/Erik-JS/masseffect-binkw32");
	}

	@FXML
	private void openElevatePage() {
		ResourceUtils.openWebpage("http://code.kliu.org/misc/elevate/");
	}

	@FXML
	private void openJojoDiffPage() {
		ResourceUtils.openWebpage("http://jojodiff.sourceforge.net/");
	}

	@FXML
	private void openLaunch4jPage() {
		ResourceUtils.openWebpage("http://launch4j.sourceforge.net/");
	}

	@FXML
	private void openLauncherWVPage() {
		ResourceUtils.openWebpage("https://github.com/Mgamerz/LauncherWV");
	}

	@FXML
	private void openMEMPage() {
		ResourceUtils.openWebpage("https://github.com/MassEffectModder/MassEffectModder");
	}

	@FXML
	private void openTankmasterPage() {
		ResourceUtils.openWebpage("http://me3explorer.freeforums.org/post12495.html#p12495");
	}
}
