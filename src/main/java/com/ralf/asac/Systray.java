package com.ralf.asac;

import java.awt.AWTException;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.event.MouseAdapter;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;
import javax.swing.SwingUtilities;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

class Systray {

	private TrayIcon trayIcon;

	Systray(Stage stage, MainClass mainClass) {
		if (!SystemTray.isSupported()) {
			return;
		}

		if (Asac.getOperationSystem() == Asac.OperationSystem.KDE) {
			return;
		}

		Stage owner = new Stage();
		owner.initStyle(StageStyle.UTILITY);
		owner.setOpacity(0);
		owner.setWidth(1);
		owner.setHeight(1);

		URL imageURL = ClassLoader.getSystemResource("alarm.png");
		BufferedImage image;
		try {
			image = ImageIO.read(imageURL);
			trayIcon = new TrayIcon(image);
			trayIcon.setImageAutoSize(true);
		} catch (IOException exception) {
			exception.printStackTrace();
			trayIcon = null;
		}

		if (trayIcon != null) {
			Popup sysTrayPopup = new Popup();
			sysTrayPopup.setAutoHide(true);
			sysTrayPopup.setHideOnEscape(true);
			sysTrayPopup.setConsumeAutoHidingEvents(true);

			DurationButton trayIconDurationButton = new DurationButton(stage, mainClass, sysTrayPopup);

			Button trayIconTimeButton = new Button(MainClass.messages.getString("MainClass.systree.set.time"));

			TimeDurationField timeDurationField = new TimeDurationField();

			Button exitButton = new Button(MainClass.messages.getString("MainClass.exit"));
			exitButton.setOnAction(event -> System.exit(0));

			Button showAlarmManager = new Button("Alarm manager");

			VBox content = new VBox(trayIconDurationButton, trayIconTimeButton, timeDurationField, showAlarmManager,
					exitButton);

			content.setPadding(new Insets(10));
			content.setSpacing(5);
			content.setStyle("-fx-background-color: white;" + "-fx-border-color: gray;");

			sysTrayPopup.getContent().add(content);

			trayIcon.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(java.awt.event.MouseEvent e) {
					if (SwingUtilities.isRightMouseButton(e)) {
						Platform.runLater(() -> {
							owner.show();
							sysTrayPopup.show(owner, e.getXOnScreen(), e.getYOnScreen());
						});
					} else if (SwingUtilities.isLeftMouseButton(e)) {
						Platform.runLater(() -> {
							mainClass.show();
							sysTrayPopup.hide();
							SystemTray.getSystemTray().remove(trayIcon);
						});
					}
				}
			});
		}
	}

	boolean hasSystray() {
		return trayIcon != null;
	}

	void setSystrayToolTip(final String tooltip) {
		if (trayIcon != null) {
			trayIcon.setToolTip(tooltip);
		}
	}

	void addSystray() {
		boolean alreadySet = false;
		for (TrayIcon currentTrayIcon : SystemTray.getSystemTray().getTrayIcons()) {
			if (currentTrayIcon == trayIcon) {
				alreadySet = true;
				break;
			}
		}

		if (!alreadySet) {
			try {
				SystemTray.getSystemTray().add(trayIcon);
			} catch (AWTException exception) {
				exception.printStackTrace();
			}
		}
	}

}
