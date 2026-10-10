package com.ralf.asac;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javafx.animation.Animation;
import javafx.animation.StrokeTransition;
import javafx.application.Platform;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Ellipse;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

class BellIcon {
	private final String name;
	private final AlarmSounds.AlarmSoundData alarmSoundData;

	AudioOutput audioOutput;
	private final String text;

	Stage stage;

	BellIcon(final String name, final AlarmSounds.AlarmSoundData alarmSoundData, String text) {
		this.name = name;
		this.alarmSoundData = alarmSoundData;
		this.text = text;
	}

	@SuppressWarnings({ "java:S4507" })
	void play() {
		final URL url = getURL();

		if (url != null) {
			audioOutput = new AudioOutput(url);
			audioOutput.play();
		}

		Platform.runLater(() -> {
			stage = new Stage();
			stage.initStyle(StageStyle.UNDECORATED);

			final List<Node> nodes = new ArrayList<>();

			int diameter = 140;

			Ellipse circle = new Ellipse(64, 64);
			circle.setStroke(Color.BLUE);
			circle.setStrokeWidth(8);
			circle.setFill(Color.WHITE);
			nodes.add(circle);

			Text javaFxText = new Text(text);
			Font font = new Font(javaFxText.getFont().getName(), 15);
			javaFxText.setFont(font);
			nodes.add(javaFxText);

			final StackPane stackPane = new StackPane();
			stackPane.setMinSize(diameter, diameter);
			stackPane.setPrefSize(diameter, diameter);
			stackPane.setMaxSize(diameter, diameter);
			stackPane.getChildren().addAll(nodes);

			StrokeTransition transition = new StrokeTransition(new Duration(100.), circle);

			transition.setFromValue(Color.RED);
			transition.setToValue(Color.BLUE);
			transition.setCycleCount(Animation.INDEFINITE);
			transition.setAutoReverse(true);
			transition.play();

			final VBox vBox = new VBox();

			if (name != null && !name.isBlank()) {
				final Label caption = new Label(name);
				vBox.getChildren().addAll(caption, stackPane);
			} else {
				vBox.getChildren().add(stackPane);
			}

			final Scene scene = new Scene(vBox);
			stage.setScene(scene);

			scene.setOnMouseClicked(event -> {
				audioOutput.stopPlaying();
				stage.close();
			});

			stage.setOnCloseRequest(event -> audioOutput.stopPlaying());

			stage.setAlwaysOnTop(true);
			stage.show();

			final Point2D point = Asac.getCoordinatesofMiddleOfTheScreen(stage);

			stage.setX(point.getX());
			stage.setY(point.getY() - 200); // 200 is for avoiding that this window is
											// overlapping the main window
		});
	}

	private URL getURL() {
		if (alarmSoundData == null || alarmSoundData.getPath() == null || alarmSoundData.getPath().isBlank()) {
			final AlarmSounds.AlarmSoundData defaultSound = Preferences.getDefaultSound();
			if (defaultSound.getName() != null) {
				return getURLbyFilePath(defaultSound.getPath());
			}
			return ClassLoader.getSystemResource(Asac.DEFAULT_SOUND_FILE);
		} else if (alarmSoundData.isResource()) {
			if (alarmSoundData.getName().equals(MainClass.messages.getString("path.default"))) {
				return ClassLoader.getSystemResource(Asac.DEFAULT_SOUND_FILE);
			}
			return ClassLoader.getSystemResource(alarmSoundData.getName());
		} else {
			return getURLbyFilePath(alarmSoundData.getPath());
		}
	}

	@SuppressWarnings("java:S4507")
	private URL getURLbyFilePath(final String filePath) {
		final File file = new File(filePath);
		if (!file.isFile()) {
			return ClassLoader.getSystemResource(Asac.DEFAULT_SOUND_FILE);
		} else {
			final URI uri = file.toURI();
			try {
				return uri.toURL();
			} catch (MalformedURLException exception) {
				exception.printStackTrace();
				return ClassLoader.getSystemResource(Asac.DEFAULT_SOUND_FILE);
			}
		}
	}
}
