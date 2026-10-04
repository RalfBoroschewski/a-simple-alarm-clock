package com.ralf.asac;

import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.stage.Stage;

abstract class ImageCreatorBase {

	static final int DIGIT_WIDTH = 8;
	static final int DIGIT_HEIGHT = 10;

	static final int X_SHIFT_2_DIGITS = 7;
	static final int Y_SHIFT = 10;

	static Point[][] digitsPoints;

	static void createDigitsPoints() {
		final String[] digitStrings = createRawDigits();
		digitsPoints = new Point[10][];
		for (int index = 0; index < 10; index++) {
			digitsPoints[index] = rawToPointsToDraw(digitStrings[index]);
		}
	}

	ImageCreatorBase() {
		if (digitsPoints == null) {
			createDigitsPoints();
		}
	}

	private static String[] createRawDigits() {
		String zero = // this comment is for avoiding connecting lines when formating this text
				/*   */ "..***..." + // see above
						".*****.." + // see above
						"**...**." + // see above
						"**...**." + // see above
						"**...**." + // see above
						"**...**." + // see above
						"**...**." + // see above
						"**...**." + // see above
						".*****.." + // see above
						"..***...";

		String one = // this comment is for avoiding connecting lines when formating this text
				/*   */ ".....**." + // see above
						"...****." + // see above
						"..**.**." + // see above
						".**..**." + // see above
						"**...**." + // see above
						".....**." + // see above
						".....**." + // see above
						".....**." + // see above
						".....**." + // see above
						".....**.";

		String two = // this comment is for avoiding connecting lines when formating this text
				/*   */ "..****.." + // see above
						"******.." + // see above
						"**....**" + // see above
						"**....**" + // see above
						"......**" + // see above
						".....**." + // see above
						"....**.." + // see above
						"..**...." + // see above
						"********" + // see above
						"********";

		String three = // this comment is for avoiding connecting lines when formating this text
				/*   */ "..******" + // see above
						"..***.**" + // see above
						".**...**" + // see above
						".....**." + // see above
						"....**.." + // see above
						".....**." + // see above
						"......**" + // see above
						".**...**" + // see above
						"..***.**" + // see above
						"..******";

		String four = // this comment is for avoiding connecting lines when formating this text
				/*   */ "**...**." + // see above
						"**...**." + // see above
						"**...**." + // see above
						"**...**." + // see above
						"**...**." + // see above
						"*******." + // see above
						"*******." + // see above
						".....**." + // see above
						".....**." + // see above
						".....**.";

		String five = // this comment is for avoiding connecting lines when formating this text
				/*   */ "*******." + // see above
						"*******." + // see above
						"**......" + // see above
						"**......" + // see above
						"*****..." + // see above
						"*****..." + // see above
						".....**." + // see above
						".....**." + // see above
						"*****..." + // see above
						"*****...";

		String six = // this comment is for avoiding connecting lines when formating this text
				/*   */ "..****.." + // see above
						"..****.." + // see above
						"**......" + // see above
						"**......" + // see above
						"*****..." + // see above
						"*****..." + // see above
						"**...**." + // see above
						"**...**." + // see above
						"..***..." + // see above
						"..***...";

		String seven = // this comment is for avoiding connecting lines when formating this text
				/*   */ "*******." + // see above
						"*******." + // see above
						".....**." + // see above
						".....**." + // see above
						"....**.." + // see above
						"...**..." + // see above
						"..**...." + // see above
						".**....." + // see above
						"**......" + // see above
						"........";

		String eight = // this comment is for avoiding connecting lines when formating this text
				/*   */ "..***..." + // see above
						"..***..." + // see above
						"**...**." + // see above
						"**...**." + // see above
						"..***..." + // see above
						"..***..." + // see above
						"**...**." + // see above
						"**...**." + // see above
						"..***..." + // see above
						"..***...";

		String nine = // this comment is for avoiding connecting lines when formating this text
				/*   */ "..***..." + // see above
						"..***..." + // see above
						"**...**." + // see above
						"**...**." + // see above
						"..*****." + // see above
						"..*****." + // see above
						".....**." + // see above
						".....**." + // see above
						"*****..." + // see above
						"*****...";

		return new String[] { zero, one, two, three, four, five, six, seven, eight, nine };
	}

	static class Point {
		final int x;
		final int y;

		Point(final int x, final int y) {
			this.x = x;
			this.y = y;
		}

	}

	private static Point[] rawToPointsToDraw(final String digitRawString) {
		final ArrayList<Point> points = new ArrayList<>();

		final int rows = 10;
		final int columns = 8;

		for (int row = 0; row < rows; row++) {
			for (int column = 0; column < columns; column++) {
				final char pixel = digitRawString.charAt(column + row * columns);
				if (pixel == '*') {
					points.add(new Point(column, row));
				}
			}
		}

		final Point[] result = new Point[points.size()];
		for (int index = 0; index < points.size(); index++) {
			result[index] = points.get(index);
		}
		return result;
	}

	protected int[] splitInteger(int value) {
		final int digit3 = value % 10;
		value /= 10;
		final int digit2 = value % 10;
		value /= 10;
		final int digit1 = value % 10;

		return new int[] { digit1, digit2, digit3 };
	}

	abstract void drawPixelWhite(int x, int y);

