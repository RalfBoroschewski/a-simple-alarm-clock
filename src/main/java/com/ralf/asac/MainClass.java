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
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

public class MainClass extends Application {
	private Stage stage;
	private Scene scene;

	private final MainPanel mainPanel;
	private final Button minimizeButton = new Button("_");
	private final Button finishButton = new Button("✕");

	BellIcon bellIcon;

	static final ResourceBundle messages = ResourceBundle.getBundle("messages", Locale.getDefault());
	static final String PAUSE_KEY = "MainClass.pause";

	private Systray systray;

	static void start(final String[] args) {
		launch(args);
	}

	public MainClass() {
		mainPanel = new MainPanel(this);
	}

	@SuppressWarnings({ "exports", "java:S3776", "java:S4507" })
	@Override
	public void start(final Stage stage) throws Exception {
		this.stage = stage;
		mainPanel.init(getTitlePane(), null, false);
		update();
		setListener();
		Platform.setImplicitExit(false); // enables mouse clicks in Systray
		systray = new Systray(this);
	}

	private void update() {
		final Point2D point = Asac.getCoordinatesofMiddleOfTheScreen(stage);
		stage.setX(point.getX());
		stage.setY(point.getY());
		mainPanel.update(point, false);
	}

	MainPanel getMainPanel() {
		return mainPanel;
	}

	Pane getTitlePane() {

		final BorderPane titlePane = new BorderPane();
		final HBox hBoxTitleBar = new HBox();
		hBoxTitleBar.setAlignment(Pos.CENTER_RIGHT);
		hBoxTitleBar.setMaxWidth(Double.MAX_VALUE);

		hBoxTitleBar.getChildren().addAll(new Label("Asac                                 "), minimizeButton,
				finishButton);
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

		finishButton.setOnAction(event -> System.exit(0));
	}

	Systray getSystray() {
		return systray;
	}

	void show() {
		stage.show();
	}

	void clearAlarmsComboBox() {
		mainPanel.getAlarmsComboBox().clear();
	}

	@SuppressWarnings("java:S4507")
	void setIcon(final boolean isActive) {

		Image image = readImage(isActive ? "alarmActive.png" : "alarm.png");

		if (image != null) {
			stage.getIcons().clear();
			stage.getIcons().add(image);
		}
	}

	@SuppressWarnings("java:S4507")
	Image readImage(String name) {
		final URL url = ClassLoader.getSystemResource(name);
		Image image = null;
		try {
			final InputStream inputStream = url.openStream();
			image = new Image(inputStream);
		} catch (IOException exception) {
			exception.printStackTrace();
		}
		return image;
	}

	void setTitle(String title) {
		Platform.runLater(() -> stage.setTitle(title));
	}

	void setTimeDurationFieldText(final String text) {
		mainPanel.timeDurationField.protectedSetText(text);
	}

	Stage getStage() {
		return stage;
	}

	Scene getScene() {
		return scene;
	}

	MainPanel getCommonPanel() {
		return mainPanel;
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
