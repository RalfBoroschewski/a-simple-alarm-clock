package com.ralf.asac;

import java.awt.AWTException;
import java.awt.Point;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.event.MouseAdapter;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;
import javax.swing.SwingUtilities;

import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

class Systray extends CommonPanel {

	private final MainClass mainClass;
	private final TrayIcon trayIcon;
	private final Stage popupStage;

	boolean timeDurationFieldIsSetInternal;

	@SuppressWarnings("java:S4507")
	Systray(final MainClass mainClass) {
		super(true);
		this.mainClass = mainClass;

		init(null);

		durationButton.init(mainClass);
		timeButton.init(mainClass);

		alarmsComboBox.initialize(mainClass, timeDurationField);
		timeDurationField.setListener(alarmsComboBox, mainClass, this);
		alarmsComboBox.setAlarmsComboBoxToBeSynchronize(mainClass.getAlarmsComboBox());

		if (!SystemTray.isSupported()) {
			trayIcon = null;
			popupStage = null;
			return;
		}

		if (Asac.getOperationSystem() == Asac.OperationSystem.KDE) {
			trayIcon = null;
			popupStage = null;
			return;
		}

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
			popupStage = null;
			return;
		}

		final Stage owner = new Stage();
		owner.setOpacity(1);
		owner.setWidth(1);
		owner.setHeight(1);

		popupStage = new Stage();
		popupStage.initOwner(owner);
		popupStage.initStyle(StageStyle.UNDECORATED);

		Pane pane = getPane();
		pane.setPrefSize(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE);

		Scene scene = new Scene(pane);

		popupStage.setScene(scene);

		final DurationButton trayIconDurationButton = new DurationButton();
		trayIconDurationButton.init(mainClass);

		addListener();

		hideWhenMouseClickedOutsideSysTrayPopup();
	}

	private void addListener() {
		trayIcon.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(java.awt.event.MouseEvent event) {
				if (SwingUtilities.isRightMouseButton(event)) {
					Platform.runLater(() -> {
						alarmsComboBox.showStoredAlarms();

						Platform.runLater(() -> {
							Point2D point = getPopupStageCoordinates(event);

							popupStage.setX(point.getX());
							popupStage.setY(point.getY());

							popupStage.show();
							popupStage.sizeToScene();
						});

					});
				} else if (SwingUtilities.isLeftMouseButton(event)) {
					Platform.runLater(() -> {
						mainClass.show();
						SystemTray.getSystemTray().remove(trayIcon);
					});
				}
			}
		});
	}

	Point2D getPopupStageCoordinates(final java.awt.event.MouseEvent event) {
		final Point point = event.getLocationOnScreen();
		final Screen screen = Screen.getScreensForRectangle(point.x, point.y, 1, 1).get(0);
		final double popupWidth = popupStage.getWidth();

		final double width = popupStage.getWidth();
		final double height = popupStage.getHeight();

		final Rectangle2D bounds = screen.getVisualBounds();

		double x = point.x;
		double y = point.y;

		// Adjust when popupStage is to right from the screen border
		if (x + width > bounds.getMaxX()) {
			x = bounds.getMaxX() - width;
		}

		// Adjust when popupStage is to below from the screen border
		if (y + height > bounds.getMaxY()) {
			y = bounds.getMaxY() - height;
		}

		// Adjust when popupStage is to left from the screen border
		if (x < bounds.getMinX()) {
			x = bounds.getMinX();
		}

		// Adjust when popupStage is to above from the screen border
		if (y < bounds.getMinY()) {
			y = bounds.getMinY();
		}

		// Horizontal:
		if (x + popupWidth > bounds.getMaxX()) {
			x = bounds.getMaxX() - popupWidth;
		}

		return new Point2D(x, y);
	}

	private void hideWhenMouseClickedOutsideSysTrayPopup() {
		popupStage.focusedProperty().addListener((obs, oldValue, focused) -> {
			if (focused == null || !focused) {
				final PauseTransition delay = new PauseTransition(Duration.millis(100));

				delay.setOnFinished(event -> {
					if (!popupStage.isFocused()) {
						popupStage.hide();
					}
				});

				delay.play();
			}
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
		if (popupStage != null) {
			popupStage.hide();
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
