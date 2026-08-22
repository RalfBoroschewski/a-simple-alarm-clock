package com.ralf.asac;

import javafx.application.Platform;
import javafx.concurrent.Task;

class PerformDuration {

	private final long minutes;
	private final RepeatAlarmData repeatAlarmData;
	private final MainPanel mainPanel;
	private Alarm oldAlarmsComboBoxValue;
	final String name;
	private MyWorker myWorker;

	PerformDuration(final long minutes, final RepeatAlarmData repeatAlarmData, final MainPanel mainPanel) {
		if (repeatAlarmData == null) {
			this.minutes = minutes;
			name = mainPanel.getName();
		} else {
			this.minutes = repeatAlarmData.duration;
			this.name = repeatAlarmData.alarmComboBox.name;
		}
		this.repeatAlarmData = repeatAlarmData;
		this.mainPanel = mainPanel;
	}

	void start() {
		if (repeatAlarmData != null) {
			oldAlarmsComboBoxValue = repeatAlarmData.alarmComboBox;
		} else {
			oldAlarmsComboBoxValue = mainPanel.getAlarmsComboBox().getValue();
		}

		mainPanel.deactivate();
		mainPanel.setVisibilityPauseButton(true);
		mainPanel.setRepeatButton(new RepeatAlarmData(-1, null, null));

		mainPanel.oldPerformDuration = this;

		mainPanel.setIcon(true);
		myWorker = new MyWorker();
		new Thread(myWorker).start();
	}

	void stop() {
		myWorker.startBell = false;
	}

	private class MyWorker extends Task<Integer> {

		boolean startBell;

		@Override
		@SuppressWarnings({ "java:S2583", "java:S3516", "java:S2589" })
		protected Integer call() throws Exception {
			startBell = true;

			mainPanel.setVisibilityDeactivateButton(true);

			long step = 1;

			for (int indexMinutes = 0; indexMinutes < minutes; indexMinutes++) {
				final long time = minutes - indexMinutes;
				final String timeString = time + "";
				mainPanel.setTimeDurationFieldText(timeString);

				final String minutesString = time + Asac.getMinuteString(time);

				if (name.isBlank()) {
					mainPanel.setTitle(minutesString);
				} else {
					mainPanel.setTitle(name + "\u00A0" + mainPanel.getDashForTitle() + "\u00A0" + minutesString);
				}

				for (int indexSeconds = 0; indexSeconds < 60; indexSeconds += step) {
					step = mainPanel.getPauseButtonIsPause() ? 0 : 1;

					if (!startBell)
						return 1;
					Asac.sleep(1000L);
				}

				if (!startBell) {
					break;
				}
			}

			if (startBell) {
				launchBell();
			}

			return 0;
		}

	}

	void launchBell() {
		mainPanel.setTimeDurationFieldText("");
		final String title = MainClass.messages.getString("title");
		mainPanel.setTitle(title);
		mainPanel.setVisibilityDeactivateButton(false);
		mainPanel.deactivatePauseButton();

		AlarmSounds.AlarmSoundData alarmSoundData = null;
		if (repeatAlarmData == null) {
			Alarm storedAlarm = mainPanel.getStoredAlarm();
			if (storedAlarm != null) {
				alarmSoundData = storedAlarm.alarmSoundData;
			}
		} else {
			alarmSoundData = repeatAlarmData.alarmSoundData;
		}

		mainPanel.setRepeatButton(new RepeatAlarmData(minutes, alarmSoundData, oldAlarmsComboBoxValue));

		if (minutes == 0) {
			Asac.sleep(100); // avoid that the BellIcon vanishes after entering 0 in the timeDurationField
		}
		mainPanel.bellIcon = new BellIcon(name, alarmSoundData);
		mainPanel.bellIcon.play();
		Platform.runLater(() -> {
			mainPanel.resetStoredAlarmsVaLue();
			mainPanel.setIcon(false);
		});
	}

}
