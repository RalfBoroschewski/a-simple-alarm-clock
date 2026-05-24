package com.ralf.asac;

import java.util.function.UnaryOperator;

import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;

class TimeDurationField extends TextField {

	TimeDurationField() {
		super();
		main();
	}

	TimeDurationField(String text) {
		super(text);
		main();
	}

	@SuppressWarnings("java:S3776")
	public void main() {
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
						} catch (NumberFormatException _) {
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

}
