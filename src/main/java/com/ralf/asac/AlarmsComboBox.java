package com.ralf.asac;

import javafx.scene.control.ComboBox;
import javafx.util.StringConverter;

public class AlarmsComboBox extends ComboBox<Alarm> {

	private Alarm storedAlarm;

	@SuppressWarnings("java:S6201")
	AlarmsComboBox(MainClass mainClass, TimeDurationField timeDurationField) {
		setEditable(true);

		setOnAction(_ -> {
			Object value = getValue();
			if (value instanceof Alarm) {
				storedAlarm = (Alarm) value;
				if (storedAlarm != null) {
					timeDurationField.setText(storedAlarm.time);
				} else {
					timeDurationField.setText("");
				}
			}
			mainClass.evaluateTimeDurationField();
		});

		setConverter(this, timeDurationField);
	}

	Alarm getStoredAlarm() {
		return storedAlarm;
	}

	void setStoredAlarm(Alarm storedAlarm) {
		this.storedAlarm = storedAlarm;
	}

	private void setConverter(AlarmsComboBox alarmsComboBox, TimeDurationField timeDurationField) {
		setConverter(new StringConverter<Alarm>() {

			@Override
			public String toString(final Alarm alarm) {
				if (alarm == null) {
					return getEditor().getText();
				}

				if (alarm.name.strip().isEmpty()) {
					return getEditor().getText();
				}
				return alarm.name;
			}

			@Override
			public Alarm fromString(final String name) {

				if (name == null || name.trim().isEmpty() || getValue() == null) {
					return new Alarm(getEditor().getText(), timeDurationField.getText(), null);
				}

				for (Alarm item : alarmsComboBox.getItems()) {
					if (item.name.equals(name)) {
						return item;
					}
				}

				return new Alarm(getEditor().getText(), timeDurationField.getText(), null);
			}
		});
	}

	@SuppressWarnings({ "java:S1905", "java:S6201" })
	String getName() {
		String name = "";
		storedAlarm = getValue();
		if (storedAlarm == null) {
			name = getEditor().getText();
		} else {
			if (storedAlarm instanceof Alarm) {
				name = ((Alarm) storedAlarm).name;
			} else {
				name = storedAlarm.toString();
			}
		}
		return name;
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
		return "Name: " + name + " Time: " + time;
	}
}