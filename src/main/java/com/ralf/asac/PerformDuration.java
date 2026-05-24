package com.ralf.asac;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;

class PerformDuration implements EventHandler<ActionEvent> {

	private final long minutes;
	private final MainClass mainClass;
	private MyWorker myWorker;

	PerformDuration(long minutes, MainClass mainClass) {
		this.minutes = minutes;
		this.mainClass = mainClass;
	}

	@Override
	public void handle(ActionEvent event) {
		mainClass.deactivate();
		mainClass.setVisibilityPauseButton(true);

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
		protected Integer call() throws Exception {
			startBell = true;
			mainClass.setVisibilityDeactivateButton(true);

			long step = 1;

			for (int indexMinutes = 0; indexMinutes < minutes; indexMinutes++) {
				mainClass.setTimeDurationFieldText((minutes - indexMinutes) + "");
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

	@SuppressWarnings("java:S2142")
	void sleep(long milliSecond) {
		try {
			Thread.sleep(milliSecond);
		} catch (InterruptedException exception) {
			exception.printStackTrace();
		}
	}
}
