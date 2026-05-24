package com.ralf.asac;

import java.util.GregorianCalendar;

import javafx.scene.control.ComboBox;
import javafx.util.StringConverter;

public class AlarmsComboBox extends ComboBox<Alarm> {

	private Alarm storedAlarm;
	private long timeOfLastEdit;

	@SuppressWarnings("java:S6201")
	AlarmsComboBox(MainClass mainClass, TimeDurationField timeDurationField) {
		setEditable(true);

		setOnAction(_ -> {

			Object value = getValue();
			if (value instanceof Alarm) {
				storedAlarm = (Alarm) value;
				timeDurationField.setText(storedAlarm.time);
				if (storedAlarm != null) {
					timeDurationField.setText(storedAlarm.time);
				} else {
					timeDurationField.setText("");
				}
			}
			mainClass.evaluateTimeDurationField();
		});

		getEditor().textProperty().addListener((_, _, newText) -> {
			if (newText == null || newText.trim().isEmpty()) {
				return;
			}

			Alarm alarm = getSelectionModel().getSelectedItem();
			if (alarm != null && alarm.name != null) {
				timeOfLastEdit = newText.equals(alarm.name) ? new GregorianCalendar().getTimeInMillis() : 0;
			}
		});

		setConverter();

	}

	Alarm getStoredAlarm() {
		return storedAlarm;
	}

	void setStoredAlarm(Alarm storedAlarm) {
		this.storedAlarm = storedAlarm;
	}

	private void setConverter() {
		setConverter(new StringConverter<Alarm>() {
			@Override
			public String toString(final Alarm alarm) {
				if (alarm == null) {
					return "";
				}
				return alarm.name;
			}

			@Override
			public Alarm fromString(final String string) {
				if (string == null || string.trim().isEmpty()) {
					return null;
				}
				return getValue();
			}
		});
	}

	@SuppressWarnings({ "java:S1905", "java:S6201" })
	String getName() {
		String name = "";
		storedAlarm = getValue();
		System.out.println("Holla 1" + storedAlarm);
		if (storedAlarm != null) {
			System.out.println("Holla 2");
			if (storedAlarm instanceof Alarm) {
				System.out.println("Holla 3");
				name = ((Alarm) storedAlarm).name;
			} else {
				System.out.println("Holla 4");
				name = storedAlarm.toString();
			}
		}
		return name;
	}

	boolean isTextFieldEdited() {
		long elapsedTime = new GregorianCalendar().getTimeInMillis() - timeOfLastEdit;

		return elapsedTime < 20_000;
	}

	void clear() {
		getEditor().clear();
	}

}

class Alarm {
	final String name;
	final String time;
	final AlarmSounds.AlarmSoundData alarmSoundData;

	Alarm(final String name, final String time, final AlarmSounds.AlarmSoundData alarmSoundData) {
		this.name = name;
		this.time = time;
		this.alarmSoundData = alarmSoundData;
	}

	public String toString() {
		return name;
	}
}