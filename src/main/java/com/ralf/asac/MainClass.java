package com.ralf.asac;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Locale;
import java.util.ResourceBundle;

import javafx.application.Application;
import javafx.application.Platform;
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

public class MainClass extends Application {
	private Stage stage;
	private Scene scene;

	private final MainPanel mainPanel;
//	private final AlarmsComboBox alarmsComboBox;
	// private final TimeDurationField timeDurationField;
//	PerformTime oldPerformTime;
//	PerformDuration oldPerformDuration;
	private final Button minimizeButton = new Button("_");
	private final Button finishButton = new Button("✕");
//	private final Button deactivateButton;
//	private final Button pauseButton;
//	private boolean pauseButtonIsPause;
//	private final Button repeatButton;
//	private RepeatAlarmData repeatAlarmData;
//	private final DurationButton durationButton;
//	private final TimeButton timeButton;

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
		mainPanel = new MainPanel(this);
//		timeDurationField = mainPanel.getTimeDurationField();
//		alarmsComboBox = mainPanel.getAlarmsComboBox();
//		deactivateButton = mainPanel.getDeactivateButton();
//		pauseButton = mainPanel.getPauseButton();
//		repeatButton = mainPanel.getRepeatButton();
//		durationButton = mainPanel.getDurationButton();
//		timeButton = mainPanel.getTimeButton();
	}

	@SuppressWarnings({ "exports", "java:S3776", "java:S4507" })
	@Override
	public void start(final Stage stage) throws Exception {
		this.stage = stage;
		mainPanel.init(getTitlePane(), null);

	}

//	@SuppressWarnings({ "exports", "java:S3776", "java:S4507" })
//	@Override
//	public void start(final Stage stage) throws Exception {
//
//		this.stage = stage;
//		stage.initStyle(StageStyle.UNDECORATED);
//		setIcon(false);
//
//		mainPanel.init(getTitlePane(), null);
//		mainPanel.getPane().setStyle("-fx-border-color: black; -fx-border-style: solid;");
//
//		durationButton.init(this.getMainPanel());
//		timeButton.init(this);
//
//		alarmsComboBox.initialize(this, timeDurationField);
//		alarmsComboBox.showStoredAlarms();
//
//		deactivateButton.setVisible(false);
//
//		final Pane pane = mainPanel.getPane();
//		scene = new Scene(pane);
//
//		setTitle(messages.getString("title"));
//		stage.setScene(scene);
//		stage.show();
//
//		final Point2D point = Asac.getCoordinatesofMiddleOfTheScreen(stage);
//		stage.setX(point.getX());
//		stage.setY(point.getY());
//
//		stage.setOnCloseRequest(event -> mainPanel.deactivate());
//
//		finishButton.setOnAction(event -> System.exit(0));
//		Platform.setImplicitExit(false);
//
//		systray = new Systray(this);
//
//		pane.setOnMousePressed(this::handleMousePressed);
//		pane.setOnMouseDragged(this::handleMouseDragged);
//
//		alarmsComboBox.setAlarmsComboBoxToBeSynchronize(systray.getAlarmsComboBox());
//		setListener();
//	}

	MainPanel getMainPanel() {
		return mainPanel;
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
		mainPanel.getAlarmsComboBox().clear();
	}
//
//	void deactivate() {
//		alarmsComboBox.getEditor().setText("");
//		timeDurationField.setText("");
//		setTitle(messages.getString("title"));
//
//		pauseButton.setVisible(false);
//		pauseButton.setText(messages.getString(PAUSE_KEY));
//
//		systray.pauseButton.setVisible(false);
//		systray.pauseButton.setText(messages.getString(PAUSE_KEY));
//
//		pauseButtonIsPause = false;
//
//		if (oldPerformTime != null) {
//			oldPerformTime.stop();
//		}
//		oldPerformTime = null;
//		if (oldPerformDuration != null) {
//			oldPerformDuration.stop();
//		}
//		oldPerformDuration = null;
//		deactivateButton.setVisible(false);
//
//		if (bellIcon != null) {
//			if (bellIcon.stage != null) {
//				bellIcon.stage.hide();
//				bellIcon.stage = null;
//			}
//			if (bellIcon.audioOutput != null) {
//				bellIcon.audioOutput.stopPlaying();
//			}
//			bellIcon = null;
//		}
//
//		setIcon(false);
//
//	}

