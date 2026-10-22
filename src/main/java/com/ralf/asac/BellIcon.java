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
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
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
	private final String timeDurationText;

	Stage stage;

	BellIcon(final String name, final AlarmSounds.AlarmSoundData alarmSoundData, String timeDurationText) {
		this.name = name;
		this.alarmSoundData = alarmSoundData;
		this.timeDurationText = timeDurationText;
	}

	@SuppressWarnings({ "java:S4507" })
	void play() {

		Platform.runLater(() -> {
			final URL url = getURL();

			if (url != null) {
				audioOutput = new AudioOutput(url);
				audioOutput.play();
			}
			stage = new Stage();
			stage.initStyle(StageStyle.TRANSPARENT);

			final List<Node> nodes = new ArrayList<>();

			final int diameter = 140;

			final double circleStrokeWidth = 8;

			final Ellipse circle = new Ellipse(64, 64);
			circle.setStroke(Color.BLUE);
			circle.setStrokeWidth(circleStrokeWidth);
			circle.setFill(Color.WHITE);
			nodes.add(circle);

			final Font font = new Font(new Text("Dummy").getFont().getName(), 15);
			TextsToBeShown textsToBeShown = getTexts(name, timeDurationText, diameter, circleStrokeWidth, font);

			final Text javaFxText;

			if (name.isEmpty()) {
				javaFxText = new Text(textsToBeShown.timeDurationText);
			} else {
				javaFxText = new Text(textsToBeShown.timeDurationText + "\n" + textsToBeShown.name);
			}

			javaFxText.setFont(font);
			nodes.add(javaFxText);

			final StackPane stackPane = new StackPane();
			stackPane.setMinSize(diameter, diameter);
			stackPane.setPrefSize(diameter, diameter);
			stackPane.setMaxSize(diameter, diameter);
			stackPane.getChildren().addAll(nodes);
			stackPane.setStyle("-fx-background-color: transparent;");

			StrokeTransition transition = new StrokeTransition(new Duration(100.), circle);

			transition.setFromValue(Color.RED);
			transition.setToValue(Color.BLUE);
			transition.setCycleCount(Animation.INDEFINITE);
			transition.setAutoReverse(true);
			transition.play();

			final Scene scene = new Scene(stackPane);
			scene.setFill(Color.TRANSPARENT); // Background transparent
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

	@SuppressWarnings("java:S9396")
	private class TextsToBeShown {
		String name;
		String timeDurationText;
	}

	@SuppressWarnings({ "java:S1643" })
	private TextsToBeShown getTexts(final String name, final String timeDurationText, final double diameter,
			final double circleStrokeWidth, Font font) {
		double width = 0;
		String nameToBeShown = name.trim();

		double minimalDistanceCircleText = 1;

		do {
			width = getWidthText(nameToBeShown + "...", font);

			if (width > diameter - 2 * (circleStrokeWidth + minimalDistanceCircleText)) {
				nameToBeShown = nameToBeShown.substring(0, nameToBeShown.length() - 2);
			}

		} while (width > diameter);

		if (!name.equals(nameToBeShown)) {
			nameToBeShown += "...";
		}

		String timeDurationTextToBeShown = timeDurationText;

		double lengthNameToBeShown = getWidthText(nameToBeShown, font);
		double lengthTimeDurationToBeShown = getWidthText(timeDurationText, font);

		if (lengthNameToBeShown < lengthTimeDurationToBeShown) {
			do {
				nameToBeShown = " " + nameToBeShown + " ";
				lengthNameToBeShown = getWidthText(nameToBeShown, font);
			} while (lengthNameToBeShown < lengthTimeDurationToBeShown);
		} else {
			do {
				timeDurationTextToBeShown = " " + timeDurationTextToBeShown + " ";
				lengthTimeDurationToBeShown = getWidthText(timeDurationTextToBeShown, font);
			} while (lengthTimeDurationToBeShown < lengthNameToBeShown);
		}

		TextsToBeShown result = new TextsToBeShown();
		result.name = nameToBeShown;
		result.timeDurationText = timeDurationTextToBeShown;
		return result;
	}

	private double getWidthText(String text, Font font) {
		final Text textForDefiningTheWidthOfTheText = new Text(text + "...");
		textForDefiningTheWidthOfTheText.setFont(font);
		Bounds bounds = textForDefiningTheWidthOfTheText.getLayoutBounds();
		return bounds.getWidth();
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
