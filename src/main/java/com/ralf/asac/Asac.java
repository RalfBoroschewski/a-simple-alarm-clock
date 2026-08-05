package com.ralf.asac;

public class Asac {

	static final String DEFAULT_SOUND_FILE = "default.wav";

	public static void main(final String[] args) {
		MainClass.start(args);
	}

	static String getMinuteString(final long time) {

		if (time == 1) {
			return MainClass.messages.getString("minute");
		}
		return MainClass.messages.getString("minutes");
	}

	enum OperationSystem {
		KDE, XFCE, OTHER
	}

	static OperationSystem getOperationSystem() {

		String xdgDesktop = System.getenv("XDG_CURRENT_DESKTOP");
		String kdeSession = System.getenv("KDE_FULL_SESSION");

		if ("KDE".equalsIgnoreCase(xdgDesktop) || "true".equalsIgnoreCase(kdeSession)) {
			return OperationSystem.KDE;
		}

		if ("XFCE".equalsIgnoreCase(xdgDesktop)) {
			return OperationSystem.XFCE;
		}

		return OperationSystem.OTHER;
	}

}