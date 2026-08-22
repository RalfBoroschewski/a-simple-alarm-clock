package com.ralf.asac;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

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

public class MainPanel {
	private final MainClass mainClass;

	private Stage stage;
	private Scene scene;
	static final String PAUSE_KEY = "MainClass.pause";

//	private final boolean hasExitButtom;

	protected final AlarmsComboBox alarmsComboBox;
	protected final TimeDurationField timeDurationField;

	PerformTime oldPerformTime;
	PerformDuration oldPerformDuration;

	protected final Button deactivateButton;
	protected final Button pauseButton;
	private boolean pauseButtonIsPause;
	protected final Button alarmManagerButton;
	protected final Button repeatButton;
	private RepeatAlarmData repeatAlarmData;

	protected final DurationButton durationButton = new DurationButton();
	protected final TimeButton timeButton = new TimeButton();

	BellIcon bellIcon;

	private GridPane gridPane;

	private double xOffset = 0;
	private double yOffset = 0;

	MainPanel(final MainClass mainClass) {
		this.mainClass = mainClass;
		// this.hasExitButtom = hasExitButtom;
		timeDurationField = new TimeDurationField();
		alarmsComboBox = new AlarmsComboBox();
		deactivateButton = new Button(MainClass.messages.getString("CommonPanel.deactivate"));
		pauseButton = new Button(MainClass.messages.getString(PAUSE_KEY));
		repeatButton = new Button();
		alarmManagerButton = new Button(MainClass.messages.getString("CommonPanel.alarm.manager"));
	}

	void init(final Pane titlePane, final Pane bottomPane) {
		stage = mainClass.getStage();
		pauseButton.setVisible(false);
		repeatButton.setVisible(false);
		deactivateButton.setVisible(false);

		final double width = Double.parseDouble(MainClass.messages.getString("CommonPanel.buttons.width"));
		alarmsComboBox.setPrefWidth(width);
		alarmManagerButton.setPrefWidth(width);
		durationButton.setPrefWidth(width);
		timeButton.setPrefWidth(width);
		timeDurationField.setPrefWidth(width);
		deactivateButton.setPrefWidth(width);

		if (gridPane == null) {
			gridPane = new GridPane();
		} else {
			gridPane.getChildren().clear();
		}

		gridPane.setStyle("-fx-background-color: white;");

		int positionX = 0;
		int positionY = 0;

		if (titlePane != null) {
			positionX++;
			gridPane.add(titlePane, positionX, positionY, 1, 1);
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
			final Button exitButton = new Button(MainClass.messages.getString("CommonPanel.exit"));
			gridPane.add(bottomPane, positionX, positionY, 1, 1);
			exitButton.setOnAction(event -> System.exit(0));
		}

		scene = new Scene(gridPane);
		setTitle(MainClass.messages.getString("title"));
		stage.setScene(scene);

		stage.initStyle(StageStyle.UNDECORATED);
		setIcon(false);

		stage.show();

		durationButton.init(this);
		timeButton.init(this);

		alarmsComboBox.initialize(this, timeDurationField);
		alarmsComboBox.showStoredAlarms();

		final Point2D point = Asac.getCoordinatesofMiddleOfTheScreen(stage);
		stage.setX(point.getX());
		stage.setY(point.getY());

		setListener(gridPane);
	}

	private void setListener(GridPane gridPane) {

		timeDurationField.setListener(alarmsComboBox, this);

		deactivateButton.setOnAction(event -> {
			deactivate();
			alarmsComboBox.setValue(null);
		});

		pauseButton.setOnAction(event -> {
			final String text = MainClass.messages.getString(pauseButtonIsPause ? PAUSE_KEY : "MainClass.continue");
			pauseButton.setText(text);
			pauseButtonIsPause = !pauseButtonIsPause;
		});

		alarmManagerButton.setOnAction(event -> {
			new AlarmManager(mainClass);
		});

		repeatButton.setOnAction(event -> {
			timeDurationField.setText(repeatAlarmData.duration + "");

			alarmsComboBox.protectedSetValue(repeatAlarmData.alarmComboBox);
			PerformDuration performDuration = new PerformDuration(0, repeatAlarmData, this);
			performDuration.start();
		});

		gridPane.setOnMousePressed(this::handleMousePressed);
		gridPane.setOnMouseDragged(this::handleMouseDragged);
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
		Platform.runLater(() -> {
			deactivateButton.setVisible(visibility);
		});
	}

	void deactivatePauseButton() {
		Platform.runLater(() -> {
			pauseButton.setVisible(false);
			pauseButton.setText(MainClass.messages.getString(PAUSE_KEY));
		});
	}

	void setVisibilityPauseButton(final boolean visibility) {
		Platform.runLater(() -> {
			pauseButton.setVisible(visibility);
		});
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

//	
//	
//	
//	
//	
//
//	void processOnActionDeactivateButton() {
//		mainClass.processOnActionDeactivateButton();
//	}
//
//	void processOnActionPauseButton() {
//		mainClass.processOnActionPauseButton();
//	}
//
//	void processOnActionAlarmManagerButton() {
//		mainClass.processOnActionAlarmManagerButton();
//	}
//
//	void processOnActionRepeatButton() {
//		mainClass.processOnActionRepeatButton();
//	}

}
