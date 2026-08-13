package com.ralf.asac;

public class MainPanel extends CommonPanel {

	private final MainClass mainClass;

	MainPanel(MainClass mainClass) {
		super(false);
		this.mainClass = mainClass;
	}

	@Override
	void processOnActionAlarmsComboBox() {
		mainClass.processOnActionAlarmsComboBox();
	}

	@Override
	void processOnActionDeactivateButton() {
		mainClass.processOnActionDeactivateButton();
	}

	@Override
	void processOnActionPauseButton() {
		mainClass.processOnActionPauseButton();
	}

	@Override
	void processOnActionAlarmManagerButton() {
		mainClass.processOnActionAlarmManagerButton();
	}

	@Override
	void processOnActionRepeatButton() {
		mainClass.processOnActionRepeatButton();
	}

}
