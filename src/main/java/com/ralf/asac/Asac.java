package com.ralf.asac;

public class Asac {

	static final String DEFAULT_SOUND_FILE = "default.wav";

	public static void main(final String[] args) {
		MainClass.start(args);
	}

	static String getMinuteString(long time) {

		if (time == 1) {
			return MainClass.messages.getString("minute");
		}
		return MainClass.messages.getString("minutes");
	}
}