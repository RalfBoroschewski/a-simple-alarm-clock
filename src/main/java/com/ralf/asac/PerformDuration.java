package com.ralf.asac;

import javafx.application.Platform;
import javafx.concurrent.Task;

class PerformDuration {

	private final long minutes;
	private final RepeatAlarmData repeatAlarmData;
	private final MainClass mainClass;
	private Alarm oldAlarmsComboBoxValue;
	private final String name;
	private final ImageCreatorJavaFx imageCreator;
	private MyWorker myWorker;

	PerformDuration(final long minutes, final RepeatAlarmData repeatAlarmData, final MainClass mainClass) {
		this.mainClass = mainClass;
		MainPanel mainPanel = mainClass.getMainPanel();
		if (repeatAlarmData == null) {
			this.minutes = minutes;
			name = mainPanel.getName();
		} else {
			this.minutes = repeatAlarmData.duration;
			name = repeatAlarmData.alarmComboBox.name;
		}
		this.repeatAlarmData = repeatAlarmData;
		imageCreator = new ImageCreatorJavaFx();
	}

	void start() {
		final MainPanel mainPanel = mainClass.getMainPanel();
		if (repeatAlarmData != null) {
			oldAlarmsComboBoxValue = repeatAlarmData.alarmComboBox;
		} else {
			oldAlarmsComboBoxValue = mainPanel.getAlarmsComboBox().getValue();
		}

		mainPanel.deactivate();
		mainPanel.setVisibilityPauseButton(true);
		mainPanel.setRepeatButton(new RepeatAlarmData(-1, null, null));

		mainPanel.oldPerformDuration = this;

		imageCreator.clearBackground();
//		imageCreator.setDuration((int) minutes);
//		imageCreator.setImageToStage(mainClass);

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
			final MainPanel mainPanel = mainClass.getMainPanel();
			startBell = true;

			mainPanel.setVisibilityDeactivateButton(true);

			// mainClass.getSystray().setIcon(true);

			long step = 1;

			for (int indexMinutes = 0; indexMinutes < minutes; indexMinutes++) {
				final long time = minutes - indexMinutes;
				final String timeString = time + "";
				mainPanel.setTimeDurationFieldText(timeString);

				final String minutesString = time + Asac.getMinuteString(time);

				Platform.runLater(() -> {
					imageCreator.clearBackgroundDuration();
					imageCreator.setDuration((int) time);
					imageCreator.setImageToStage(mainClass);
				});

				if (name.isBlank()) {
					mainPanel.setTitle(minutesString);
					mainClass.getSystray().setSystrayToolTip(minutesString);
				} else {
					mainPanel.setTitle(name + "\u00A0" + mainPanel.getDashForTitle() + "\u00A0" + minutesString);
					mainClass.getSystray().setSystrayToolTip(name + " - " + minutesString);
				}

//				imageCreator.clearBackgroundDuration();
//				imageCreator.setDuration((int) time);

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

			mainClass.getSystray().setIcon(false);
			if (startBell) {
				launchBell();
			}

			return 0;
		}

	}

	void launchBell() {
		final MainPanel mainPanel = mainClass.getMainPanel();
		mainPanel.setTimeDurationFieldText("");
		final String title = MainClass.messages.getString("title");
		mainPanel.setTitle(title);
		mainClass.getSystray().setSystrayToolTip(title);
		mainPanel.setVisibilityDeactivateButton(false);
		mainPanel.deactivatePauseButton();

		AlarmSounds.AlarmSoundData alarmSoundData = null;
		if (repeatAlarmData == null) {
			final Alarm storedAlarm = mainPanel.getStoredAlarm();
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
			mainPanel.setDeactivateIcon();
		});
	}

}