	abstract void drawPixelRed(int x, int y);

	abstract void drawPixelBlack(int x, int y);

	void clearBackground() {
		for (int y = 2; y < 30; y++) {
			for (int x = 2; x < 30; x++) {
				drawPixelWhite(x, y);
			}
		}

		for (int x = 1; x < 31; x++) {
			drawPixelRed(x, 0);
			drawPixelRed(x, 1);
			drawPixelRed(x, 30);
			drawPixelRed(x, 31);
		}

		for (int y = 1; y < 31; y++) {
			drawPixelRed(0, y);
			drawPixelRed(1, y);
			drawPixelRed(30, y);
			drawPixelRed(31, y);
		}
	}

	void clearBackgroundDuration() {
		for (int y = Y_SHIFT; y < Y_SHIFT + DIGIT_HEIGHT; y++) {
			for (int x = 2; x < 30; x++) {
				drawPixelWhite(x, y);
			}
		}
	}

	void setDuration(final int duration) {
		final int[] digits = splitInteger(duration);
		int shiftX = 13;
		if (digits[1] != 0) {
			shiftX = X_SHIFT_2_DIGITS;
		}
		boolean isRed = true;
		if (digits[0] != 0) {
			shiftX = 2;
			isRed = false;
		}

		for (int y = Y_SHIFT - 1; y < Y_SHIFT + DIGIT_HEIGHT + 1; y++) {
			if (isRed) {
				drawPixelRed(0, y);
				drawPixelRed(1, y);
				drawPixelRed(30, y);
				drawPixelRed(31, y);
			} else {
				drawPixelWhite(0, y);
				drawPixelWhite(1, y);
				drawPixelWhite(30, y);
				drawPixelWhite(31, y);
			}
		}

		if (duration > 1000) {
			for (int y = Y_SHIFT; y < Y_SHIFT + DIGIT_HEIGHT; y++) {
				for (int x = 4; x < 28; x++) {
					drawPixelBlack(x, y);
				}
			}
		}

		setColor(digits, shiftX, Y_SHIFT, 0, false);
	}

	void setTime(final int hour, final int minute) {
		final int[] hourDigits = splitInteger(hour);
		final int[] minuteDigits = splitInteger(minute);

		setColor(hourDigits, X_SHIFT_2_DIGITS, 4, 1, true);
		setColor(minuteDigits, X_SHIFT_2_DIGITS, 17, 1, true);

	}

	void setColor(final int[] digits, int shiftX, final int shiftY, final int indexStart, boolean enableAllDigits) {
		for (int index = indexStart; index < 3; index++) {
			if (digits[index] != 0 || index == 2 || enableAllDigits) {
				for (Point point : digitsPoints[digits[index]]) {
					drawPixelBlack(point.x + shiftX, point.y + shiftY);
				}
				shiftX += DIGIT_WIDTH + 2;
			}
		}
	}
}

class ImageCreatorJavaFx extends ImageCreatorBase {

	private WritableImage image;
	private PixelWriter pixelWriter;

	static int colorToArgb(javafx.scene.paint.Color color) {
		int a = (int) Math.round(color.getOpacity() * 255);
		int red = (int) Math.round(color.getRed() * 255);
		int green = (int) Math.round(color.getGreen() * 255);
		int blue = (int) Math.round(color.getBlue() * 255);
		return (a << 24) | (red << 16) | (green << 8) | blue;
	}

	static final int WHITE = colorToArgb(javafx.scene.paint.Color.WHITE);
	static final int RED = colorToArgb(javafx.scene.paint.Color.RED);
	static final int BLACK = colorToArgb(javafx.scene.paint.Color.BLACK);

	ImageCreatorJavaFx() {
		image = new WritableImage(32, 32);
		pixelWriter = image.getPixelWriter();
		clearBackground();
	}

	void setImageToStage(final MainClass mainClass) {
		Stage stage = mainClass.getStage();
		stage.getIcons().clear();
		stage.getIcons().add(image);
	}

	void drawPixelWhite(int x, int y) {
		pixelWriter.setArgb(x, y, WHITE);
	}

	void drawPixelRed(int x, int y) {
		pixelWriter.setArgb(x, y, RED);
	}

	void drawPixelBlack(int x, int y) {
		pixelWriter.setArgb(x, y, BLACK);
	}
}

class ImageCreatorAWT extends ImageCreatorBase {

	private final BufferedImage image;
	private final Graphics2D graphics;

	ImageCreatorAWT() {
		image = new BufferedImage(34, 34, BufferedImage.TYPE_INT_ARGB);
		graphics = image.createGraphics();
		graphics.setStroke(new BasicStroke(1));
		clearBackground();
	}

	@Override
	void drawPixelWhite(int x, int y) {
		graphics.setColor(java.awt.Color.WHITE);
		graphics.drawLine(x, y, x, y);
	}

	@Override
	void drawPixelRed(int x, int y) {
		graphics.setColor(java.awt.Color.RED);
		graphics.drawLine(x, y, x, y);
	}

	@Override
	void drawPixelBlack(int x, int y) {
		graphics.setColor(java.awt.Color.BLACK);
		graphics.drawLine(x, y, x, y);
	}

	void dispose() {
		graphics.dispose();
	}

	BufferedImage getImage() {
		return image;
	}
}
