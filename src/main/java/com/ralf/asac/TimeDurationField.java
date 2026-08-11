package com.ralf.asac;

import java.util.function.UnaryOperator;

import javafx.application.Platform;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.stage.Stage;

class TimeDurationField extends TextField {

	boolean timeDurationFieldIsSetInternal;

	TimeDurationField() {
		super();
		init();
	}

	TimeDurationField(final String text) {
		super(text);
		init();
	}

	@SuppressWarnings("java:S3776")
	public void init() {
		final UnaryOperator<TextFormatter.Change> filter = change -> {
			final String newText = change.getControlNewText();

			int numberColons = 0;

			for (char myChar : newText.toCharArray()) {
				if (myChar == '.' || myChar == ',' || myChar == ':') {
					numberColons++;
				}
			}

			if (newText.matches("[0123456789.,:]*")) {
				final char[] chars = change.getText().toCharArray();
				final StringBuilder result = new StringBuilder();
				for (int index = 0; index < chars.length; index++) {
					if (chars[index] == ',' || chars[index] == '.' || chars[index] == ':') {
						if (numberColons < 2) {
							result.append(':');
						}
					} else {
						result.append(chars[index]);
					}
				}

				final int colonPosition = newText.indexOf(":");
				if (colonPosition >= 0) {
					final String hourString = newText.substring(0, colonPosition);

					if (hourString.isEmpty())
						return null;
					final String minuteString = newText.substring(colonPosition + 1);

					final int hour = Integer.parseInt("0" + hourString);
					final int minute = Integer.parseInt("0" + minuteString);

					if (hour > 23 || minute > 59) {
						return null;
					}
				} else {
					if (!newText.isEmpty()) {
						try {
							Long.parseLong(newText);
						} catch (NumberFormatException exception) {
							return null;
						}
					}
				}

				change.setText(result.toString());
				return change;
			}
			return null;
		};

		setTextFormatter(new TextFormatter<>(filter));
	}

	void setIsInternal(boolean timeDurationFieldIsSetInternal) {
		this.timeDurationFieldIsSetInternal = timeDurationFieldIsSetInternal;
	}

	void evaluateTimeDurationField(MainClass mainClass) {
		if (timeDurationFieldIsSetInternal) {
			return;
		}
		final String timeDuration = this.getText();

		if (timeDuration != null && !timeDuration.isEmpty()) {
			mainClass.deactivate();
			final int colonIndex = timeDuration.indexOf(':');
			if (colonIndex < 0) {
				final long minute = Long.parseLong(timeDuration);
				final PerformDuration performDuration = new PerformDuration(minute, mainClass);
				performDuration.start();
			} else {
				final String hourString = timeDuration.substring(0, colonIndex);
				final String minuteString = timeDuration.substring(colonIndex + 1);
				final int hour = Integer.parseInt(hourString);
				final int minute = Integer.parseInt(minuteString);

				final String tmp = "00" + minuteString;
				final String minuteStringFormated = tmp.substring(0, tmp.length() - 2);
				final String time = hour + ":" + minuteStringFormated;
				final String name = mainClass.getName();
				Stage stage = mainClass.getStage();
				if (name.isBlank()) {
					Platform.runLater(() -> stage.setTitle(time));
				} else {
					Platform.runLater(
							() -> stage.setTitle(name + "\u00A0" + mainClass.getDashForTitle() + "\u00A0" + time));
				}

				final PerformTime performTime = new PerformTime(hour, minute, mainClass);
				performTime.start();
			}
		}
	}

}
