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

import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.stage.Popup;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

class Systray extends CommonPanel {

	private final MainClass mainClass;
	private final TrayIcon trayIcon;

	private /* final */ Popup sysTrayPopup;
	boolean timeDurationFieldIsSetInternal;

	@SuppressWarnings("java:S4507")
	Systray(final MainClass mainClass) {
		super(true);
		this.mainClass = mainClass;

		init(null);

		durationButton.init(mainClass);
		timeButton.init(mainClass);

		System.out.println("Holla 1");
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

		System.out.println("Holla 2");
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

		System.out.println("Holla 3");
		final Stage owner = new Stage();
//		owner.initStyle(StageStyle.UTILITY);
		owner.setOpacity(1);
		owner.setWidth(1);
		owner.setHeight(1);
		sysTrayPopup = new Popup();
//		sysTrayPopup.setAutoHide(true);
//		sysTrayPopup.setHideOnEscape(true);
//		sysTrayPopup.setConsumeAutoHidingEvents(true);

		Stage popupStage = new Stage();
		popupStage.initOwner(owner);
		popupStage.initStyle(StageStyle.UNDECORATED);

		Pane pane = getPane();
		pane.setPrefSize(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE);

		Scene scene = new Scene(pane);

		popupStage.setScene(scene);
		// popupStage.show();

		Platform.runLater(() -> pane.requestFocus());

//		VBox vbox = new VBox();
//
//		vbox.setPrefSize(300, 350);
//		vbox.setStyle("-fx-background-color: green;");
//		TextField tt = new TextField();
//		vbox.getChildren().addAll(tt);
//		sysTrayPopup.getContent().add(vbox);
//		owner.show();
//		sysTrayPopup.show(owner, 200, 200);
//
//		Platform.runLater(() -> {
//			tt.requestFocus();
//		});
		System.out.println("Holla 4");

		final DurationButton trayIconDurationButton = new DurationButton();
		trayIconDurationButton.init(mainClass);

		final Button showAlarmManager = new Button("Alarm manager");

		// sysTrayPopup.getContent().add(getPane());

		// hideWhenMouseClickedOutsideSysTrayPopup(mainClass, pane, showAlarmManager);

		trayIcon.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(java.awt.event.MouseEvent event) {
				if (SwingUtilities.isRightMouseButton(event)) {
					Platform.runLater(() -> {
						System.out.println("Holla 5");
						alarmsComboBox.showStoredAlarms();

						Point point = event.getLocationOnScreen();

						System.out.println("Holla 6");
						Platform.runLater(() -> {
							System.out.println("Holla 7");
							popupStage.show();
							popupStage.sizeToScene();

							double width = popupStage.getWidth();
							double height = popupStage.getHeight();
							Screen screen = Screen.getScreensForRectangle(point.x, point.y, 1, 1).get(0);

							double popupWidth = popupStage.getWidth();
							double popupHeight = popupStage.getHeight();

							// Bildschirm ermitteln, auf dem sich die Maus befindet
							Rectangle2D bounds = screen.getVisualBounds();

							double x = point.x;
							double y = point.y;

							System.out.println("Holla 8");
							// Rechts über den Bildschirmrand hinaus?
							if (x + width > bounds.getMaxX()) {
								System.out.println("Holla 9");
								x = bounds.getMaxX() - width;
							}

							// Unten über den Bildschirmrand hinaus?
							if (y + height > bounds.getMaxY()) {
								System.out.println("Holla 10");
								y = bounds.getMaxY() - height;
							}

							// Links über den Bildschirmrand hinaus?
							if (x < bounds.getMinX()) {
								System.out.println("Holla 11");
								x = bounds.getMinX();
							}

							// Oben über den Bildschirmrand hinaus?
							if (y < bounds.getMinY()) {
								System.out.println("Holla 12");
								y = bounds.getMinY();
							}

							// Horizontal:
							if (x + popupWidth > bounds.getMaxX()) {
								x = bounds.getMaxX() - popupWidth;
							}

							// Vertikal:
//							if (point.y + popupHeight <= bounds.getMaxY()) {
//								// Popup unterhalb des Mauszeigers
//								y = point.y;
//							} else {
//								// Kein Platz unten -> oberhalb des Mauszeigers
//								y = point.y - popupHeight;
//							}
							System.out.println("Mouse: " + point);
							System.out.println("Visual bounds: " + bounds);
							System.out.println("Popup size: " + popupStage.getWidth() + " x " + popupStage.getHeight());

							popupStage.setX(x);
							popupStage.setY(y);
						});

						// owner.show();
//						popupStage.show();
//						popupStage.setX(event.getXOnScreen());
//						popupStage.setY(event.getYOnScreen());
//						sysTrayPopup.show(owner, event.getXOnScreen(), event.getYOnScreen());
					});
				} else if (SwingUtilities.isLeftMouseButton(event)) {
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
		System.out.println("Hallo 1 " + text);
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
