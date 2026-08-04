package com.ralf.asac;

import java.awt.AWTException;
import java.awt.Menu;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.event.MouseAdapter;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.imageio.ImageIO;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

public class MainClass extends Application {
	private Stage stage;

	private final AlarmsComboBox alarmsComboBox;
	private final TimeDurationField timeDurationField;
	private boolean timeDurationFieldIsSetInternal;
	PerformTime oldPerformTime;
	PerformDuration oldPerformDuration;
	private final Button deactivateButton;
	private final Button pauseButton;
	boolean pauseButtonIsPause;

	BellIcon bellIcon;

	static final ResourceBundle messages = ResourceBundle.getBundle("messages", Locale.getDefault());
	static final String PAUSE_KEY = "MainClass.pause";

	private double xOffset = 0;
	private double yOffset = 0;

	private TrayIcon trayIcon;
	private Menu trayIconDurationMenu;
	private Menu trayIconTimeMenu;

	static void start(final String[] args) {
		launch(args);
	}

	public MainClass() {
		timeDurationField = new TimeDurationField();
		deactivateButton = new Button(messages.getString("MainClass.deactivate"));
		pauseButton = new Button(messages.getString(PAUSE_KEY));
		alarmsComboBox = new AlarmsComboBox(this, timeDurationField);
	}

