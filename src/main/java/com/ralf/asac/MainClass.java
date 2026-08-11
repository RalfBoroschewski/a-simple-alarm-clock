package com.ralf.asac;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Locale;
import java.util.ResourceBundle;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.stage.Popup;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class MainClass extends Application {
	private Stage stage;
	private Scene scene;

	private final CommonPanel commonPanel;
	private final AlarmsComboBox alarmsComboBox;
	private final TimeDurationField timeDurationField;
	PerformTime oldPerformTime;
	PerformDuration oldPerformDuration;
	private final Button minimizeButton = new Button("_");
	private final Button finishButton = new Button("✕");
	private final Button deactivateButton;
	private final Button pauseButton;
	private final Button alarmManagerButton;
	private boolean pauseButtonIsPause;
	private final Button repeatButton;
	private long repeatDuration;
	final DurationButton durationButton;
	final TimeButton timeButton;

	BellIcon bellIcon;

	static final ResourceBundle messages = ResourceBundle.getBundle("messages", Locale.getDefault());
	static final String PAUSE_KEY = "MainClass.pause";

	private Systray systray;
	private double xOffset = 0;
	private double yOffset = 0;

	static void start(final String[] args) {
		launch(args);
	}

	public MainClass() {
		commonPanel = new CommonPanel();
		timeDurationField = commonPanel.getTimeDurationField();
		alarmsComboBox = commonPanel.getAlarmsComboBox();
		deactivateButton = commonPanel.getDeactivateButton();
		pauseButton = commonPanel.getPauseButton();
		repeatButton = commonPanel.getRepeatButton();
		alarmManagerButton = commonPanel.getAlarmManagerButton();
		durationButton = commonPanel.getDurationButton();
		timeButton = commonPanel.getTimeButton();
	}

	@SuppressWarnings({ "exports", "java:S3776", "java:S4507", "unused" })
	@Override
	public void start(final Stage stage) throws Exception {

		this.stage = stage;
		stage.initStyle(StageStyle.UNDECORATED);
		setIcon(false);

		commonPanel.init(getTitlePane());

		durationButton.init(this);
		timeButton.init(this);

		alarmsComboBox.initialize(this, timeDurationField);
		alarmsComboBox.showStoredAlarms();

		deactivateButton.setVisible(false);

		Pane pane = commonPanel.getPane();
		scene = new Scene(pane);

		setTitle(messages.getString("MainClass.title"));
		stage.setScene(scene);
		stage.show();

		setToMiddleOfTheScreen();

		stage.setOnCloseRequest(event -> deactivate());

		finishButton.setOnAction(event -> System.exit(0));
		Platform.setImplicitExit(false);

		systray = new Systray(this);

		pane.setOnMousePressed(this::handleMousePressed);
		pane.setOnMouseDragged(this::handleMouseDragged);

		alarmsComboBox.setAlarmsComboBoxToBeSynchronize(systray.getAlarmsComboBox());
		setListener();
	}

	private void setToMiddleOfTheScreen() {
		final Rectangle2D primScreenBounds = Screen.getPrimary().getVisualBounds();

		final double stageWidth = stage.getWidth();
		final double stageHeight = stage.getHeight();
		final double windowsPositionX = (primScreenBounds.getWidth() - stageWidth) / 2;
		final double windowsPositionY = (primScreenBounds.getHeight() - stageHeight) / 2;
		stage.setX(windowsPositionX);
		stage.setY(windowsPositionY);
	}

	private Pane getTitlePane() {

		BorderPane titlePane = new BorderPane();
		HBox hBoxTitleBar = new HBox();
		hBoxTitleBar.setAlignment(Pos.CENTER_RIGHT);
		hBoxTitleBar.setMaxWidth(Double.MAX_VALUE);

		hBoxTitleBar.getChildren().addAll(minimizeButton, finishButton);
		titlePane.setRight(hBoxTitleBar);

		return titlePane;
	}

	private void setListener() {
		alarmManagerButton.setOnAction(event -> {
			new AlarmManager(this);
			alarmsComboBox.showStoredAlarms();
		});

		deactivateButton.setOnAction(event -> {
			deactivate();
			alarmsComboBox.setValue(null);
		});

		pauseButton.setOnAction(event -> {
			pauseButton.setText(messages.getString(pauseButtonIsPause ? PAUSE_KEY : "MainClass.continue"));
			pauseButtonIsPause = !pauseButtonIsPause;
		});

		repeatButton.setOnAction(event -> {
			timeDurationField.setText(repeatDuration + "");
			PerformDuration performDuration = new PerformDuration(repeatDuration, this);
			performDuration.start();
		});

		minimizeButton.setOnAction(event -> {

			if (Preferences.getSystrayMode() == Preferences.SystrayMode.NOT_IN_SYSTRAY || !systray.hasSystray()) {
				Platform.runLater(() -> stage.setIconified(true));
				return;
			}

			stage.hide();
			systray.addSystray();

		});

		timeDurationField.setListener(alarmsComboBox, this);
	}

	Systray getSystray() {
		return systray;
	}

	private void handleMousePressed(MouseEvent event) {
		xOffset = event.getSceneX();
		yOffset = event.getSceneY();
	}

	private void handleMouseDragged(MouseEvent event) {
		final Stage tmpStage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
		tmpStage.setX(event.getScreenX() - xOffset);
		tmpStage.setY(event.getScreenY() - yOffset);
	}

	void show() {
		stage.show();
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

	String getDashForTitle() {
		switch (Asac.getOperationSystem()) {
		case KDE:
			return "⸺";
		case XFCE:
		case WINDOWS:
			return "-";
		default:
			return "-";
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

	boolean getPauseButtonIsPause() {
		return pauseButtonIsPause;
	}

	void setTitle(String title) {
		System.out.println("Holla 1 " + title);
		Platform.runLater(() -> stage.setTitle(title));
	}

	void setTimeDurationFieldText(final String text) {
		timeDurationField.protectedSetText(text);
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

	AlarmsComboBox getAlarmsComboBox() {
		return alarmsComboBox;
	}

	Alarm getStoredAlarm() {
		return alarmsComboBox.getStoredAlarm();
	}

	Stage getStage() {
		return stage;
	}

	Scene getScene() {
		return scene;
	}

	void setRepeatButton(long duration) {
		repeatDuration = duration;
		Platform.runLater(() -> {
			repeatButton.setText(messages.getString("MainClass.repeat.button.part1") + duration
					+ Asac.getMinuteString(duration) + messages.getString("MainClass.repeat.button.part2"));
			repeatButton.setVisible(duration >= 0);
		});
	}
}

class MyDurationPopupListener implements DurationPopupListener {
	final ContextMenu trayIconTimeMenu;
	final MainClass mainClass;
	final Popup sysTrayPopup;

	MyDurationPopupListener(ContextMenu popupMenu, MainClass mainClass, Popup sysTrayPopup) {
		this.trayIconTimeMenu = popupMenu;
		this.mainClass = mainClass;
		this.sysTrayPopup = sysTrayPopup;
	}

	@Override
	public void setMenuItem(int minute, String minutesString) {
		String menuItemText = minute + minutesString;
		final MenuItem menuItem = new MenuItem(menuItemText);
		menuItem.setOnAction(event -> {
			mainClass.setTimeDurationFieldText(minute + "");
			PerformDuration performDuration = new PerformDuration(minute, mainClass);
			performDuration.start();
			if (sysTrayPopup != null) {
				sysTrayPopup.hide();
			}
		});
		trayIconTimeMenu.getItems().addAll(menuItem);
	}

	@Override
	public void addSeparator() {
		trayIconTimeMenu.getItems().add(new SeparatorMenuItem());
	}

}
