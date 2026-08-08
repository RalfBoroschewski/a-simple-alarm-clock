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
		mainClass.deactivate();
		mainClass.setVisibilityPauseButton(true);
		mainClass.setRepeatButton(-1);

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
				long time = minutes - indexMinutes;
				String timeString = time + "";
				mainClass.setTimeDurationFieldText(timeString);

				String minutesString = time + Asac.getMinuteString(time);

				final String name = mainClass.getName();
				mainClass.setSystrayToolTip(name + " - " + minutesString);

				setTitle(name, minutesString);

				for (int indexSeconds = 0; indexSeconds < 60; indexSeconds += step) {
					step = mainClass.pauseButtonIsPause ? 0 : 1;

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

	void setTitle(String name, String minutesString) {
		if (name.isBlank()) {
			Platform.runLater(() -> stage.setTitle(minutesString));
		} else {
			Platform.runLater(
					() -> stage.setTitle(name + "\u00A0" + mainClass.getDashForTitle() + "\u00A0" + minutesString));
		}
	}

	void launchBell() {
		final String name = mainClass.getName();
		mainClass.setTimeDurationFieldText("");
		Platform.runLater(() -> stage.setTitle(""));
		mainClass.setSystrayToolTip("");
		mainClass.setVisibilityDeactivateButton(false);
		mainClass.deactivatePauseButton();
		mainClass.setRepeatButton(minutes);

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

}
