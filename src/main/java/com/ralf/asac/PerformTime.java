package com.ralf.asac;

import java.time.LocalTime;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;

class PerformTime implements EventHandler<javafx.event.ActionEvent> {

	private MyWorker myWorker;
	private final int hour;
	private final int minute;
	private final MainClass mainClass;

	PerformTime(final int hour, final int minute, final MainClass mainClass) {
		this.hour = hour;
		this.minute = minute;
		this.mainClass = mainClass;
	}

	@Override
	public void handle(final ActionEvent event) {
		mainClass.deactivate();

		mainClass.oldPerformTime = this;
		mainClass.setIcon(true);

		final LocalTime now = LocalTime.now();
		final long nowMilliSeconds = now.toNanoOfDay() / 1000000;

		final LocalTime desiredTime = LocalTime.of(hour, minute, 0, 0);

		long desiredMilliSeconds = desiredTime.toNanoOfDay() / 1000000;

		if (desiredMilliSeconds < nowMilliSeconds) {
			desiredMilliSeconds += 24 * 3600 * 1000;
		}

		String hourString = "0" + hour;
		hourString = hourString.substring(hourString.length() - 2);

		String minuteString = "0" + minute;
		minuteString = minuteString.substring(minuteString.length() - 2);

		mainClass.setTimeDurationFieldText(hourString + ":" + minuteString);

		myWorker = new MyWorker(desiredMilliSeconds - nowMilliSeconds);
		new Thread(myWorker).start();
	}

	void stop() {
		myWorker.startBell = false;
	}

	private class MyWorker extends Task<Integer> {

		private final long durationMilliSeconds;
		boolean startBell;

		MyWorker(final long durationMilliSeconds) {
			this.durationMilliSeconds = durationMilliSeconds;
		}

		@SuppressWarnings("java:S2142")
		@Override
		protected Integer call() throws Exception {
			startBell = true;
			mainClass.setVisibilityDeactivateButton(true);
			try {
				Thread.sleep(durationMilliSeconds);
			} catch (InterruptedException exception) {
				exception.printStackTrace();
			}

			if (startBell) {
				mainClass.setVisibilityDeactivateButton(false);
				mainClass.setTimeDurationFieldText("");

				final String name = mainClass.getName();

				AlarmSounds.AlarmSoundData alarmSoundData = null;
				Alarm storedAlarm = mainClass.getStoredAlarm();
				if (storedAlarm != null) {
					alarmSoundData = storedAlarm.alarmSoundData;
				}

				mainClass.bellIcon = new BellIcon(name, alarmSoundData);
				mainClass.bellIcon.play();
				Platform.runLater(() -> {
					mainClass.resetStoredAlarmsVaLue();
					mainClass.setIcon(false);
				});
			}
			return 0;
		}
	}

}