	@SuppressWarnings({ "exports", "java:S3776", "unused" })
	@Override
	public void start(final Stage stage) throws Exception {

		this.stage = stage;
		stage.initStyle(StageStyle.UNDECORATED);
		setIcon(false);

		final Pane pane = new Pane();

		pane.getChildren();

		final Button alarmManagerButton = new Button(messages.getString("MainClass.alarm.manager"));
		final DurationButton durationButton = new DurationButton(this);
		final TimeButton timeButton = new TimeButton(this);
		pauseButton.setVisible(false);

		showStoredAlarms();

		final double width = Double.parseDouble(messages.getString("MainClass.buttons.width"));
		alarmsComboBox.setPrefWidth(width);
		alarmManagerButton.setPrefWidth(width);
		durationButton.setPrefWidth(width);
		timeButton.setPrefWidth(width);
		timeDurationField.setPrefWidth(width);
		deactivateButton.setPrefWidth(width);

		final GridPane gridPane = new GridPane();

		int positionX = 0;
		int positionY = 0;

		Button minimizeButton = new Button("_");
		Button finishButton = new Button("✕");

		BorderPane titlePane = new BorderPane();
		HBox hBoxTitleBar = new HBox();
		hBoxTitleBar.setAlignment(Pos.CENTER_RIGHT);
		hBoxTitleBar.setMaxWidth(Double.MAX_VALUE);

		hBoxTitleBar.getChildren().addAll(minimizeButton, finishButton);
		titlePane.setRight(hBoxTitleBar);

		positionX = 1;
		gridPane.add(titlePane, positionX, positionY, 1, 1);

		positionX = 0;
		positionY++;

		gridPane.add(alarmsComboBox, positionX, positionY, 1, 1);
		positionX++;
		gridPane.add(alarmManagerButton, positionX, positionY, 1, 1);

		positionX = 0;
		positionY++;

		gridPane.add(durationButton, positionX, positionY, 1, 1);
		positionX++;
		gridPane.add(timeButton, positionX, positionY, 1, 1);

		positionX = 0;
		positionY++;

		gridPane.add(timeDurationField, positionX, positionY, 1, 1);

		positionX++;
		gridPane.add(deactivateButton, positionX, positionY, 1, 1);

		positionX = 0;
		positionY++;

		gridPane.add(pauseButton, positionX, positionY, 1, 1);
		gridPane.setStyle("-fx-background-color: red;");

		VBox vBox = new VBox();

		alarmManagerButton.setOnAction(event -> {
			new AlarmManager(this);
			showStoredAlarms();
		});

		timeDurationField.setOnAction(event -> {
			String name = alarmsComboBox.getEditor().getText();
			for (Alarm alarm : alarmsComboBox.getItems()) {
				if (name.equals(alarm.name)) {
					alarmsComboBox.getEditor().setText("");
					break;
				}
			}
			evaluateTimeDurationField();
		});

		deactivateButton.setVisible(false);
		deactivateButton.setOnAction(event -> {
			deactivate();
			alarmsComboBox.setValue(null);
		});

		pauseButton.setOnAction(event -> {
			pauseButton.setText(messages.getString(pauseButtonIsPause ? PAUSE_KEY : "MainClass.continue"));
			pauseButtonIsPause = !pauseButtonIsPause;
		});

		final Scene scene = new Scene(gridPane);
		stage.setTitle(messages.getString("MainClass.title"));
		stage.setScene(scene);
		stage.show();

		final Rectangle2D primScreenBounds = Screen.getPrimary().getVisualBounds();

		final double stageWidth = stage.getWidth();
		final double stageHeight = stage.getHeight();
		final double windowsPositionX = (primScreenBounds.getWidth() - stageWidth) / 2;
		final double windowsPositionY = (primScreenBounds.getHeight() - stageHeight) / 2;
		stage.setX(windowsPositionX);
		stage.setY(windowsPositionY);

		stage.setOnCloseRequest(event -> deactivate());

		minimizeButton.setOnAction(event -> {

			if (Preferences.getSystrayMode() == Preferences.SystrayMode.NOT_IN_SYSTRAY || trayIcon == null) {
				return;
			}

			stage.hide();

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
				} catch (AWTException e1) {
					e1.printStackTrace();
				}
			}

		});

		finishButton.setOnAction(event -> System.exit(0));
		Platform.setImplicitExit(false);
		buildSysTray();

		gridPane.setOnMousePressed(this::handleMousePressed);
		gridPane.setOnMouseDragged(this::handleMouseDragged);
	}

	private void handleMousePressed(MouseEvent event) {
		xOffset = event.getSceneX();
		yOffset = event.getSceneY();
	}

	private void handleMouseDragged(MouseEvent event) {
		Stage tmpStage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
		tmpStage.setX(event.getScreenX() - xOffset);
		tmpStage.setY(event.getScreenY() - yOffset);
	}

	private void buildSysTray() {

		if (!SystemTray.isSupported()) {
			return;
		}

		String xdgDesktop = System.getenv("XDG_CURRENT_DESKTOP");
		String kdeSession = System.getenv("KDE_FULL_SESSION");

		if ("KDE".equalsIgnoreCase(xdgDesktop) || "true".equalsIgnoreCase(kdeSession)) {
			return;
		}

		URL imageURL = ClassLoader.getSystemResource("alarm.png");
		BufferedImage image;
		try {
			image = ImageIO.read(imageURL);
			trayIcon = new TrayIcon(image);
			trayIcon.setImageAutoSize(true);

			trayIconDurationMenu = new Menu("Duration");
			trayIconTimeMenu = new Menu("Time");

			PopupMenu mainPopupMenu = new PopupMenu();
			mainPopupMenu.add(trayIconDurationMenu);
			mainPopupMenu.add(trayIconTimeMenu);

			trayIcon.setPopupMenu(mainPopupMenu);

			trayIcon.addActionListener(event -> Platform.runLater(() -> {
				stage.show();
				stage.setIconified(false);
				SystemTray.getSystemTray().remove(trayIcon);
			}));

			trayIcon.addMouseListener(new MouseAdapter() {
				@Override
				public void mousePressed(java.awt.event.MouseEvent e) {
					if (e.isPopupTrigger()) {
						trayIconTimeMenu.removeAll();
						trayIconTimeMenu.add(new MenuItem("qqq"));
					}
				}
			});

		} catch (IOException e) {
			e.printStackTrace();
			trayIcon = null;
		}
	}

	boolean hasSystray() {
		return trayIcon != null;
	}

	void setSystrayTooltip(final String tooltip) {

		if (trayIcon != null) {
			trayIcon.setToolTip(tooltip);
		}
	}

	void showStoredAlarms() {
		final ArrayList<AlarmManager.AlarmManagerItem> items = Preferences.getAlarms();

		final ArrayList<Alarm> tmpStoredAlarms = new ArrayList<>();
		for (AlarmManager.AlarmManagerItem item : items) {
			tmpStoredAlarms.add(new Alarm(item.getName(), item.getTime(), item.getAlarmSoundData()));
		}

		alarmsComboBox.getItems().clear();
		alarmsComboBox.getItems().addAll(tmpStoredAlarms);

		if (items.isEmpty()) {
			Tooltip tooltip = new Tooltip(messages.getString("MainClass.tooltip"));
			tooltip.setShowDelay(new Duration(0));
			alarmsComboBox.setTooltip(tooltip);
		}

	}

	void clearAlarmsComboBox() {
		alarmsComboBox.clear();
	}

	void deactivate() {
		timeDurationField.setText("");

		pauseButton.setVisible(false);
		pauseButton.setText(messages.getString(PAUSE_KEY));
		pauseButtonIsPause = false;

		if (oldPerformTime != null) {
			oldPerformTime.stop();
		}
		oldPerformTime = null;
		if (oldPerformDuration != null) {
			oldPerformDuration.stop();
		}
		oldPerformDuration = null;
		deactivateButton.setVisible(false);

		if (bellIcon != null) {
			if (bellIcon.stage != null) {
				bellIcon.stage.hide();
				bellIcon.stage = null;
			}
			if (bellIcon.audioOutput != null) {
				bellIcon.audioOutput.stopPlaying();
			}
			bellIcon = null;
		}

		setIcon(false);

	}

	void evaluateTimeDurationField() {
		if (timeDurationFieldIsSetInternal) {
			return;
		}
		final String timeDuration = timeDurationField.getText();

		if (timeDuration != null && !timeDuration.isEmpty()) {
			deactivate();
			final int colonIndex = timeDuration.indexOf(':');
			if (colonIndex < 0) {
				final long minute = Long.parseLong(timeDuration);
				final PerformDuration performDuration = new PerformDuration(minute, this);
				performDuration.handle(null);
			} else {
				final String hourString = timeDuration.substring(0, colonIndex);
				final String minuteString = timeDuration.substring(colonIndex + 1);
				final int hour = Integer.parseInt(hourString);
				final int minute = Integer.parseInt(minuteString);
				final PerformTime performTime = new PerformTime(hour, minute, this);
				performTime.handle(null);
			}
		}
	}

	@SuppressWarnings("java:S6201")
	String getName() {
		return alarmsComboBox.getName();
	}

	void resetStoredAlarmsVaLue() {
		alarmsComboBox.setValue(null);
	}

	@SuppressWarnings("java:S4507")
	void setIcon(final boolean isActive) {

		final URL url = ClassLoader.getSystemResource(isActive ? "alarmActive.png" : "alarm.png");
		Image image = null;
		try {
			final InputStream inputStream = url.openStream();
			image = new Image(inputStream);
		} catch (IOException exception) {
			exception.printStackTrace();
		}

		if (image != null) {
			stage.getIcons().clear();
			stage.getIcons().add(image);
		}
	}

	void setTimeDurationFieldText(final String text) {
		Platform.runLater(() -> {
			timeDurationFieldIsSetInternal = true;
			timeDurationField.setText(text);
			timeDurationFieldIsSetInternal = false;
		});
	}

	void setVisibilityDeactivateButton(final boolean visibility) {
		Platform.runLater(() -> deactivateButton.setVisible(visibility));
	}

	void deactivatePauseButton() {
		Platform.runLater(() -> {
			pauseButton.setVisible(false);
			pauseButton.setText(messages.getString(PAUSE_KEY));
		});
	}

	void setVisibilityPauseButton(final boolean visibility) {
		Platform.runLater(() -> pauseButton.setVisible(visibility));
	}

	Alarm getStoredAlarm() {
		return alarmsComboBox.getStoredAlarm();
	}

	Stage getStage() {
		return stage;
	}
}
