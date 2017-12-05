package com.me3tweaks.modmanager;

import java.awt.Dialog;
import java.awt.Dimension;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import javax.swing.JDialog;
import javax.swing.JPanel;

import com.me3tweaks.modmanager.utilities.ResourceUtils;

import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.Duration;

public class AboutWindow2 extends JDialog {

	private Dimension WINDOW_SIZE = new Dimension(550, 415);

	public AboutWindow2() {
        super(null, Dialog.ModalityType.APPLICATION_MODAL);
		Platform.setImplicitExit(false);
		setupWindow();
		setVisible(true);
	}

	private void setupWindow() {
		setResizable(false);
		setTitle("About Mod Manager");
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		setIconImages(ModManager.ICONS);
		setPreferredSize(WINDOW_SIZE);
		JFXPanel fxPanel = new JFXPanel();
		add(fxPanel);
		//setPreferredSize(WINDOW_SIZE);
		pack();
		setLocationRelativeTo(ModManagerWindow.ACTIVE_WINDOW);
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				initFX(fxPanel);
			}
		});
	}

	private void initFX(JFXPanel fxPanel) {
		// This method is invoked on the JavaFX thread
		try {
			Parent root = FXMLLoader.load(getClass().getResource("/com/me3tweaks/modmanager/ui/aboutwindow.fxml"));
			Scene scene = new Scene(root);
			Label version = (Label) scene.lookup("#versionLabel");
			version.setText("Version " + ModManager.VERSION + " - Build " + ModManager.BUILD_NUMBER + " - " + ModManager.BUILD_DATE);
			//Scene scene = createScene();
			fxPanel.setScene(scene);
		} catch (Exception e) {
			ModManager.debugLogger.writeErrorWithException("ERROR CREATING SCENE...", e);
		}
	}

}
