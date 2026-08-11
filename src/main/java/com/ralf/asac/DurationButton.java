package com.ralf.asac;

import javafx.geometry.Side;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;

class DurationButton extends Button {

	DurationButton(final MainClass mainClass) {
		super(MainClass.messages.getString("DurationButton.set.duration"));

//		new Exception().printStackTrace();
//		System.out.println("Holla 1");

		final ContextMenu contextMenu = new ContextMenu();
		final MyDurationPopupListener listener = new MyDurationPopupListener(mainClass, contextMenu);

		final DurationPopup durationPopup = new DurationPopup();
		durationPopup.buildPopup(listener);

		this.setOnAction(event -> contextMenu.show(this, Side.BOTTOM, 0, 0));

	}

	class MyDurationPopupListener implements DurationPopupListener {
		private final MainClass mainClass;
		private final ContextMenu contextMenu;

		MyDurationPopupListener(final MainClass mainClass, final ContextMenu contextMenu) {
			this.mainClass = mainClass;
			this.contextMenu = contextMenu;
		}

		@Override
		public void setMenuItem(final int minute, final String minutesString) {
			final String margin = "            ";
			final String menuItemText = "\u00A0" + margin + minute + minutesString + margin;
			final MenuItem menuItem = new MenuItem(menuItemText);
			menuItem.setOnAction(event -> {
				new PerformDuration(minute, mainClass).start();
				mainClass.getSystray().hide();
			});
			contextMenu.getItems().add(menuItem);
		}

		@Override
		public void addSeparator() {
			contextMenu.getItems().add(new SeparatorMenuItem());
		}

	}

}
