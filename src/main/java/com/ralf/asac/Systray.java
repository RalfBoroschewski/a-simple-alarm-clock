package com.ralf.asac;

import java.awt.AWTException;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.event.MouseAdapter;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.SwingUtilities;

import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

class Systray {

	private final TrayIcon trayIcon;

	private final CommonPanel commonPanel;
	private final Popup sysTrayPopup;
	boolean timeDurationFieldIsSetInternal;

	private final AlarmsComboBox alarmsComboBox;
	private final TimeDurationField timeDurationField;
	private final Button deactivateButton;
	private final Button pauseButton;
	private final Button alarmManagerButton;
	private boolean pauseButtonIsPause;
	private final Button repeatButton;
	private long repeatDuration;
	final DurationButton durationButton;
	final TimeButton timeButton;

	Systray(final MainClass mainClass) {

		commonPanel = new CommonPanel();
		timeDurationField = commonPanel.getTimeDurationField();
		alarmsComboBox = commonPanel.getAlarmsComboBox();
		deactivateButton = commonPanel.getDeactivateButton();
		pauseButton = commonPanel.getPauseButton();
		repeatButton = commonPanel.getRepeatButton();
		alarmManagerButton = commonPanel.getAlarmManagerButton();
		durationButton = commonPanel.getDurationButton();
		timeButton = commonPanel.getTimeButton();

		commonPanel.init(null);

		durationButton.init(mainClass);
		timeButton.init(mainClass);

		sysTrayPopup = new Popup();
		alarmsComboBox.initialize(mainClass, timeDurationField);
		timeDurationField.setListener(alarmsComboBox, mainClass);
		alarmsComboBox.setAlarmsComboBoxToBeSynchronize(mainClass.getAlarmsComboBox());

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

		DurationButton trayIconDurationButton = new DurationButton();
		trayIconDurationButton.init(mainClass);

		Button trayIconTimeButton = new Button(MainClass.messages.getString("MainClass.systree.set.time"));

		Button exitButton = new Button(MainClass.messages.getString("MainClass.exit"));
		exitButton.setOnAction(event -> System.exit(0));

		Button showAlarmManager = new Button("Alarm manager");

//		VBox content = new VBox(trayIconDurationButton, trayIconTimeButton, timeDurationField, alarmsComboBox,
//				showAlarmManager, exitButton);
//
//		content.setPadding(new Insets(10));
//		content.setSpacing(5);
//		content.setStyle("-fx-background-color: white;" + "-fx-border-color: gray;");

		sysTrayPopup.getContent().add(commonPanel.getPane());

//		hideWhenMouseClickedOutsideSysTrayPopup(mainClass, sysTrayPopup, showAlarmManager);
		hideWhenMouseClickedOutsideSysTrayPopup(mainClass, commonPanel.getPane(), showAlarmManager);

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

	}

	void hideWhenMouseClickedOutsideSysTrayPopup(MainClass mainClass, Popup sysTrayPopup, Button showAlarmManager) {
		List<MouseEvent> events = new ArrayList<>();

		EventHandler handler = (javafx.event.EventHandler<MouseEvent>) event -> {

			double screenX = event.getScreenX();
			double screenY = event.getScreenY();

			boolean insidePopup = sysTrayPopup.getContent().stream().filter(Node.class::isInstance)
					.map(Node.class::cast).anyMatch(node -> {
						Bounds bounds = node.localToScreen(node.getBoundsInLocal());

						return bounds != null && bounds.contains(screenX, screenY);
					});

			if (!insidePopup) {
				sysTrayPopup.hide();
			}
		};

		mainClass.getScene().addEventFilter(MouseEvent.MOUSE_PRESSED, handler);

		sysTrayPopup.setOnHidden(event -> mainClass.getScene().removeEventFilter(MouseEvent.MOUSE_PRESSED, handler));
	}

	void hideWhenMouseClickedOutsideSysTrayPopup(MainClass mainClass, Pane pane, Button showAlarmManager) {
		Scene scene = mainClass.getScene();

		EventHandler<MouseEvent> handler = event -> {
			Bounds bounds = pane.localToScreen(pane.getBoundsInLocal());

			if (bounds == null || !bounds.contains(event.getScreenX(), event.getScreenY())) {
				sysTrayPopup.hide();
			}
		};

		scene.addEventFilter(MouseEvent.MOUSE_PRESSED, handler);

		sysTrayPopup.setOnHidden(event -> {
			scene.removeEventFilter(MouseEvent.MOUSE_PRESSED, handler);
		});

		showAlarmManager.setOnAction(event -> {
			new AlarmManager(mainClass);
			alarmsComboBox.showStoredAlarms();
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

	AlarmsComboBox getAlarmsComboBox() {
		return alarmsComboBox;
	}
}
