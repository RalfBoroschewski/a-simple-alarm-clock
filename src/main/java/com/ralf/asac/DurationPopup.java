package com.ralf.asac;

class DurationPopup {

	void buildPopup(final DurationPopupListener listener) {
		final int[] minutesList = new int[] { 1, 2, 3, 4, 5, 6, 7, 8, 9, -1, 10, 11, 12, 13, 14, 15, 20, 25, 30, 45, 60,
				75, 90, 120 };

		for (int minute : minutesList) {
			if (minute != -1) {
				final String minutesString = Asac.getMinuteString(minute);
				listener.setMenuItem(minute, minutesString);
			} else {
				listener.addSeparator();
			}
		}
	}
}

interface DurationPopupListener {
	void setMenuItem(int minute, String minutesString);

	void addSeparator();
}
