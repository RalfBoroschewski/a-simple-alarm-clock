package com.ralf.asac;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.stage.Stage;

class PerformDuration implements EventHandler<ActionEvent> {

	private final long minutes;
	private final Stage stage;
	private final MainClass mainClass;
	private MyWorker myWorker;

	PerformDuration(final long minutes, final Stage stage, final MainClass mainClass) {
		this.minutes = minutes;
		this.stage = stage;
		this.mainClass = mainClass;
	}

	@Override
	public void handle(final ActionEvent event) {
		stage.setTitle("Holla 1");
		mainClass.deactivate();
		stage.setTitle("Holla 2");
		mainClass.setVisibilityPauseButton(true);

		mainClass.oldPerformDuration = this;

		mainClass.setIcon(true);
		stage.setTitle("Holla 3");
		myWorker = new MyWorker();
		stage.setTitle("Holla 4");
		new Thread(myWorker).start();
	}

	void stop() {
		myWorker.startBell = false;
	}

	private class MyWorker extends Task<Integer> {

		boolean startBell;

		@Override
		protected Integer call() throws Exception {
			startBell = true;
			mainClass.setVisibilityDeactivateButton(true);

			long step = 1;

			for (int indexMinutes = 0; indexMinutes < minutes; indexMinutes++) {
				long time = minutes - indexMinutes;
				String timeString = time + "";
				mainClass.setTimeDurationFieldText(timeString);

				String minutesString = time + Asac.getMinuteString(time);

				mainClass.setSystrayTooltip(minutesString);
				Platform.runLater(() -> stage.setTitle(minutesString));

				for (int indexSeconds = 0; indexSeconds < 60; indexSeconds += step) {
					if (mainClass.pauseButtonIsPause) {
						step = 0;
					} else {
						step = 1;
					}

					if (!startBell)
						return 1;
					sleep(1000L);
				}
				if (!startBell) {
					break;
				}
			}

			if (startBell) {
				final String name = mainClass.getName();
				mainClass.setTimeDurationFieldText("");
				mainClass.setSystrayTooltip("");
				mainClass.setVisibilityDeactivateButton(false);
				mainClass.deactivatePauseButton();

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

	@SuppressWarnings({ "java:S2142", "java:S4507" })
	void sleep(final long milliSecond) {
		try {
			Thread.sleep(milliSecond);
		} catch (InterruptedException exception) {
			exception.printStackTrace();
		}
	}
}
