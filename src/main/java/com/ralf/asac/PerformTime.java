package com.ralf.asac;

import java.time.LocalDateTime;
import java.time.ZoneId;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;

class PerformTime implements EventHandler<javafx.event.ActionEvent> {

	private MyWorker myWorker;
	private final int hour;
	private final int minute;
	private final MainClass mainClass;

	static int INTERVAL_LENGTH_IN_SECONDS = 15;

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
		mainClass.setRepeatButton(-1);

		final LocalDateTime now = LocalDateTime.now(ZoneId.systemDefault());

		LocalDateTime desiredTime = LocalDateTime.of(now.getYear(), now.getMonth(), now.getDayOfMonth(), hour, minute);

		if (desiredTime.compareTo(now) < 0) {
			desiredTime = desiredTime.plusDays(1);
		}

		String hourString = "0" + hour;
		hourString = hourString.substring(hourString.length() - 2);

		String minuteString = "0" + minute;
		minuteString = minuteString.substring(minuteString.length() - 2);

		final String time = hourString + ":" + minuteString;
		mainClass.setTimeDurationFieldText(time);

		final String name = mainClass.getName();

		if (name.isBlank()) {
			mainClass.getSystray().setSystrayToolTip(time);
		} else {
			mainClass.getSystray().setSystrayToolTip(name + "- " + time);
		}

		myWorker = new MyWorker(desiredTime);
		new Thread(myWorker).start();
	}

	void stop() {
		myWorker.startBell = false;
	}

	private class MyWorker extends Task<Integer> {
		private final LocalDateTime desiredTime;
		boolean startBell;

		MyWorker(LocalDateTime desiredTime) {
			this.desiredTime = desiredTime;
		}

		@SuppressWarnings({ "java:S2142", "java:S4507", "java:S2589" })
		@Override
		protected Integer call() throws Exception {
			startBell = true;
			mainClass.setVisibilityDeactivateButton(true);

			LocalDateTime now = LocalDateTime.now(ZoneId.systemDefault());

			while (now.compareTo(desiredTime) < 0 && startBell) {
				final int nowSeconds = now.getSecond();
				final int nextIntervalSeconds = ((nowSeconds + INTERVAL_LENGTH_IN_SECONDS) / INTERVAL_LENGTH_IN_SECONDS)
						* INTERVAL_LENGTH_IN_SECONDS;

				final int durationInSeconds = nextIntervalSeconds - nowSeconds;

				if (durationInSeconds > 0) {
					Asac.sleep(durationInSeconds * 1000l);
				} else {
					break;
				}
				now = LocalDateTime.now(ZoneId.systemDefault());
			}

			if (startBell) {
				mainClass.setVisibilityDeactivateButton(false);
				mainClass.setTimeDurationFieldText("");
				mainClass.getSystray().setSystrayToolTip("");

				final String name = mainClass.getName();

				AlarmSounds.AlarmSoundData alarmSoundData = null;
				final Alarm storedAlarm = mainClass.getStoredAlarm();
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
