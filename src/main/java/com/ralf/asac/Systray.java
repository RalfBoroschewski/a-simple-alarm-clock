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
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

class Systray extends CommonPanel {

	private final MainClass mainClass;
	private final TrayIcon trayIcon;

	private final Popup sysTrayPopup;
	boolean timeDurationFieldIsSetInternal;

	@SuppressWarnings("java:S4507")
	Systray(final MainClass mainClass) {
		super(true);
		this.mainClass = mainClass;

		init(null);

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

		final Stage owner = new Stage();
		owner.initStyle(StageStyle.UTILITY);
		owner.setOpacity(1);
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

		final DurationButton trayIconDurationButton = new DurationButton();
		trayIconDurationButton.init(mainClass);

		final Button showAlarmManager = new Button("Alarm manager");

		sysTrayPopup.getContent().add(getPane());

		hideWhenMouseClickedOutsideSysTrayPopup(mainClass, getPane(), showAlarmManager);

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

	@SuppressWarnings("unused")
	private void hideWhenMouseClickedOutsideSysTrayPopup(MainClass mainClass, Popup sysTrayPopup) {
		EventHandler<MouseEvent> handler = (javafx.event.EventHandler<MouseEvent>) event -> {

			final double screenX = event.getScreenX();
			final double screenY = event.getScreenY();

			final boolean insidePopup = sysTrayPopup.getContent().stream().filter(Node.class::isInstance)
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

	private void hideWhenMouseClickedOutsideSysTrayPopup(final MainClass mainClass, final Pane pane,
			final Button showAlarmManager) {
		final Scene scene = mainClass.getScene();

		final EventHandler<MouseEvent> handler = event -> {
			final Bounds bounds = pane.localToScreen(pane.getBoundsInLocal());

			if (bounds == null || !bounds.contains(event.getScreenX(), event.getScreenY())) {
				sysTrayPopup.hide();
			}
		};

		scene.addEventFilter(MouseEvent.MOUSE_PRESSED, handler);

		sysTrayPopup.setOnHidden(event -> scene.removeEventFilter(MouseEvent.MOUSE_PRESSED, handler));

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

	@SuppressWarnings("java:S4507")
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

	@Override
	AlarmsComboBox getAlarmsComboBox() {
		return alarmsComboBox;
	}

	@Override
	public void processOnActionAlarmsComboBox() {
		mainClass.getCommonPanel().processOnActionAlarmsComboBox();
	}

	@Override
	public void processOnActionDeactivateButton() {
		mainClass.getCommonPanel().processOnActionDeactivateButton();
	}

	@Override
	public void processOnActionPauseButton() {
		mainClass.getCommonPanel().processOnActionPauseButton();
	}

	@Override
	public void processOnActionAlarmManagerButton() {
		mainClass.getCommonPanel().processOnActionAlarmManagerButton();
	}

	@Override
	public void processOnActionRepeatButton() {
		mainClass.getCommonPanel().processOnActionRepeatButton();
	}
}
