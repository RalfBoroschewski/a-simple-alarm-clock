package com.ralf.asac;

import java.awt.AWTException;
import java.awt.Point;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.event.MouseAdapter;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import javax.imageio.ImageIO;
import javax.swing.SwingUtilities;

import javafx.application.Platform;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.stage.Screen;
import javafx.stage.Stage;

class Systray {

	private final MainClass mainClass;
	private final TrayIcon trayIcon;

	boolean timeDurationFieldIsSetInternal;

	private final HBox bottomPanel;

	@SuppressWarnings("java:S4507")
	Systray(final MainClass mainClass) {
		this.mainClass = mainClass;

		bottomPanel = new HBox();
		final Button exitButton = new Button(MainClass.messages.getString("Systray.exit"));
		bottomPanel.getChildren().add(exitButton);
		exitButton.setOnAction(event -> System.exit(0));

		if (!SystemTray.isSupported()) {
			trayIcon = null;
			return;
		}

		if (Asac.getOperationSystem() == Asac.OperationSystem.KDE) {
			trayIcon = null;
			return;
		}

		final URL imageURL = ClassLoader.getSystemResource("alarm.png");
		BufferedImage image = null;
		try {
			image = ImageIO.read(imageURL);
		} catch (IOException exception) {
			exception.printStackTrace();
		}

		if (image != null) {
			trayIcon = new TrayIcon(image);
			trayIcon.setImage(image);
			trayIcon.setImageAutoSize(true);
		} else {
			trayIcon = null;
			return;
		}

		final Pane pane = mainClass.getMainPanel().getPane();
		pane.setPrefSize(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE);

		addListener();
	}

	private void addListener() {
		trayIcon.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(final java.awt.event.MouseEvent event) {
				if (SwingUtilities.isLeftMouseButton(event)) {
					final Preferences.SystrayMode systrayMode = hasSystray() ? Preferences.getSystrayMode()
							: Preferences.SystrayMode.NOT_IN_SYSTRAY;

					switch (systrayMode) {
					case ONLY_IN_SYSTRAY:
						showAtSystray(event);
						break;
					case MINIMIZE_TO_SYSTRAY:
					case NOT_IN_SYSTRAY:
					default:
						showNormal();
						break;
					}
				} else if (SwingUtilities.isRightMouseButton(event)) {
					showAtSystray(event);
				}
			}
		});
	}

	private void showNormal() {
		Platform.runLater(() -> {
			final MainPanel mainPanel = mainClass.getMainPanel();
			mainPanel.restorePosition();
			final Stage stage = mainPanel.getStage();
			mainPanel.setHasFocusedPropertyCounterListener(Long.MAX_VALUE);
			mainPanel.init(mainClass.getTitlePane(), null, false);
			SystemTray.getSystemTray().remove(trayIcon);
			stage.show();
			mainPanel.restorePosition();
			stage.toFront();
			stage.sizeToScene();
		});
	}

	private void showAtSystray(final java.awt.event.MouseEvent event) {
		Platform.runLater(() -> {
			final Point2D point = getPopupStageCoordinates(event);
			final MainPanel mainPanel = mainClass.getMainPanel();
			mainPanel.setHasFocusedPropertyCounterListener(1);
			final Stage stage = mainPanel.getStage();
			mainPanel.init(null, bottomPanel, true);
			stage.setX(point.getX());
			stage.setY(point.getY());
			stage.show();
			stage.toFront();
			stage.setAlwaysOnTop(true);
		});
	}

	@SuppressWarnings("java:S4507")
	void setIcon(final boolean isActive) {
		if (trayIcon == null) {
			return;
		}
		final String name = isActive ? "alarmActive.png" : "alarm.png";
		final URL url = ClassLoader.getSystemResource(name);

		BufferedImage image = null;

		try {
			final InputStream inputStream = url.openStream();
			image = ImageIO.read(inputStream);
			trayIcon.setImage(image);
		} catch (IOException exception) {
			exception.printStackTrace();
		}
	}

	Point2D getPopupStageCoordinates(final java.awt.event.MouseEvent event) {
		final Point point = event.getLocationOnScreen();
		final Screen screen = Screen.getScreensForRectangle(point.x, point.y, 1, 1).get(0);
		final double popupWidth = mainClass.getStage().getWidth();

		final double width = mainClass.getStage().getWidth();
		final double height = mainClass.getStage().getHeight();

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
		for (final TrayIcon currentTrayIcon : SystemTray.getSystemTray().getTrayIcons()) {
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
