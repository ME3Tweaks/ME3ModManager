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
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.util.Duration;

public class AboutWindow2 extends JDialog {
	public AboutWindow2() {
		setupWindow();
		setVisible(true);
	}

	private FadeTransition fadeIn = new FadeTransition(Duration.millis(2000));

	private void setupWindow() {
		setTitle("About Mod Manager");
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		setResizable(false);
		setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
		setIconImages(ModManager.ICONS);

		JFXPanel fxPanel = new JFXPanel();

		add(fxPanel);
		setPreferredSize(new Dimension(400, 300));
		pack();
		setLocationRelativeTo(ModManagerWindow.ACTIVE_WINDOW);
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				initFX(fxPanel);
				fadeIn.playFromStart();

			}
		});
	}

	private void initFX(JFXPanel fxPanel) {
		// This method is invoked on the JavaFX thread
		try {
			Scene scene = createScene();
			fxPanel.setScene(scene);
		} catch (Exception e) {
			ModManager.debugLogger.writeErrorWithException("ERROR CREATING SCENE...", e);
		}
	}

	private Scene createScene() throws IOException {
		Group root = new Group();
		java.awt.Color awtColor = new JPanel().getBackground();
		int r = awtColor.getRed();
		int g = awtColor.getGreen();
		int b = awtColor.getBlue();
		int a = awtColor.getAlpha();
		double opacity = a / 255.0;
		javafx.scene.paint.Color backgroundColor = javafx.scene.paint.Color.rgb(r, g, b, opacity);
		Scene scene = new Scene(root, backgroundColor);

		GridPane topPane = new GridPane();

		ImageView logoImage = new ImageView();
		URL url = getClass().getResource("/resource/icon128.png");
		Image fxImgDirect = new Image(url.openStream());
		// simple displays ImageView the image as is
		logoImage.setImage(fxImgDirect);
		logoImage.setSmooth(true);
		logoImage.setCache(true);

		topPane.add(logoImage, 0, 0);
		GridPane.setRowSpan(logoImage, 5);

		Label me3ModManager = new Label("Mass Effect 3 Mod Manager");
		me3ModManager.setFont(new Font(15));
		topPane.add(me3ModManager, 1, 0);

		fadeIn.setNode(me3ModManager);
		fadeIn.setFromValue(0.0);
		fadeIn.setToValue(1.0);
		fadeIn.setCycleCount(1);
		fadeIn.setAutoReverse(false);

		//Uses - Bottom Panel
		GridPane usesGridPane = new GridPane();
		usesGridPane.setAlignment(Pos.CENTER);
		Label usesCode = new Label();
		usesCode.setFont(Font.font("Verdana", FontWeight.BOLD, 20));
		usesCode.setText("Contains code from");
		usesGridPane.add(usesCode, 0, 0);
		GridPane.setHalignment(usesCode, HPos.CENTER);
		GridPane.setColumnSpan(usesCode, GridPane.REMAINING);

		int column = 0;
		int row = 1;
		//ME3Explorer
		{
			Hyperlink me3explorerlink = new Hyperlink("ME3Explorer");
			me3explorerlink.setOnAction(new EventHandler<ActionEvent>() {

				@Override
				public void handle(ActionEvent event) {
					try {
						ResourceUtils.openWebpage(new URI("https://github.com/ME3Explorer/ME3Explorer"));
					} catch (URISyntaxException e) {
						ModManager.debugLogger.writeErrorWithException("Can't open ME3Explorer webpage!", e);
					}
				}
			});
			usesGridPane.add(me3explorerlink, column, row);
		}
		//Mass Effect Modder
		Hyperlink memlink = new Hyperlink("Mass Effect Modder");
		memlink.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				try {
					ResourceUtils.openWebpage(new URI("https://github.com/MassEffectModder/MassEffectModder"));
				} catch (URISyntaxException e) {
					ModManager.debugLogger.writeErrorWithException("Can't open MassEffectModder!", e);
				}
			}
		});
		usesGridPane.add(memlink, ++column, row);

		//7-Zip
		Hyperlink sevenzip = new Hyperlink("7-Zip JBinding");
		sevenzip.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				try {
					ResourceUtils.openWebpage(new URI("http://sevenzipjbind.sourceforge.net/"));
				} catch (URISyntaxException e) {
					ModManager.debugLogger.writeErrorWithException("Can't open 7-Zip JBinding!", e);
				}
			}
		});
		usesGridPane.add(sevenzip, ++column, row);

		//JNA
		Hyperlink jna = new Hyperlink("JNA");
		jna.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				try {
					ResourceUtils.openWebpage(new URI("https://github.com/java-native-access/jna"));
				} catch (URISyntaxException e) {
					ModManager.debugLogger.writeErrorWithException("Filedrop URL couldn't be opened.", e);
				}
			}
		});
		usesGridPane.add(jna,  ++column, row);

		row++;
		column = 0;
		//FileDrop
		Hyperlink filedrop = new Hyperlink("FileDrop");
		filedrop.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				try {
					ResourceUtils.openWebpage(new URI("http://iharder.sourceforge.net/current/java/filedrop/"));
				} catch (URISyntaxException e) {
					ModManager.debugLogger.writeErrorWithException("Filedrop URL couldn't be opened.", e);
				}
			}
		});
		usesGridPane.add(filedrop,  column, row);
		
		//json-simple
		Hyperlink jsonsimple = new Hyperlink("json-simple");
		jsonsimple.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				try {
					ResourceUtils.openWebpage(new URI("https://github.com/fangyidong/json-simple"));
				} catch (URISyntaxException e) {
					ModManager.debugLogger.writeErrorWithException("JSON-SIMPLE URL couldn't be opened.", e);
				}
			}
		});
		usesGridPane.add(jsonsimple,  ++column, row);
		
		//json-simple
		Hyperlink ini4j = new Hyperlink("ini4j");
		ini4j.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				try {
					ResourceUtils.openWebpage(new URI("http://ini4j.sourceforge.net/"));
				} catch (URISyntaxException e) {
					ModManager.debugLogger.writeErrorWithException("ini4j URL couldn't be opened.", e);
				}
			}
		});
		usesGridPane.add(ini4j,  ++column, row);

		//BUILD UI
		GridPane overallGridPane = new GridPane();
		overallGridPane.add(topPane, 0, 0);
		overallGridPane.add(usesGridPane, 0, 1);
		root.getChildren().add(overallGridPane);

		return (scene);
	}

	public static void main(String args[]) {
		new AboutWindow2();
	}
}
