package com.ralf.asac;

import java.util.ArrayList;
import java.util.List;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.ResourceList;
import io.github.classgraph.ScanResult;

class AlarmSounds {

	List<AlarmSoundData> getAlarmSoundDatas() {
		final List<AlarmSoundData> result = getAlarmSoundsResource();
		final ArrayList<SoundManager.SoundManagerItem> storedSounds = Preferences.getSounds();

		for (SoundManager.SoundManagerItem storedSound : storedSounds) {

			final AlarmSoundData alarmData = new AlarmSoundData(storedSound.getName(), storedSound.getPath());
			result.add(alarmData);
		}

		return result;
	}

	// build a list with all sounds in side the .jar
	private List<AlarmSoundData> getAlarmSoundsResource() {
		final List<AlarmSoundData> result = new ArrayList<>();
		try (ScanResult scanResult = new ClassGraph().acceptPaths(".").scan()) {
			final ResourceList resourceList = scanResult.getAllResources();
			final List<String> paths = resourceList.getPaths();
			for (String path : paths) {
				if (testExtention(path, ".mp3") || testExtention(path, ".wav") || testExtention(path, ".aac")) {
					if (path.equals(Asac.DEFAULT_SOUND_FILE)) {
						path = MainClass.messages.getString("path.default");
					}
					AlarmSoundData alarmSoundData = new AlarmSoundData(path);
					result.add(alarmSoundData);
				}
			}
		}
		return result;
	}

	private boolean testExtention(final String path, final String test) {
		final int lengthPath = path.length();
		final int lastIndex = path.lastIndexOf(test);
		final int testLength = test.length();
		return lastIndex == lengthPath - testLength;
	}

	static class AlarmSoundData {
		private final String name;
		private final String path;
		private final boolean isResourceValue;

		AlarmSoundData(String name) {
			this.name = name;
			this.path = null;
			isResourceValue = true;
		}

		AlarmSoundData(String name, String path) {
			this.name = name;
			this.path = path;
			isResourceValue = false;
		}

		String getName() {
			return name;
		}

		String getPath() {
			return path;
		}

		boolean isResource() {
			return isResourceValue;
		}

		@Override
		public String toString() {
			return name;
		}
	}
}
