package com.ralf.asac;

import java.util.ArrayList;
import java.util.Objects;

import javafx.application.Platform;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Tooltip;
import javafx.util.Duration;
import javafx.util.StringConverter;

public class AlarmsComboBox extends ComboBox<Alarm> {

	private Alarm storedAlarm;
	private AlarmsComboBox alarmsComboBoxToBeSynchronize;

	private boolean isInternal;

	@SuppressWarnings("java:S6201")
	AlarmsComboBox() {
	}

	void initialize(final MainClass mainClass, final TimeDurationField timeDurationField) {
		setEditable(true);

		valueProperty().addListener((obs, oldValue, newValue) -> {

			if (isInternal) {
				return;
			}

			if (Objects.equals(oldValue, newValue)) {
				return;
			}

			final Object value = getValue();
			if (value instanceof Alarm) {
				storedAlarm = (Alarm) value;
				if (storedAlarm != null) {
					timeDurationField.setText(storedAlarm.time);
				} else {
					timeDurationField.setText("");
				}
				if (alarmsComboBoxToBeSynchronize != null) {
					alarmsComboBoxToBeSynchronize.isInternal = true;
					alarmsComboBoxToBeSynchronize.setValue(storedAlarm);
					alarmsComboBoxToBeSynchronize.isInternal = false;
				}

			}
			timeDurationField.evaluateTimeDurationField(mainClass);

		});

		setConverter(this, timeDurationField);
	}

	void setAlarmsComboBoxToBeSynchronize(AlarmsComboBox alarmsComboBoxToBeSynchronize) {
		this.alarmsComboBoxToBeSynchronize = alarmsComboBoxToBeSynchronize;
	}

	Alarm getStoredAlarm() {
		return storedAlarm;
	}

	void setStoredAlarm(final Alarm storedAlarm) {
		this.storedAlarm = storedAlarm;
	}

	private void setConverter(final AlarmsComboBox alarmsComboBox, final TimeDurationField timeDurationField) {
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

	void showStoredAlarms() {
		final ArrayList<AlarmManager.AlarmManagerItem> items = Preferences.getAlarms();

		final ArrayList<Alarm> tmpStoredAlarms = new ArrayList<>();

		for (AlarmManager.AlarmManagerItem item : items) {
			tmpStoredAlarms.add(new Alarm(item.getName(), item.getTime(), item.getAlarmSoundData()));
		}

		getItems().clear();
		getItems().addAll(tmpStoredAlarms);

		if (items.isEmpty()) {
			Tooltip tooltip = new Tooltip(MainClass.messages.getString("MainClass.tooltip"));
			tooltip.setShowDelay(new Duration(0));
			setTooltip(tooltip);
		}
	}

	void protectedSetValue(Alarm alarm) {
		Platform.runLater(() -> {
			isInternal = true;
			setValue(alarm);
			isInternal = false;
		});
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