package com.ralf.asac;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Locale;
import java.util.ResourceBundle;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
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
	private boolean pauseButtonIsPause;
	private final Button repeatButton;
	private RepeatAlarmData repeatAlarmData;
	private final DurationButton durationButton;
	private final TimeButton timeButton;

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
		commonPanel = new MainPanel(this);
		timeDurationField = commonPanel.getTimeDurationField();
		alarmsComboBox = commonPanel.getAlarmsComboBox();
		deactivateButton = commonPanel.getDeactivateButton();
		pauseButton = commonPanel.getPauseButton();
		repeatButton = commonPanel.getRepeatButton();
		durationButton = commonPanel.getDurationButton();
		timeButton = commonPanel.getTimeButton();
	}

	@SuppressWarnings({ "exports", "java:S3776", "java:S4507" })
	@Override
	public void start(final Stage stage) throws Exception {

		this.stage = stage;
		stage.initStyle(StageStyle.UNDECORATED);
		setIcon(false);

		commonPanel.init(getTitlePane());
		commonPanel.getPane().setStyle("-fx-border-color: black; -fx-border-style: solid;");

		durationButton.init(this);
		timeButton.init(this);

		alarmsComboBox.initialize(this, timeDurationField);
		alarmsComboBox.showStoredAlarms();

		deactivateButton.setVisible(false);

		final Pane pane = commonPanel.getPane();
		scene = new Scene(pane);

		setTitle(messages.getString("title"));
		stage.setScene(scene);
		stage.show();

		final Point2D point = Asac.getCoordinatesofMiddleOfTheScreen(stage);
		stage.setX(point.getX());
		stage.setY(point.getY());

		stage.setOnCloseRequest(event -> deactivate());

		finishButton.setOnAction(event -> System.exit(0));
		Platform.setImplicitExit(false);

		systray = new Systray(this);

		pane.setOnMousePressed(this::handleMousePressed);
		pane.setOnMouseDragged(this::handleMouseDragged);

		alarmsComboBox.setAlarmsComboBoxToBeSynchronize(systray.getAlarmsComboBox());
		setListener();
	}

	private Pane getTitlePane() {

		final BorderPane titlePane = new BorderPane();
		final HBox hBoxTitleBar = new HBox();
		hBoxTitleBar.setAlignment(Pos.CENTER_RIGHT);
		hBoxTitleBar.setMaxWidth(Double.MAX_VALUE);

		hBoxTitleBar.getChildren().addAll(minimizeButton, finishButton);
		titlePane.setRight(hBoxTitleBar);

		return titlePane;
	}

	private void setListener() {

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
		alarmsComboBox.getEditor().setText("");
		timeDurationField.setText("");
		setTitle(messages.getString("title"));

		pauseButton.setVisible(false);
		pauseButton.setText(messages.getString(PAUSE_KEY));

		systray.pauseButton.setVisible(false);
		systray.pauseButton.setText(messages.getString(PAUSE_KEY));

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

	@SuppressWarnings("java:S6208")
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
		Platform.runLater(() -> stage.setTitle(title));
	}

	void setTimeDurationFieldText(final String text) {
		timeDurationField.protectedSetText(text);
	}

	void setVisibilityDeactivateButton(final boolean visibility) {
		Platform.runLater(() -> {
			deactivateButton.setVisible(visibility);
			systray.deactivateButton.setVisible(visibility);
		});
	}

	void deactivatePauseButton() {
		Platform.runLater(() -> {
			pauseButton.setVisible(false);
			pauseButton.setText(messages.getString(PAUSE_KEY));
			systray.pauseButton.setVisible(false);
			systray.pauseButton.setText(messages.getString(PAUSE_KEY));
		});
	}

	void setVisibilityPauseButton(final boolean visibility) {
		Platform.runLater(() -> {
			pauseButton.setVisible(visibility);
			systray.pauseButton.setVisible(visibility);
		});
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

	void setRepeatButton(RepeatAlarmData repeatAlarmData) {
		if (repeatAlarmData != null) {
			this.repeatAlarmData = repeatAlarmData;
			Platform.runLater(() -> {
				String text = messages.getString("MainClass.repeat.button.part1") + repeatAlarmData.duration
						+ Asac.getMinuteString(repeatAlarmData.duration)
						+ messages.getString("MainClass.repeat.button.part2");

				repeatButton.setText(text);
				repeatButton.setVisible(repeatAlarmData.duration >= 0);

				systray.repeatButton.setText(text);
				systray.repeatButton.setVisible(repeatAlarmData.duration >= 0);
			});
		}
	}

	void processOnActionAlarmsComboBox() {
		alarmsComboBox.showStoredAlarms();
	}

	void processOnActionDeactivateButton() {
		deactivate();
		alarmsComboBox.setValue(null);
	}

	void processOnActionPauseButton() {
		final String text = messages.getString(pauseButtonIsPause ? PAUSE_KEY : "MainClass.continue");
		pauseButton.setText(text);
		systray.pauseButton.setText(text);
		pauseButtonIsPause = !pauseButtonIsPause;
	}

	void processOnActionAlarmManagerButton() {
		new AlarmManager(this);
	}

	void processOnActionRepeatButton() {
		timeDurationField.setText(repeatAlarmData.duration + "");

		alarmsComboBox.protectedSetValue(repeatAlarmData.alarmComboBox);
		systray.alarmsComboBox.protectedSetValue(repeatAlarmData.alarmComboBox);
		PerformDuration performDuration = new PerformDuration(0, repeatAlarmData, this);
		performDuration.start();
	}

	CommonPanel getCommonPanel() {
		return commonPanel;
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
	public void setMenuItem(final int minute, final String minutesString) {
		final String menuItemText = minute + minutesString;
		final MenuItem menuItem = new MenuItem(menuItemText);
		menuItem.setOnAction(event -> {
			mainClass.setTimeDurationFieldText(minute + "");
			PerformDuration performDuration = new PerformDuration(minute, null, mainClass);
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

class RepeatAlarmData {
	final long duration;
	final AlarmSounds.AlarmSoundData alarmSoundData;
	final Alarm alarmComboBox;

	RepeatAlarmData(final long duration, final AlarmSounds.AlarmSoundData alarmSoundData, final Alarm alarmComboBox) {
		this.duration = duration;
		this.alarmSoundData = alarmSoundData;
		this.alarmComboBox = alarmComboBox;
	}
}
