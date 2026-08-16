package com.ralf.asac;

import javafx.application.Platform;
import javafx.concurrent.Task;

class PerformDuration {

	private final long minutes;
	private final RepeatAlarmData repeatAlarmData;
	private final MainClass mainClass;
	private MyWorker myWorker;

	PerformDuration(final long minutes, final RepeatAlarmData repeatAlarmData, final MainClass mainClass) {
		if (repeatAlarmData == null) {
			this.minutes = minutes;
		} else {
			this.minutes = repeatAlarmData.duration;
		}
		System.out.println("PerformDuration 1 " + this.minutes);
		this.repeatAlarmData = repeatAlarmData;
		this.mainClass = mainClass;
	}

	void start() {
		mainClass.deactivate();
		mainClass.setVisibilityPauseButton(true);
		mainClass.setRepeatButton(new RepeatAlarmData(-1, null));

		mainClass.oldPerformDuration = this;

		mainClass.setIcon(true);
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
			System.out.println("PerformDuration 2 ");
			startBell = true;

			mainClass.setVisibilityDeactivateButton(true);

			long step = 1;

			for (int indexMinutes = 0; indexMinutes < minutes; indexMinutes++) {
				final long time = minutes - indexMinutes;
				String timeString = time + "";
				mainClass.setTimeDurationFieldText(timeString);
				mainClass.getSystray().setTimeDurationFieldText(timeString);

				final String minutesString = time + Asac.getMinuteString(time);

				final String name = mainClass.getName();
				mainClass.getSystray().setSystrayToolTip(name + " - " + minutesString);

				if (name.isBlank()) {
					mainClass.setTitle(minutesString);
				} else {
					mainClass.setTitle(name + "\u00A0" + mainClass.getDashForTitle() + "\u00A0" + minutesString);
				}

				for (int indexSeconds = 0; indexSeconds < 60; indexSeconds += step) {
					step = mainClass.getPauseButtonIsPause() ? 0 : 1;

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
		System.out.println("PerformDuration 3 ");
		final String name = mainClass.getName();
		mainClass.setTimeDurationFieldText("");
		mainClass.getSystray().setTimeDurationFieldText("");
		String title = MainClass.messages.getString("title");
		mainClass.setTitle(title);
		mainClass.getSystray().setSystrayToolTip(title);
		mainClass.setVisibilityDeactivateButton(false);
		mainClass.deactivatePauseButton();

		AlarmSounds.AlarmSoundData alarmSoundData = null;
		if (repeatAlarmData == null) {
			System.out.println("PerformDuration 4 ");
			Alarm storedAlarm = mainClass.getStoredAlarm();
			if (storedAlarm != null) {
				alarmSoundData = storedAlarm.alarmSoundData;
			}
		} else {
			alarmSoundData = repeatAlarmData.alarmSoundData;
		}

		mainClass.setRepeatButton(new RepeatAlarmData(minutes, alarmSoundData));

		if (minutes == 0) {
			Asac.sleep(100); // avoid that the BellIcon vanishes after entering 0 in the timeDurationField
		}
		mainClass.bellIcon = new BellIcon(name, alarmSoundData);
		mainClass.bellIcon.play();
		Platform.runLater(() -> {
			mainClass.resetStoredAlarmsVaLue();
			mainClass.setIcon(false);
		});
	}

}
