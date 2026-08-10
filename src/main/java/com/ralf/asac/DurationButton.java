package com.ralf.asac;

import javafx.geometry.Side;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.stage.Popup;
import javafx.stage.Stage;

class DurationButton extends Button {

	DurationButton(final Stage stage, final MainClass mainClass, Popup sysTrayPopup) {
		super(MainClass.messages.getString("DurationButton.set.duration"));

		final ContextMenu contextMenu = new ContextMenu();
		final MyDurationPopupListener listener = new MyDurationPopupListener(stage, mainClass, contextMenu,
				sysTrayPopup);

		final DurationPopup durationPopup = new DurationPopup();
		durationPopup.buildPopup(listener);

		this.setOnAction(event -> contextMenu.show(this, Side.BOTTOM, 0, 0));

	}

	class MyDurationPopupListener implements DurationPopupListener {
		private final Stage stage;
		private final MainClass mainClass;
		private final ContextMenu contextMenu;
		private final Popup sysTrayPopup;

		MyDurationPopupListener(final Stage stage, final MainClass mainClass, final ContextMenu contextMenu,
				Popup sysTrayPopup) {
			this.stage = stage;
			this.mainClass = mainClass;
			this.contextMenu = contextMenu;
			this.sysTrayPopup = sysTrayPopup;
		}

		@Override
		public void setMenuItem(final int minute, final String minutesString) {
			final String margin = "            ";
			final String menuItemText = "\u00A0" + margin + minute + minutesString + margin;
			final MenuItem menuItem = new MenuItem(menuItemText);
			menuItem.setOnAction(event -> {
				new PerformDuration(minute, stage, mainClass);
				if (sysTrayPopup != null) {
					sysTrayPopup.hide();
				}
			});
			contextMenu.getItems().add(menuItem);
		}

		@Override
		public void addSeparator() {
			contextMenu.getItems().add(new SeparatorMenuItem());
		}

	}

}
