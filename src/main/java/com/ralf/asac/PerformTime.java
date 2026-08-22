package com.ralf.asac;

import java.time.LocalDateTime;
import java.time.ZoneId;

import javafx.application.Platform;
import javafx.concurrent.Task;

class PerformTime {

	private MyWorker myWorker;
	private final int hour;
	private final int minute;
	private final MainPanel mainPanel;

	static int INTERVAL_LENGTH_IN_SECONDS = 15;

	PerformTime(final int hour, final int minute, final MainPanel MainPanel) {
		this.hour = hour;
		this.minute = minute;
		this.mainPanel = MainPanel;
	}

	public void start() {
		mainPanel.deactivate();
		mainPanel.setRepeatButton(null);

		mainPanel.oldPerformTime = this;
		mainPanel.setIcon(true);
		mainPanel.setRepeatButton(new RepeatAlarmData(-1, null, null));

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
		mainPanel.setTimeDurationFieldText(time);
		mainPanel.setTitle(time);

		final String name = mainPanel.getName();

//		if (name.isBlank()) {
//			mainPanel.getSystray().setSystrayToolTip(time);
//		} else {
//			mainPanel.getSystray().setSystrayToolTip(name + " - " + time);
//		}

		myWorker = new MyWorker(desiredTime);
		new Thread(myWorker).start();
	}

	void stop() {
		myWorker.startBell = false;
	}

	private class MyWorker extends Task<Integer> {
		private final LocalDateTime desiredTime;
		boolean startBell;

		MyWorker(final LocalDateTime desiredTime) {
			this.desiredTime = desiredTime;
		}

		@SuppressWarnings({ "java:S2142", "java:S4507", "java:S2589" })
		@Override
		protected Integer call() throws Exception {
			startBell = true;
			mainPanel.setVisibilityDeactivateButton(true);

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
				mainPanel.setVisibilityDeactivateButton(false);
//				mainPanel.setTimeDurationFieldText("");
//				mainPanel.getSystray().setTimeDurationFieldText("");
//				mainPanel.getSystray().setSystrayToolTip("");
				mainPanel.setTitle(MainClass.messages.getString("title"));

				final String name = mainPanel.getName();

				AlarmSounds.AlarmSoundData alarmSoundData = null;
				final Alarm storedAlarm = mainPanel.getStoredAlarm();
				if (storedAlarm != null) {
					alarmSoundData = storedAlarm.alarmSoundData;
				}

				mainPanel.bellIcon = new BellIcon(name, alarmSoundData);
				mainPanel.bellIcon.play();
				Platform.runLater(() -> {
					mainPanel.resetStoredAlarmsVaLue();
					mainPanel.setIcon(false);
				});
			}
			return 0;
		}
	}

}
