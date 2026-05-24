package com.ralf.asac;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Locale;
import java.util.ResourceBundle;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.Duration;

public class MainClass extends Application {
	private Stage stage;

	private AlarmsComboBox alarmsComboBox;
	private TimeDurationField timeDurationField;
	private boolean timeDurationFieldIsSetInternal;
	PerformTime oldPerformTime;
	PerformDuration oldPerformDuration;
	private Button deactivateButton;
	private Button pauseButton;
	boolean pauseButtonIsPause;

	BellIcon bellIcon;

	static final ResourceBundle messages = ResourceBundle.getBundle("messages", Locale.getDefault());
	static final String PAUSE_KEY = "MainClass.pause";

	public static void main(String[] args) {
		launch(args);
	}

	@SuppressWarnings({ "exports", "java:S3776" })
	@Override
	public void start(final Stage stage) throws Exception {

		this.stage = stage;

		setIcon(false);

		Pane pane = new Pane();

		pane.getChildren();

		final Button alarmManagerButton = new Button(messages.getString("MainClass.alarm.manager"));
		final DurationButton durationButton = new DurationButton(this);
		final TimeButton timeButton = new TimeButton(this);
		timeDurationField = new TimeDurationField();
		deactivateButton = new Button(messages.getString("MainClass.deactivate"));
		pauseButton = new Button(messages.getString(PAUSE_KEY));
		pauseButton.setVisible(false);

		alarmsComboBox = new AlarmsComboBox(this, timeDurationField);
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

		alarmManagerButton.setOnAction(_ -> {
			new AlarmManager(this);
			showStoredAlarms();
		});

		timeDurationField.setOnAction(_ -> evaluateTimeDurationField());

		deactivateButton.setVisible(false);
		deactivateButton.setOnAction(_ -> deactivate());

		pauseButton.setOnAction(_ -> {
			pauseButton.setText(messages.getString(pauseButtonIsPause ? PAUSE_KEY : "MainClass.continue"));
			pauseButtonIsPause = !pauseButtonIsPause;
		});

		final Scene scene = new Scene(gridPane);
		stage.setScene(scene);
		stage.show();

		final Rectangle2D primScreenBounds = Screen.getPrimary().getVisualBounds();

		final double stageWidth = stage.getWidth();
		final double stageHeight = stage.getHeight();
		double windowsPositionX = (primScreenBounds.getWidth() - stageWidth) / 2;
		double windowsPositionY = (primScreenBounds.getHeight() - stageHeight) / 2;
		stage.setX(windowsPositionX);
		stage.setY(windowsPositionY);

		stage.setOnCloseRequest(_ -> deactivate());
	}

	void showStoredAlarms() {
		System.out.println("Hallo 4");
		final ArrayList<AlarmManager.AlarManagerItem> items = Preferences.getAlarms();

		final ArrayList<Alarm> tmpStoredAlarms = new ArrayList<>();
		for (AlarmManager.AlarManagerItem item : items) {
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
		System.out.println("Hallo 3");
		alarmsComboBox.clear();
	}

	void deactivate() {
		if (!alarmsComboBox.isTextFieldEdited())
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
				PerformDuration performDuration = new PerformDuration(minute, this);
				performDuration.handle(null);
			} else {
				final String hourString = timeDuration.substring(0, colonIndex);
				final String minuteString = timeDuration.substring(colonIndex + 1);
				final int hour = Integer.parseInt(hourString);
				final int minute = Integer.parseInt(minuteString);
				PerformTime performTime = new PerformTime(hour, minute, this);
				performTime.handle(null);
			}
		}
	}

	@SuppressWarnings("java:S6201")
	String getName() {
		System.out.println("Hallo 1 " + alarmsComboBox.getName());
		return alarmsComboBox.getName();
	}

	void resetStoredAlarmsVaLue() {
		System.out.println("Hallo 2");
		alarmsComboBox.setValue(null);
	}

	void setIcon(boolean isActive) {

		final URL url = ClassLoader.getSystemResource(isActive ? "alarmActive.png" : "alarm.png");
		Image image = null;
		try {
			InputStream inputStream = url.openStream();
			image = new Image(inputStream);
		} catch (IOException e) {
			e.printStackTrace();
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
			pauseButton.setText("Pause");
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
