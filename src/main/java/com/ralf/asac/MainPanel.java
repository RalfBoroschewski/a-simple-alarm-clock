package com.ralf.asac;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.geometry.Point2D;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

public class MainPanel {
	private final MainClass mainClass;

	private Stage stage;
	private Scene scene;
	static final String PAUSE_KEY = "MainClass.pause";

	private final AlarmsComboBox alarmsComboBox;
	final TimeDurationField timeDurationField;

	PerformTime oldPerformTime;
	PerformDuration oldPerformDuration;

	private final Button deactivateButton;
	private final Button pauseButton;
	private boolean pauseButtonIsPause;
	private final Button alarmManagerButton;
	private final Button repeatButton;
	private RepeatAlarmData repeatAlarmData;

	protected final DurationButton durationButton = new DurationButton();
	protected final TimeButton timeButton = new TimeButton();

	BellIcon bellIcon;

	private GridPane gridPane;

	private double xOffset;
	private double yOffset;

	private boolean isSystray;
	private double xPosition;
	private double yPosition;

	private boolean hasFocusedPropertyListener;

	MainPanel(final MainClass mainClass) {
		this.mainClass = mainClass;
		timeDurationField = new TimeDurationField();
		alarmsComboBox = new AlarmsComboBox();
		deactivateButton = new Button(MainClass.messages.getString("MainPanel.deactivate"));
		pauseButton = new Button(MainClass.messages.getString(PAUSE_KEY));
		repeatButton = new Button();
		alarmManagerButton = new Button(MainClass.messages.getString("MainPanel.alarm.manager"));

	}

	void init(final Pane titlePane, final Pane bottomPane, boolean isSystray) {
		this.isSystray = isSystray;

		if (gridPane == null) {
			gridPane = new GridPane();
			scene = new Scene(gridPane);
			stage = mainClass.getStage();
			stage.setScene(scene);
			stage.initStyle(StageStyle.UNDECORATED);
//			gridPane.setStyle("-fx-border-color: black; -fx-border-style: solid;");
			setListener(gridPane);
			gridPane.setStyle("-fx-background-color: white;");
			durationButton.init(mainClass);
			timeButton.init(mainClass);
			alarmsComboBox.initialize(mainClass, timeDurationField);

			pauseButton.setVisible(false);
			repeatButton.setVisible(false);
			deactivateButton.setVisible(false);

			final double width = Double.parseDouble(MainClass.messages.getString("MainPanel.buttons.width"));
			alarmsComboBox.setPrefWidth(width);
			alarmManagerButton.setPrefWidth(width);
			durationButton.setPrefWidth(width);
			timeButton.setPrefWidth(width);
			timeDurationField.setPrefWidth(width);
			deactivateButton.setPrefWidth(width);
			setTitle(MainClass.messages.getString("title"));

			setIcon(false);

		} else {
			gridPane.getChildren().clear();
		}

		int positionX = 0;
		int positionY = 0;

		if (titlePane != null) {
			// positionX++;
			gridPane.add(titlePane, positionX, positionY, 2, 1);
		}

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

		gridPane.add(repeatButton, positionX, positionY, 2, 1);

		if (bottomPane != null) {
			positionY++;
			gridPane.add(bottomPane, positionX, positionY, 1, 1);
		}

	}

	void update(Point2D point, boolean hasFocusedPropertyListener) {
		System.out.println("Holla 20 " + point);
		this.hasFocusedPropertyListener = hasFocusedPropertyListener;

		alarmsComboBox.showStoredAlarms();
		if (point != null) {
			stage.setX(point.getX());
			stage.setY(point.getY());
			System.out.println("Holla 21");
			xPosition = point.getX();
			yPosition = point.getY();
		}
		System.out.println("Holla 22 " + stage.getX());
		System.out.println("Holla 23 " + stage.getY());
		stage.show();
	}

