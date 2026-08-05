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
		KDE, XFCE, WINDOWS, MAC, OTHER
	}

	static OperationSystem getOperationSystem() {

		String xdgDesktop = System.getenv("XDG_CURRENT_DESKTOP");
		String kdeSession = System.getenv("KDE_FULL_SESSION");

		System.out.println("Hallo 1 " + xdgDesktop);

		System.out.println("Hallo 2 " + kdeSession);

		if ("KDE".equalsIgnoreCase(xdgDesktop) || "true".equalsIgnoreCase(kdeSession)) {
			return OperationSystem.KDE;
		}

		if ("XFCE".equalsIgnoreCase(xdgDesktop)) {
			return OperationSystem.XFCE;
		}

		String osName = System.getProperty("os.name").toLowerCase();

		if (osName.contains("win")) {
			return OperationSystem.WINDOWS;
		} else if (osName.contains("mac")) {
			return OperationSystem.MAC;
		}
		return OperationSystem.OTHER;
	}

}