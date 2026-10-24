package com.ralf.rat;

import java.time.LocalDateTime;
import java.time.ZoneId;

import javafx.application.Platform;
import javafx.concurrent.Task;

class PerformTime {

	private MyWorker myWorker;
	private final int hour;
	private final int minute;
	private String timeString;
	private final MainClass mainClass;
	private static final String TITLE = MainClass.messages.getString("title");

	static int INTERVAL_LENGTH_IN_SECONDS = 15;

	PerformTime(final int hour, final int minute, final MainClass mainClass) {
		this.hour = hour;
		this.minute = minute;
		this.mainClass = mainClass;
	}

	public void start() {
		final MainPanel mainPanel = mainClass.getMainPanel();
		mainPanel.setRepeatButton(null);

		mainPanel.oldPerformTime = this;
		mainPanel.setRepeatButton(new RepeatAlarmData(-1, null, null, null));

		final LocalDateTime now = LocalDateTime.now(ZoneId.systemDefault());

		LocalDateTime desiredTime = LocalDateTime.of(now.getYear(), now.getMonth(), now.getDayOfMonth(), hour, minute);

		if (desiredTime.compareTo(now) < 0) {
			desiredTime = desiredTime.plusDays(1);
		}

		String hourString = "0" + hour;
		hourString = hourString.substring(hourString.length() - 2);

		String minuteString = "0" + minute;
		minuteString = minuteString.substring(minuteString.length() - 2);

		timeString = hourString + ":" + minuteString;
		mainPanel.setTimeDurationFieldText(timeString);
		mainPanel.setTitle(TITLE + "\n" + timeString);

		final String name = mainPanel.getName();

		ImageCreatorJavaFx imageCreatorTaskbar = new ImageCreatorJavaFx();
		imageCreatorTaskbar.setTime(hour, minute);
		imageCreatorTaskbar.setImageToStage(mainClass);

		ImageCreatorAWT imageCreatorSystray = new ImageCreatorAWT();
		imageCreatorSystray.setTime(hour, minute);
		mainClass.getSystray().setIcon(imageCreatorSystray.getImage());

		if (name.isBlank()) {
			mainPanel.setTitle(TITLE + "\n" + timeString);
			mainClass.getSystray().setSystrayToolTip(TITLE + ": " + timeString);
		} else {
			mainPanel.setTitle(TITLE + "\n" + name + "\u00A0" + mainPanel.getDashForTitle() + "\u00A0\n" + timeString);
			mainClass.getSystray().setSystrayToolTip(TITLE + ": " + name + " - " + timeString);
		}

		myWorker = new MyWorker(desiredTime);
		new Thread(myWorker).start();
	}

	void setPerformMode(PerformMode performMode) {
		myWorker.performMode = performMode;
	}

	private class MyWorker extends Task<Integer> {
		private final LocalDateTime desiredTime;
		PerformMode performMode;

		MyWorker(final LocalDateTime desiredTime) {
			this.desiredTime = desiredTime;
		}

		@SuppressWarnings({ "java:S2142", "java:S4507", "java:S2589" })
		@Override
		protected Integer call() throws Exception {
			performMode = PerformMode.NORMAL;
			final MainPanel mainPanel = mainClass.getMainPanel();
			mainPanel.setVisibilityDeactivateButton(true);

			LocalDateTime now = LocalDateTime.now(ZoneId.systemDefault());

			while (now.compareTo(desiredTime) < 0 && performMode == PerformMode.NORMAL) {
				final int nowSeconds = now.getSecond();
				final int nextIntervalSeconds = ((nowSeconds + INTERVAL_LENGTH_IN_SECONDS) / INTERVAL_LENGTH_IN_SECONDS)
						* INTERVAL_LENGTH_IN_SECONDS;

				final int durationInSeconds = nextIntervalSeconds - nowSeconds;

				if (durationInSeconds > 0) {
					Rat.sleep(durationInSeconds * 1000l);
				} else {
					break;
				}

				now = LocalDateTime.now(ZoneId.systemDefault());
			}

			switch (performMode) {
			case NORMAL:
				performCommon(mainPanel);
				final String name = mainPanel.getName();

				AlarmSounds.AlarmSoundData alarmSoundData = null;
				final Alarm storedAlarm = mainPanel.getStoredAlarm();
				if (storedAlarm != null) {
					alarmSoundData = storedAlarm.alarmSoundData;
				}

				mainPanel.bellIcon = new BellIcon(name, alarmSoundData, timeString);
				mainPanel.bellIcon.play();
				break;
			case DEACTIVATE:
				performCommon(mainPanel);
				break;
			default:
				break;
			}

			return 0;
		}

		void performCommon(MainPanel mainPanel) {
			mainClass.getSystray().setDeactivateIcon();
			Platform.runLater(() -> {
				mainPanel.resetStoredAlarmsVaLue();
				mainPanel.setDeactivateIcon();

				mainPanel.setVisibilityDeactivateButton(false);
				mainPanel.setTimeDurationFieldText("");
				mainPanel.setTitle(TITLE);
				mainClass.getSystray().setSystrayToolTip(TITLE);

			});
		}
	}

}
