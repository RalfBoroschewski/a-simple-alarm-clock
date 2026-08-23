package com.ralf.asac;

import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.stage.Screen;
import javafx.stage.Stage;

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

		final String xdgDesktop = System.getenv("XDG_CURRENT_DESKTOP");
		final String kdeSession = System.getenv("KDE_FULL_SESSION");

		if ("KDE".equalsIgnoreCase(xdgDesktop) || "true".equalsIgnoreCase(kdeSession)) {
			return OperationSystem.KDE;
		}

		if ("XFCE".equalsIgnoreCase(xdgDesktop)) {
			return OperationSystem.XFCE;
		}

		final String osName = System.getProperty("os.name").toLowerCase();

		if (osName.contains("win")) {
			return OperationSystem.WINDOWS;
		} else if (osName.contains("mac")) {
			return OperationSystem.MAC;
		}
		return OperationSystem.OTHER;
	}

	@SuppressWarnings({ "java:S2142", "java:S4507" })
	static void sleep(final long milliSecond) {
		try {
			Thread.sleep(milliSecond);
		} catch (InterruptedException exception) {
			exception.printStackTrace();
		}
	}

	static Point2D getCoordinatesofMiddleOfTheScreen(final Stage stage) {
		final Rectangle2D primScreenBounds = Screen.getPrimary().getVisualBounds();
		final double stageWidth = stage.getWidth();
		final double stageHeight = stage.getHeight();
		final double windowsPositionX = (primScreenBounds.getWidth() - stageWidth) / 2;
		final double windowsPositionY = (primScreenBounds.getHeight() - stageHeight) / 2;

		return new Point2D(windowsPositionX, windowsPositionY);
	}
}