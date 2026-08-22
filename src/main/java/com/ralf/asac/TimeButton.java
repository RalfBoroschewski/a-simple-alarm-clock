package com.ralf.asac;

import java.time.LocalDateTime;
import java.time.ZoneId;

import javafx.geometry.Side;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;

class TimeButton extends Button {

	TimeButton() {
		super(MainClass.messages.getString("TimeButton.set.time"));
	}

	void init(MainPanel mainPanel) {
		this.setOnAction(event -> buildTimePopup(mainPanel).show(this, Side.BOTTOM, 0, 0));
	}

	ContextMenu buildTimePopup(final MainPanel mainPanel) {

		final LocalDateTime now = LocalDateTime.now(ZoneId.systemDefault());
		int hourNow = now.getHour();
		final int minuteNow = now.getMinute();

		int startIndex = minuteNow / 5 + 1;

		if (minuteNow >= 55) {
			hourNow++;
			startIndex = 0;
		}

		final ContextMenu timePopup = new ContextMenu();

		final String marginHour = "            ";
		final String marginMinute = "       ";

		for (int hour = hourNow; hour < hourNow + 24; hour++) {

			final Menu hourMenu = new Menu("\u00A0" + marginHour + (hour % 24) + ":00" + marginHour);

			for (int index = startIndex; index < 12; index++) {
				final int minute = index * 5;
				String minuteString = "0" + minute;
				minuteString = minuteString.substring(minuteString.length() - 2);
				final MenuItem menuItem = new MenuItem("\u00A0" + marginMinute + minuteString + marginMinute);
				final int tmpHour = hour;
				menuItem.setOnAction(event -> new PerformTime(tmpHour % 24, minute, mainPanel).start());
				hourMenu.getItems().add(menuItem);
			}
			startIndex = 0;
			timePopup.getItems().add(hourMenu);

		}

		return timePopup;
	}
}