	private void setListener(GridPane gridPane) {

		timeDurationField.setListener(alarmsComboBox, mainClass);

		deactivateButton.setOnAction(event -> {
			deactivate();
			alarmsComboBox.setValue(null);
		});

		pauseButton.setOnAction(event -> {
			final String text = MainClass.messages.getString(pauseButtonIsPause ? PAUSE_KEY : "MainClass.continue");
			pauseButton.setText(text);
			pauseButtonIsPause = !pauseButtonIsPause;
		});

		alarmManagerButton.setOnAction(event -> new AlarmManager(mainClass));

		repeatButton.setOnAction(event -> {
			timeDurationField.setText(repeatAlarmData.duration + "");

			alarmsComboBox.protectedSetValue(repeatAlarmData.alarmComboBox);
			PerformDuration performDuration = new PerformDuration(0, repeatAlarmData, mainClass);
			performDuration.start();
		});

		gridPane.setOnMousePressed(this::handleMousePressed);
		gridPane.setOnMouseDragged(this::handleMouseDragged);

		stage.focusedProperty().addListener((obs, oldValue, focused) -> {
			if (!hasFocusedPropertyListener) {
				return;
			}

			if (focused == null || !focused) {
				final PauseTransition delay = new PauseTransition(Duration.millis(100));

				delay.setOnFinished(event -> {
					if (!stage.isFocused()) {
						stage.hide();
					}
				});

				delay.play();
			}
		});
	}

	private void handleMousePressed(MouseEvent event) {
		xOffset = event.getSceneX();
		yOffset = event.getSceneY();
	}

	private void handleMouseDragged(MouseEvent event) {
		final Stage tmpStage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
		tmpStage.setX(event.getScreenX() - xOffset);
		tmpStage.setY(event.getScreenY() - yOffset);

		if (!isSystray) {
			xPosition = stage.getX();
			yPosition = stage.getY();
			System.out.println("Hallo 1 " + xPosition);
			System.out.println("Hallo 2 " + yPosition);
		}
	}

	void deactivate() {
		alarmsComboBox.getEditor().setText("");
		timeDurationField.setText("");
		setTitle(MainClass.messages.getString("title"));

		pauseButton.setVisible(false);
		pauseButton.setText(MainClass.messages.getString(PAUSE_KEY));

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

	void restorePosition() {
		System.out.println("Hallo 3");
		// if (!isSystray) {
		System.out.println("Hallo 4 " + xPosition);
		System.out.println("Hallo 5 " + yPosition);
		stage.setX(xPosition);
		stage.setY(yPosition);
		// }
	}

	Pane getPane() {
		return gridPane;
	}

	TimeDurationField getTimeDurationField() {
		return timeDurationField;
	}

	AlarmsComboBox getAlarmsComboBox() {
		return alarmsComboBox;
	}

	Button getDeactivateButton() {
		return deactivateButton;
	}

	Button getPauseButton() {
		return pauseButton;
	}

	Button getRepeatButton() {
		return repeatButton;
	}

	Button getAlarmManagerButton() {
		return alarmManagerButton;
	}

	DurationButton getDurationButton() {
		return durationButton;
	}

	TimeButton getTimeButton() {
		return timeButton;
	}

	boolean getPauseButtonIsPause() {
		return pauseButtonIsPause;
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

	void setTitle(String title) {
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
			pauseButton.setText(MainClass.messages.getString(PAUSE_KEY));
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

	Scene getScene() {
		return scene;
	}

	void setRepeatButton(RepeatAlarmData repeatAlarmData) {
		if (repeatAlarmData != null) {
			this.repeatAlarmData = repeatAlarmData;
			Platform.runLater(() -> {
				String text = MainClass.messages.getString("MainClass.repeat.button.part1") + repeatAlarmData.duration
						+ Asac.getMinuteString(repeatAlarmData.duration)
						+ MainClass.messages.getString("MainClass.repeat.button.part2");

				repeatButton.setText(text);
				repeatButton.setVisible(repeatAlarmData.duration >= 0);

			});
		}
	}
}
