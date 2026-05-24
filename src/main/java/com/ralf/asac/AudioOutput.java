package com.ralf.asac;

import java.net.URL;

import javafx.concurrent.Task;
import javafx.scene.media.AudioClip;

class AudioOutput extends Task<Integer> {
	private final AudioClip audioClip;
	private boolean isRunning;

	AudioOutput(URL url) {
		audioClip = new AudioClip(url.toString());
	}

	void play() {
		new Thread(this).start();
	}

	void stopPlaying() {
		isRunning = false;
		audioClip.stop();
	}

	@Override
	@SuppressWarnings({ "java:S2189", "java:S2589", "java:S2142" })
	protected Integer call() throws Exception {
		isRunning = true;
		while (isRunning) {
			try {
				audioClip.play();
			} catch (IllegalArgumentException illegalArgumentException) {
				illegalArgumentException.printStackTrace();
			}

			while (audioClip.isPlaying()) {
				try {
					Thread.sleep(100);
				} catch (final InterruptedException exception) {
					exception.printStackTrace();
				}
			}
		}
		return 0;
	}
}
