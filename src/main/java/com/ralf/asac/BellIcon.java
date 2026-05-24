package com.ralf.asac;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

class BellIcon {
	final String name;
	final AlarmSounds.AlarmSoundData alarmSoundData;

	AudioOutput audioOutput;

	Stage stage;

	BellIcon(final String name, final AlarmSounds.AlarmSoundData alarmSoundData) {
		this.name = name;
		this.alarmSoundData = alarmSoundData;
	}

	void play() {
		URL url = getURL();

		if (url != null) {
			audioOutput = new AudioOutput(url);
			audioOutput.play();
		}

		final URL urlButton = ClassLoader.getSystemResource("alarm.png");

		ImageView imageView = null;
		try {
			final InputStream inputStream = urlButton.openStream();
			final Image image = new Image(inputStream);
			imageView = new ImageView(image);
		} catch (IOException exception) {
			exception.printStackTrace();
		}

		final ImageView finalImageView = imageView;

		Platform.runLater(() -> {

			stage = new Stage();
			final VBox vBox = new VBox();

			final Button button = new Button();

			if (name != null) {
				final Label caption = new Label(name);
				vBox.getChildren().addAll(caption, button);
			} else {
				vBox.getChildren().add(button);
			}

			if (finalImageView != null) {
				button.setGraphic(finalImageView);
			} else {
				button.setText(MainClass.messages.getString("BellIcon.dismiss"));
			}

			button.setOnAction(_ -> {
				audioOutput.stopPlaying();
				stage.close();
			});

			button.setOnKeyReleased(_ -> {
				audioOutput.stopPlaying();
				stage.close();
			});

			stage.setOnCloseRequest(_ -> audioOutput.stopPlaying());

			final Scene scene = new Scene(vBox);
			stage.setScene(scene);
			stage.setAlwaysOnTop(true);
			stage.show();
		});
	}

	URL getURL() {
		if (alarmSoundData == null || alarmSoundData.getPath() == null || alarmSoundData.getPath().isBlank()) {
			return ClassLoader.getSystemResource(Asac.DEFAULT_SOUND_FILE);
		} else if (alarmSoundData.isResource()) {
			if (alarmSoundData.getName().equals(MainClass.messages.getString("path.default"))) {
				return ClassLoader.getSystemResource(Asac.DEFAULT_SOUND_FILE);
			}
			return ClassLoader.getSystemResource(alarmSoundData.getName());
		} else {
			final File file = new File(alarmSoundData.getPath());
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
}
