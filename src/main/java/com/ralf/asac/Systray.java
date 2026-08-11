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
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

class Systray {

	private final TrayIcon trayIcon;
	private final Popup sysTrayPopup;
	private final TimeDurationField timeDurationField;
	boolean timeDurationFieldIsSetInternal;
	private final AlarmsComboBox alarmsComboBox;

	Systray(final MainClass mainClass) {

		timeDurationField = new TimeDurationField();
		sysTrayPopup = new Popup();
		alarmsComboBox = new AlarmsComboBox(mainClass, timeDurationField);

		if (!SystemTray.isSupported()) {
			trayIcon = null;
			return;
		}

		if (Asac.getOperationSystem() == Asac.OperationSystem.KDE) {
			trayIcon = null;
			return;
		}

		Stage owner = new Stage();
		owner.initStyle(StageStyle.UTILITY);
		owner.setOpacity(0);
		owner.setWidth(1);
		owner.setHeight(1);

		URL imageURL = ClassLoader.getSystemResource("alarm.png");
		BufferedImage image = null;
		try {
			image = ImageIO.read(imageURL);
		} catch (IOException exception) {
			exception.printStackTrace();
		}

		if (image != null) {
			trayIcon = new TrayIcon(image);
			trayIcon.setImageAutoSize(true);
		} else {
			trayIcon = null;
			return;
		}

		sysTrayPopup.setAutoHide(true);
		sysTrayPopup.setHideOnEscape(true);
		sysTrayPopup.setConsumeAutoHidingEvents(true);

		DurationButton trayIconDurationButton = new DurationButton(mainClass);

		Button trayIconTimeButton = new Button(MainClass.messages.getString("MainClass.systree.set.time"));

		Button exitButton = new Button(MainClass.messages.getString("MainClass.exit"));
		exitButton.setOnAction(event -> System.exit(0));

		Button showAlarmManager = new Button("Alarm manager");

		VBox content = new VBox(trayIconDurationButton, trayIconTimeButton, timeDurationField, alarmsComboBox,
				showAlarmManager, exitButton);

		content.setPadding(new Insets(10));
		content.setSpacing(5);
		content.setStyle("-fx-background-color: white;" + "-fx-border-color: gray;");

		sysTrayPopup.getContent().add(content);

		trayIcon.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(java.awt.event.MouseEvent e) {
				if (SwingUtilities.isRightMouseButton(e)) {
					Platform.runLater(() -> {
						alarmsComboBox.showStoredAlarms();
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

		Scene scene = mainClass.getScene();

		EventHandler<MouseEvent> handler = event -> {
			Bounds bounds = content.localToScreen(content.getBoundsInLocal());

			if (bounds == null || !bounds.contains(event.getScreenX(), event.getScreenY())) {
				sysTrayPopup.hide();
			}
		};

		scene.addEventFilter(MouseEvent.MOUSE_PRESSED, handler);

// Später wieder entfernen:
		sysTrayPopup.setOnHidden(event -> {
			scene.removeEventFilter(MouseEvent.MOUSE_PRESSED, handler);
		});

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

	void hide() {
		if (sysTrayPopup != null) {
			sysTrayPopup.hide();
		}
	}

	void setTimeDurationFieldText(final String text) {
		Platform.runLater(() -> {
			timeDurationFieldIsSetInternal = true;
			timeDurationField.setText(text);
			timeDurationFieldIsSetInternal = false;
		});
	}
}
