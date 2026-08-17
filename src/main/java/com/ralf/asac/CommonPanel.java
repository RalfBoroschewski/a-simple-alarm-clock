package com.ralf.asac;

import javafx.scene.control.Button;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;

public abstract class CommonPanel {

	static final String PAUSE_KEY = "MainClass.pause";

	private final boolean hasExitButtom;

	protected final AlarmsComboBox alarmsComboBox;
	protected final TimeDurationField timeDurationField;
	protected final Button deactivateButton;
	protected final Button pauseButton;
	protected final Button alarmManagerButton;
	protected final Button repeatButton;

	protected final DurationButton durationButton = new DurationButton();
	protected final TimeButton timeButton = new TimeButton();

	private GridPane gridPane;

	CommonPanel(final boolean hasExitButtom) {
		this.hasExitButtom = hasExitButtom;
		timeDurationField = new TimeDurationField();
		alarmsComboBox = new AlarmsComboBox();
		deactivateButton = new Button(MainClass.messages.getString("CommonPanel.deactivate"));
		pauseButton = new Button(MainClass.messages.getString(PAUSE_KEY));
		repeatButton = new Button();
		alarmManagerButton = new Button(MainClass.messages.getString("CommonPanel.alarm.manager"));
	}

	void init(final Pane titlePane) {

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

		gridPane = new GridPane();
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

		if (hasExitButtom) {
			positionY++;
			final Button exitButton = new Button(MainClass.messages.getString("CommonPanel.exit"));
			gridPane.add(exitButton, positionX, positionY, 1, 1);
			exitButton.setOnAction(event -> System.exit(0));
		}

		setListener();
	}

	private void setListener() {
		deactivateButton.setOnAction(event -> processOnActionDeactivateButton());
		pauseButton.setOnAction(event -> processOnActionPauseButton());
		alarmManagerButton.setOnAction(event -> processOnActionAlarmManagerButton());
		repeatButton.setOnAction(event -> processOnActionRepeatButton());
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

	abstract void processOnActionDeactivateButton();

	abstract void processOnActionPauseButton();

	abstract void processOnActionAlarmManagerButton();

	abstract void processOnActionRepeatButton();

}
