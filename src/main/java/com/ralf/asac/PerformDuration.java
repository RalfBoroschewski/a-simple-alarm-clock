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
	private static final String TITLE = MainClass.messages.getString("title");

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
		mainPanel.setRepeatButton(new RepeatAlarmData(-1, null, null, null));

		mainPanel.oldPerformDuration = this;

		imageCreatorTaskbar.clearBackground();
		mainPanel.setVisibilityDeactivateButton(true);
		myWorker = new MyWorker();
		new Thread(myWorker).start();
	}

	void setPerformMode(PerformMode performMode) {
		myWorker.performMode = performMode;
	}

	void doNotChangeIcons() {
		myWorker.doNotChangeIcons = true;
	}

	private class MyWorker extends Task<Integer> {

		PerformMode performMode;
		boolean doNotChangeIcons;

		@Override
		@SuppressWarnings({ "java:S2583", "java:S3516", "java:S2589", "java:S3776" })
		protected Integer call() throws Exception {
			final MainPanel mainPanel = mainClass.getMainPanel();
			performMode = PerformMode.NORMAL;

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

					if (performMode != PerformMode.NORMAL)
						return 1;
					Asac.sleep(1000L);
				}

				if (performMode != PerformMode.NORMAL) {
					break;
				}
			}

			switch (performMode) {
			case NORMAL:
				performCommon(mainPanel);
				launchBell();
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

			mainPanel.setTimeDurationFieldText("");
			mainPanel.setTitle(TITLE);
			mainClass.getSystray().setSystrayToolTip(TITLE);
			mainPanel.setVisibilityDeactivateButton(false);
			mainPanel.deactivatePauseButton();

			mainClass.getSystray().setDeactivateIcon();
			Platform.runLater(() -> {
				mainPanel.resetStoredAlarmsVaLue();
				if (!doNotChangeIcons) {
					mainPanel.setDeactivateIcon();
				}
			});
			mainPanel.setVisibilityDeactivateButton(false);
			mainClass.getSystray().setSystrayToolTip(TITLE);
			imageCreatorSystray.dispose();
			mainPanel.deactivatePauseButton();
			if (!doNotChangeIcons) {
				mainPanel.setDeactivateIcon();
			}
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

		AlarmSounds.AlarmSoundData alarmSoundData = null;
		if (repeatAlarmData == null) {
			final Alarm storedAlarm = mainPanel.getStoredAlarm();
			if (storedAlarm != null) {
				alarmSoundData = storedAlarm.alarmSoundData;
			}
		} else {
			alarmSoundData = repeatAlarmData.alarmSoundData;
		}

		mainPanel.setRepeatButton(new RepeatAlarmData(minutes, name, alarmSoundData, oldAlarmsComboBoxValue));

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
