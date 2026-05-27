package com.ralf.asac;

import java.util.ArrayList;
import java.util.List;
import java.util.prefs.BackingStoreException;

class Preferences {

	private static final char CHAR1 = 0x18C1; // Unified Canadian Aboriginal Syllabics Extended: CANADIAN SYLLABICS SHAY
	private static final char CHAR2 = 0x1981; // Neu-Tai-Lue: NEW TAI LUE LETTER LOW QA
	private static final char CHAR3 = 0x1382; // Ethiopic Supplement: ETHIOPIC SYLLABLE MWEE

	private static final String SEPARATOR = String.valueOf(new char[] { CHAR1, CHAR2, CHAR3 });

	private static final java.util.prefs.Preferences PREFERENCES_ROOT = java.util.prefs.Preferences.userRoot()
			.node("/com/ralf/asac");

	private static final String ALARMS = "alarms";
	private static final String SOUNDS = "sounds";

	private static final int ALARM_STRING_PARTS_NUMBER = 20;
	private static final int ALARM_SOUND_STRING_PARTS_NUMBER = 10;

	private Preferences() {
	}

	static void setAlarms(final List<AlarmManager.AlarmManagerItem> items) {
		final StringBuilder alarms = new StringBuilder();
		boolean isFirst = true;
		for (AlarmManager.AlarmManagerItem item : items) {
			final String[] alarmStrings = new String[ALARM_STRING_PARTS_NUMBER];

			for (int index = 0; index < alarmStrings.length; index++) {
				alarmStrings[index] = "Future Expansation";
			}
			alarmStrings[0] = item.getName();
			alarmStrings[1] = item.getTime();

			final AlarmSounds.AlarmSoundData alarmSoundData = item.getAlarmSoundData();
			if (alarmSoundData != null) {
				alarmStrings[2] = alarmSoundData.getName();
				alarmStrings[3] = alarmSoundData.getPath();
				alarmStrings[4] = alarmSoundData.isResource() ? "1" : "0";
			} else {
				alarmStrings[2] = "";
				alarmStrings[3] = "";
				alarmStrings[4] = "";
			}

			if (!isFirst) {
				alarms.append(SEPARATOR);
			}

			for (int index = 0; index < alarmStrings.length - 1; index++) {
				alarms.append(alarmStrings[index]);
				alarms.append(SEPARATOR);
			}
			alarms.append(alarmStrings[alarmStrings.length - 1]);

			isFirst = false;
		}
		PREFERENCES_ROOT.put(ALARMS, alarms.toString());
		try {
			PREFERENCES_ROOT.flush();
		} catch (BackingStoreException e) {
			e.printStackTrace();
		}
	}

	static ArrayList<AlarmManager.AlarmManagerItem> getAlarms() {
		ArrayList<AlarmManager.AlarmManagerItem> items = new ArrayList<>();
		final String alarmsString = PREFERENCES_ROOT.get(ALARMS, "");

		final String[] alarmsStrings = alarmsString.split(SEPARATOR);

		int alarmStringsOffset = 0;

		while (alarmStringsOffset < alarmsStrings.length - 1) {
			final String[] alarmStrings = new String[ALARM_STRING_PARTS_NUMBER];
			for (int index = 0; index < alarmStrings.length; index++) {
				alarmStrings[index] = alarmsStrings[index + alarmStringsOffset];
			}

			boolean isResource = alarmStrings[4].equals("1");

			AlarmSounds.AlarmSoundData alarmSoundData;
			if (isResource) {
				alarmSoundData = new AlarmSounds.AlarmSoundData(alarmStrings[2]);
			} else {
				alarmSoundData = new AlarmSounds.AlarmSoundData(alarmStrings[2], alarmStrings[3]);
			}

			AlarmManager.AlarmManagerItem item = new AlarmManager.AlarmManagerItem(alarmStrings[0], alarmStrings[1],
					alarmSoundData);
			items.add(item);

			alarmStringsOffset += alarmStrings.length;
		}

		return items;

	}

	static void setSounds(final List<SoundManager.SoundManagerItem> items) {
		final StringBuilder alarms = new StringBuilder();
		boolean isFirst = true;
		for (SoundManager.SoundManagerItem item : items) {
			final String[] alarmStrings = new String[ALARM_SOUND_STRING_PARTS_NUMBER];

			for (int index = 0; index < alarmStrings.length; index++) {
				alarmStrings[index] = "Future Expansation";
			}
			alarmStrings[0] = item.getName();
			alarmStrings[1] = item.getPath();

			if (!isFirst) {
				alarms.append(SEPARATOR);
			}

			for (int index = 0; index < alarmStrings.length - 1; index++) {
				alarms.append(alarmStrings[index]);
				alarms.append(SEPARATOR);
			}
			alarms.append(alarmStrings[alarmStrings.length - 1]);

			isFirst = false;
		}
		PREFERENCES_ROOT.put(SOUNDS, alarms.toString());
		try {
			PREFERENCES_ROOT.flush();
		} catch (BackingStoreException exception) {
			exception.printStackTrace();
		}
	}

	static ArrayList<SoundManager.SoundManagerItem> getSounds() {
		final ArrayList<SoundManager.SoundManagerItem> items = new ArrayList<>();
		final String alarmsString = PREFERENCES_ROOT.get(SOUNDS, "");

		final String[] alarmsStrings = alarmsString.split(SEPARATOR);

		int alarmStringsOffset = 0;

		while (alarmStringsOffset < alarmsStrings.length - 1) {
			final String[] alarmStrings = new String[ALARM_SOUND_STRING_PARTS_NUMBER];
			for (int index = 0; index < alarmStrings.length; index++) {
				alarmStrings[index] = alarmsStrings[index + alarmStringsOffset];
			}

			final SoundManager.SoundManagerItem item = new SoundManager.SoundManagerItem(alarmStrings[0],
					alarmStrings[1]);
			items.add(item);

			alarmStringsOffset += alarmStrings.length;
		}

		return items;

	}

}
