package com.ralf.asac;

import java.util.Calendar;
import java.util.GregorianCalendar;

import javafx.geometry.Side;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;

class TimeButton extends Button {

	TimeButton(MainClass mainClass) {
		super(MainClass.messages.getString("TimeButton.set.time"));
		this.setOnAction(_ -> buildTimePopup(mainClass).show(this, Side.BOTTOM, 0, 0));
	}

	ContextMenu buildTimePopup(final MainClass mainClass) {

		final Calendar now = new GregorianCalendar();
		int hourNow = now.get(Calendar.HOUR_OF_DAY);
		int minuteNow = now.get(Calendar.MINUTE);

		int startIndex = minuteNow / 5 + 1;

		if (minuteNow >= 55) {
			hourNow++;
			startIndex = 0;
		}

		final ContextMenu timePopup = new ContextMenu();

		for (int hour = hourNow; hour < hourNow + 24; hour++) {

			final Menu hourMenu = new Menu((hour % 24) + ":00");

			for (int index = startIndex; index < 12; index++) {
				final int minute = index * 5;
				String minuteString = "0" + minute;
				minuteString = minuteString.substring(minuteString.length() - 2);
				final MenuItem menuItem = new MenuItem(minuteString);
				menuItem.setOnAction(new PerformTime(hour % 24, minute, mainClass));
				hourMenu.getItems().add(menuItem);
			}
			startIndex = 0;
			timePopup.getItems().add(hourMenu);

		}

		return timePopup;
	}
}
