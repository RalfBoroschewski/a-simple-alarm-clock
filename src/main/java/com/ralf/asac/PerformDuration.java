package com.ralf.asac;

import javafx.application.Platform;
import javafx.concurrent.Task;

class PerformDuration {

	private final long minutes;
	private final RepeatAlarmData repeatAlarmData;
	private final MainClass mainClass;
	private Alarm oldAlarmsComboBoxValue;
	private final String name;
	private final ImageCreatorJavaFx imageCreatorTaskbar;
	private final ImageCreatorAWT imageCreatorSystray;
	private MyWorker myWorker;

	PerformDuration(final long minute, final RepeatAlarmData repeatAlarmData, final MainClass mainClass) {
		this.mainClass = mainClass;
		MainPanel mainPanel = mainClass.getMainPanel();
		if (repeatAlarmData == null) {
			this.minutes = minute;
			name = mainPanel.getName();
		} else {
			this.minutes = repeatAlarmData.duration;
			if (repeatAlarmData.alarmComboBox != null) {
				name = repeatAlarmData.alarmComboBox.name;
			} else {
				name = "";
			}

		}
		this.repeatAlarmData = repeatAlarmData;
		imageCreatorTaskbar = new ImageCreatorJavaFx();
		imageCreatorSystray = new ImageCreatorAWT();
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

		imageCreatorTaskbar.clearBackground();
		mainPanel.setVisibilityDeactivateButton(true);
		myWorker = new MyWorker();
		new Thread(myWorker).start();
	}

	void stop() {
		myWorker.startBell = false;
	}

	void doNotChangeIcons() {
		myWorker.doNotChangeIcons = true;
	}

	private class MyWorker extends Task<Integer> {

		boolean startBell;
		boolean doNotChangeIcons;

		@Override
		@SuppressWarnings({ "java:S2583", "java:S3516", "java:S2589", "java:S3776" })
		protected Integer call() throws Exception {
			final MainPanel mainPanel = mainClass.getMainPanel();
			startBell = true;

			long step = 1;

			final String programName = MainClass.messages.getString("title");

			for (int indexMinutes = 0; indexMinutes < minutes; indexMinutes++) {
				final long time = minutes - indexMinutes;
				final String timeString = time + "";
				mainPanel.setTimeDurationFieldText(timeString);

				final String minutesString = time + Asac.getMinuteString(time);

				if (!doNotChangeIcons) {
					adjustIcons(time);
					if (name.isBlank()) {
						String title = programName + "\n" + minutesString;
						mainPanel.setTitle(title);
						mainClass.getSystray().setSystrayToolTip(title);
					} else {
						// String
						mainPanel.setTitle(programName + "\n" + name + "\u00A0" + mainPanel.getDashForTitle()
								+ "\u00A0\n" + minutesString);
						mainClass.getSystray().setSystrayToolTip(programName + ": " + name + " - " + minutesString);
					}
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

			if (mainClass.getSystray().hasSystray()) {
				if (!doNotChangeIcons) {
					mainClass.getSystray().setDeactivateIcon();
				}

				imageCreatorSystray.dispose();
			}

			if (startBell) {
				launchBell();
			}

			return 0;
		}

	}

	@SuppressWarnings("java:S3398")
	private void adjustIcons(long time) {
		Platform.runLater(() -> {
			imageCreatorTaskbar.clearBackgroundDuration();
			imageCreatorTaskbar.setDuration((int) time);
			imageCreatorTaskbar.setImageToStage(mainClass);

			if (mainClass.getSystray().hasSystray()) {
				imageCreatorSystray.clearBackgroundDuration();
				imageCreatorSystray.setDuration((int) time);
				mainClass.getSystray().setIcon(imageCreatorSystray.getImage());
			}

		});
	}

	@SuppressWarnings("java:S3398")
	private void launchBell() {
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
		mainPanel.bellIcon = new BellIcon(name, alarmSoundData, minutes + Asac.getMinuteString(minutes));
		mainPanel.bellIcon.play();
		Platform.runLater(() -> {
			mainPanel.resetStoredAlarmsVaLue();
			mainPanel.setDeactivateIcon();
		});
	}

}
