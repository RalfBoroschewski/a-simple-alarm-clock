package com.ralf.asac;

import java.util.ArrayList;
import java.util.List;

import javafx.geometry.Side;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;

class DurationButton extends Button {

	DurationButton(final MainClass mainClass) {
		super(MainClass.messages.getString("DurationButton.set.duration"));
		final ContextMenu contextMenu = new ContextMenu();

		final List<Integer> minutesList = getMinutesList();

		for (int minute : minutesList) {
			if (minute != -1) {
				final String minutesString;
				if (minute == 1) {
					minutesString = MainClass.messages.getString("DurationButton.minute");
				} else {
					minutesString = MainClass.messages.getString("DurationButton.minutes");
				}

				final MenuItem menuItem = new MenuItem(minute + minutesString);
				menuItem.setOnAction(new PerformDuration(minute, mainClass));
				contextMenu.getItems().add(menuItem);
			} else {
				contextMenu.getItems().add(new SeparatorMenuItem());
			}
		}

		this.setOnAction(_ -> contextMenu.show(this, Side.BOTTOM, 0, 0));

	}

	private List<Integer> getMinutesList() {
		final int[] values = new int[] { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, -1, 11, 12, 13, 14, 15, 20, 25, 30, 45, 60, 75,
				90, 120 };

		final List<Integer> result = new ArrayList<>();
		for (int value : values) {
			result.add(value);
		}
		return result;
	}
}
