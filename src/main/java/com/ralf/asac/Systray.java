package com.ralf.asac;

import java.awt.AWTEvent;
import java.awt.AWTException;
import java.awt.Point;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.event.AWTEventListener;
import java.awt.event.MouseAdapter;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;
import javax.swing.SwingUtilities;

import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.stage.Popup;
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

		trayIcon.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(java.awt.event.MouseEvent event) {
				if (SwingUtilities.isRightMouseButton(event)) {
					Platform.runLater(() -> {
						alarmsComboBox.showStoredAlarms();

						Point point = event.getLocationOnScreen();

						Platform.runLater(() -> {
							popupStage.show();
							popupStage.sizeToScene();

							double width = popupStage.getWidth();
							double height = popupStage.getHeight();
							Screen screen = Screen.getScreensForRectangle(point.x, point.y, 1, 1).get(0);

							double popupWidth = popupStage.getWidth();

							Rectangle2D bounds = screen.getVisualBounds();

							double x = point.x;
							double y = point.y;

							// Rechts über den Bildschirmrand hinaus?
							if (x + width > bounds.getMaxX()) {
								x = bounds.getMaxX() - width;
							}

							// Unten über den Bildschirmrand hinaus?
							if (y + height > bounds.getMaxY()) {
								y = bounds.getMaxY() - height;
							}

							// Links über den Bildschirmrand hinaus?
							if (x < bounds.getMinX()) {
								x = bounds.getMinX();
							}

							// Oben über den Bildschirmrand hinaus?
							if (y < bounds.getMinY()) {
								y = bounds.getMinY();
							}

							// Horizontal:
							if (x + popupWidth > bounds.getMaxX()) {
								x = bounds.getMaxX() - popupWidth;
							}

							popupStage.setX(x);
							popupStage.setY(y);
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

		hideWhenMouseClickedOutsideSysTrayPopup2();
	}

	private void hideWhenMouseClickedOutsideSysTrayPopup() {
		Toolkit.getDefaultToolkit().addAWTEventListener(event -> {
			if (event instanceof java.awt.event.MouseEvent) {
				java.awt.event.MouseEvent mouseEvent = (java.awt.event.MouseEvent) event;
				if (mouseEvent.getID() == java.awt.event.MouseEvent.MOUSE_PRESSED) {
					Point p = mouseEvent.getLocationOnScreen();

					Platform.runLater(() -> {
						if (!popupStage.isShowing()) {
							return;
						}

						double x = popupStage.getX();
						double y = popupStage.getY();
						double width = popupStage.getWidth();
						double height = popupStage.getHeight();

						boolean inside = p.x >= x && p.x <= x + width && p.y >= y && p.y <= y + height;

						if (!inside) {
							popupStage.hide();
						}
					});
				}

			}
		}, AWTEvent.MOUSE_EVENT_MASK);
	}

	private void hideWhenMouseClickedOutsideSysTrayPopup1() {
		AWTEventListener listener = event -> {
			if (event instanceof java.awt.event.MouseEvent) {

				java.awt.event.MouseEvent mouseEvent = (java.awt.event.MouseEvent) event;
				if (mouseEvent.getID() == java.awt.event.MouseEvent.MOUSE_PRESSED) {
					Point p = mouseEvent.getLocationOnScreen();

					Platform.runLater(() -> {
						if (!popupStage.isShowing()) {
							return;
						}

						boolean inside = p.x >= popupStage.getX() && p.x <= popupStage.getX() + popupStage.getWidth()
								&& p.y >= popupStage.getY() && p.y <= popupStage.getY() + popupStage.getHeight();

						if (!inside) {
							popupStage.hide();
						}
					});
				}
			}

		};

		Toolkit.getDefaultToolkit().addAWTEventListener(listener, AWTEvent.MOUSE_EVENT_MASK);
	}

	private void hideWhenMouseClickedOutsideSysTrayPopup2() {
		popupStage.focusedProperty().addListener((obs, oldValue, focused) -> {
			if (!focused) {
				PauseTransition delay = new PauseTransition(Duration.millis(100));

				delay.setOnFinished(event -> {
					if (!popupStage.isFocused()) {
						popupStage.hide();
					}
				});

				delay.play();
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

//	private void hideWhenMouseClickedOutsideSysTrayPopup(final MainClass mainClass, final Pane pane,
//			final Button showAlarmManager) {
//		final Scene scene = mainClass.getScene();
//
//		final EventHandler<MouseEvent> handler = event -> {
//			final Bounds bounds = pane.localToScreen(pane.getBoundsInLocal());
//		};
//
//		scene.addEventFilter(MouseEvent.MOUSE_PRESSED, handler);
//
//		showAlarmManager.setOnAction(event -> {
//			new AlarmManager(mainClass);
//			alarmsComboBox.showStoredAlarms();
//		});
//	}

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
//
//	@Override
//	public void processAlarmsComboBoxValuePropertyListener() {
//	}
//
//	@Override
//	public void processOnActionTimeDurationField() {
//		System.out.println("Hallo 2 ");
//		mainClass.getCommonPanel().processOnActionTimeDurationField();
//		System.out.println("Hallo 3 ");
//		popupStage.hide();
//	}

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