//	@SuppressWarnings("java:S6208")
//	String getDashForTitle() {
//		switch (Asac.getOperationSystem()) {
//		case KDE:
//			return "⸺";
//		case XFCE:
//		case WINDOWS:
//			return "-";
//		default:
//			return "-";
//		}
//	}
//
//	@SuppressWarnings("java:S6201")
//	String getName() {
//		return alarmsComboBox.getName();
//	}
//
//	void resetStoredAlarmsVaLue() {
//		alarmsComboBox.setValue(null);
//	}

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
//
//	boolean getPauseButtonIsPause() {
//		return pauseButtonIsPause;
//	}

	void setTitle(String title) {
		Platform.runLater(() -> stage.setTitle(title));
	}

	void setTimeDurationFieldText(final String text) {
		mainPanel.timeDurationField.protectedSetText(text);
	}

//	void setVisibilityDeactivateButton(final boolean visibility) {
//		Platform.runLater(() -> {
//			deactivateButton.setVisible(visibility);
//			systray.deactivateButton.setVisible(visibility);
//		});
//	}
//
//	void deactivatePauseButton() {
//		Platform.runLater(() -> {
//			pauseButton.setVisible(false);
//			pauseButton.setText(messages.getString(PAUSE_KEY));
//			systray.pauseButton.setVisible(false);
//			systray.pauseButton.setText(messages.getString(PAUSE_KEY));
//		});
//	}
//
//	void setVisibilityPauseButton(final boolean visibility) {
//		Platform.runLater(() -> {
//			pauseButton.setVisible(visibility);
//			systray.pauseButton.setVisible(visibility);
//		});
//	}
//
//	AlarmsComboBox getAlarmsComboBox() {
//		return alarmsComboBox;
//	}
//
//	Alarm getStoredAlarm() {
//		return alarmsComboBox.getStoredAlarm();
//	}

	Stage getStage() {
		return stage;
	}

	Scene getScene() {
		return scene;
	}

//	void setRepeatButton(RepeatAlarmData repeatAlarmData) {
//		if (repeatAlarmData != null) {
//			this.repeatAlarmData = repeatAlarmData;
//			Platform.runLater(() -> {
//				String text = messages.getString("MainClass.repeat.button.part1") + repeatAlarmData.duration
//						+ Asac.getMinuteString(repeatAlarmData.duration)
//						+ messages.getString("MainClass.repeat.button.part2");
//
//				repeatButton.setText(text);
//				repeatButton.setVisible(repeatAlarmData.duration >= 0);
//
//				systray.repeatButton.setText(text);
//				systray.repeatButton.setVisible(repeatAlarmData.duration >= 0);
//			});
//		}
//	}

//	private void processOnActionAlarmsComboBox() {
//		alarmsComboBox.showStoredAlarms();
//	}

//	private void processOnActionDeactivateButton() {
//		deactivate();
//		alarmsComboBox.setValue(null);
//	}

//	private void processOnActionPauseButton() {
//		final String text = messages.getString(pauseButtonIsPause ? PAUSE_KEY : "MainClass.continue");
//		pauseButton.setText(text);
//		systray.pauseButton.setText(text);
//		pauseButtonIsPause = !pauseButtonIsPause;
//	}

//	private void processOnActionAlarmManagerButton() {
//		new AlarmManager(this);
//	}

//	private void processOnActionRepeatButton() {
//		timeDurationField.setText(repeatAlarmData.duration + "");
//
//		alarmsComboBox.protectedSetValue(repeatAlarmData.alarmComboBox);
//		systray.alarmsComboBox.protectedSetValue(repeatAlarmData.alarmComboBox);
//		PerformDuration performDuration = new PerformDuration(0, repeatAlarmData, this);
//		performDuration.start();
//	}

	MainPanel getCommonPanel() {
		return mainPanel;
	}
}

class MyDurationPopupListener implements DurationPopupListener {
	final ContextMenu trayIconTimeMenu;
	final MainPanel mainPanel;
	final Popup sysTrayPopup;

	MyDurationPopupListener(ContextMenu popupMenu, MainPanel mainPanel, Popup sysTrayPopup) {
		this.trayIconTimeMenu = popupMenu;
		this.mainPanel = mainPanel;
		this.sysTrayPopup = sysTrayPopup;
	}

	@Override
	public void setMenuItem(final int minute, final String minutesString) {
		final String menuItemText = minute + minutesString;
		final MenuItem menuItem = new MenuItem(menuItemText);
		menuItem.setOnAction(event -> {
			mainPanel.setTimeDurationFieldText(minute + "");
			PerformDuration performDuration = new PerformDuration(minute, null, mainPanel);
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
