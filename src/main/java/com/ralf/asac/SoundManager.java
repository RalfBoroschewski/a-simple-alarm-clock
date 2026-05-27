package com.ralf.asac;

import java.util.ArrayList;
import java.util.Optional;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

class SoundManager {

	private final ArrayList<SoundManagerItem> soundItems;
	private final TableView<MyRow> tableView;
	MyRow selectedItem;
	private final Button editButton;
	private final Button deleteButton;
	private final Button addButton;
	private final Stage stage;

	SoundManager(final AddEditAlarmManagerItem addEditAlarmManagerItem, final Stage ownerStage,
			final ArrayList<AlarmManager.AlarmManagerItem> alarmManagerItems, final AlarmManager alarmManager) {
		soundItems = Preferences.getSounds();

		final ObservableList<MyRow> tableItems = FXCollections.observableArrayList();
		tableView = new TableView<>(tableItems);

		stage = new Stage();
		stage.setTitle(MainClass.messages.getString("SoundManager.title"));

		rebuildListView();

		tableView.getSelectionModel().selectedItemProperty().addListener((_, _, newValue) -> selectedItem = newValue);

		final double widthButtons = 100;

		editButton = new Button(MainClass.messages.getString("edit"));
		editButton.setDisable(true);
		editButton.setPrefWidth(widthButtons);
		editButton.setMinWidth(widthButtons);

		deleteButton = new Button(MainClass.messages.getString("delete"));
		deleteButton.setDisable(true);
		deleteButton.setPrefWidth(widthButtons);
		deleteButton.setMinWidth(widthButtons);

		addButton = new Button(MainClass.messages.getString("add"));
		addButton.setPrefWidth(widthButtons);
		addButton.setMinWidth(widthButtons);

		final var okButton = new Button(MainClass.messages.getString("ok"));
		okButton.setPrefWidth(widthButtons);
		okButton.setMinWidth(widthButtons);

		buildTableView();

		final ScrollPane scrollPane = new ScrollPane();
		scrollPane.setContent(tableView);

		scrollPane.setHbarPolicy(ScrollBarPolicy.AS_NEEDED);
		scrollPane.setVbarPolicy(ScrollBarPolicy.AS_NEEDED);

		final GridPane gridPane = new GridPane();

		Insets insets = new Insets(0, 0, 10, 10);

		int positionX = 0;
		int positionY = 0;

		gridPane.add(scrollPane, positionX, positionY, 1, 3);

		positionX++;

		gridPane.add(editButton, positionX, positionY, 1, 1);
		GridPane.setMargin(editButton, insets);

		positionX++;

		gridPane.add(deleteButton, positionX, positionY, 1, 1);
		GridPane.setMargin(deleteButton, insets);

		positionX++;

		gridPane.add(addButton, positionX, positionY, 1, 1);
		GridPane.setMargin(addButton, new Insets(0, 10, 10, 10));

		positionX = 1;
		positionY++;

		gridPane.add(okButton, positionX, positionY, 1, 1);
		GridPane.setMargin(okButton, insets);

		tableView.getSelectionModel().selectedItemProperty().addListener(_ -> {
			deleteButton.setDisable(false);
			editButton.setDisable(false);
		});

		setListener(addEditAlarmManagerItem, alarmManagerItems, alarmManager);

		final Scene scene = new Scene(gridPane);
		stage.setScene(scene);

		stage.initOwner(ownerStage); // <-- Set the parent window here
		stage.initModality(Modality.APPLICATION_MODAL);

		tableView.prefWidthProperty().bind(scene.widthProperty().add(600));

		okButton.setOnAction(_ -> stage.hide());
		stage.showAndWait();
	}

	@SuppressWarnings("java:S3776")
	private void setListener(final AddEditAlarmManagerItem addEditAlarmManagerItem,
			final ArrayList<AlarmManager.AlarmManagerItem> alarmManagerItems, final AlarmManager alarmManager) {
		editButton.setOnAction(_ -> {
			if (selectedItem != null) {
				final String selectedName = selectedItem.getName();
				final AddEditSoundManagerItem addEditSoundManagerItem = new AddEditSoundManagerItem(selectedName,
						selectedItem.getPath(), false, soundItems, stage);

				final String newName = addEditSoundManagerItem.getName();
				final String newPath = addEditSoundManagerItem.getPath();

				if (addEditSoundManagerItem.isOk()) {
					boolean hasAChangedItem = false;
					for (AlarmManager.AlarmManagerItem alarmManagerItem : alarmManagerItems) {
						if (alarmManagerItem.getAlarmSoundData().getName().equals(selectedName)) {

							alarmManagerItem.setAlarmSoundData(new AlarmSounds.AlarmSoundData(newName, newPath));
							hasAChangedItem = true;
						}
					}

					if (hasAChangedItem) {
						Preferences.setAlarms(alarmManagerItems);
					}

					final SoundManagerItem item = new SoundManagerItem(newName, newPath);
					soundItems.set(selectedItem.index, item);
					rebuildListView();
					Preferences.setSounds(soundItems);
					addEditAlarmManagerItem.buildAlarmsComboBox();
					alarmManager.rebuildListView();
				}
			}
		});

		addButton.setOnAction(_ -> {
			final AddEditSoundManagerItem addEditSoundManagerItem = new AddEditSoundManagerItem("", "", true,
					soundItems, stage);

			if (addEditSoundManagerItem.isOk()) {
				final SoundManagerItem item = new SoundManagerItem(addEditSoundManagerItem.getName(),
						addEditSoundManagerItem.getPath());
				soundItems.add(item);
				rebuildListView();
				Preferences.setSounds(soundItems);
				addEditAlarmManagerItem.buildAlarmsComboBox();
			}
		});

		deleteButton.setOnAction(_ -> {
			if (selectedItem == null)
				return;

			for (AlarmManager.AlarmManagerItem alarmManagerItem : alarmManagerItems) {
				if (alarmManagerItem.getAlarmSoundData().getName().equals(selectedItem.getItem().name)) {
					final Alert errorAlert = new Alert(AlertType.ERROR);
					errorAlert.setTitle("");
					errorAlert.setHeaderText(MainClass.messages.getString("SoundManager.deleting.error"));
					errorAlert.setContentText(null);
					errorAlert.showAndWait();
					return;
				}
			}

			final Alert confirmationAlert = new Alert(AlertType.CONFIRMATION);
			confirmationAlert.setTitle("");
			confirmationAlert.setHeaderText(MainClass.messages.getString("SoundManager.shure.deleting.entry"));
			confirmationAlert.setContentText(null);

			final Optional<ButtonType> result = confirmationAlert.showAndWait();

			if (result.isPresent() && result.get() == ButtonType.OK) {
				soundItems.remove(selectedItem.getItem());
				rebuildListView();
				Preferences.setSounds(soundItems);
				addEditAlarmManagerItem.buildAlarmsComboBox();
				deleteButton.setDisable(true);
				editButton.setDisable(true);
			}

		});

	}

	@SuppressWarnings("unchecked")
	void buildTableView() {
		final TableColumn<MyRow, String> tableColumn1 = new TableColumn<>(
				MainClass.messages.getString("SoundManager.name"));
		tableColumn1.setCellValueFactory(new PropertyValueFactory<>("name"));

		final TableColumn<MyRow, Integer> tableColumn2 = new TableColumn<>(
				MainClass.messages.getString("SoundManager.path"));
		tableColumn2.setCellValueFactory(new PropertyValueFactory<>("path"));

		tableView.getColumns().addAll(tableColumn1, tableColumn2);

		rebuildListView();

		tableView.getSelectionModel().selectedItemProperty().addListener((_, _, newValue) -> {
			if (newValue != null) {
				selectedItem = newValue;
			}
		});

	}

	void rebuildListView() {
		ArrayList<MyRow> tableData = new ArrayList<>();
		int index = 0;
		for (SoundManagerItem item : soundItems) {
			tableData.add(new MyRow(item, index++));
		}
		ObservableList<MyRow> data = FXCollections.observableArrayList(tableData);
		tableView.setItems(data);

	}

	static class SoundManagerItem {
		private final String name;
		private final String path;

		SoundManagerItem(final String name, final String path) {
			this.name = name;
			this.path = path;
		}

		String getName() {
			return name;
		}

		String getPath() {
			return path;
		}

		@Override
		public String toString() {
			return "Name: " + name + " Path: " + path;
		}
	}

	public static class MyRow {
		final SoundManagerItem item;
		private final Integer index;

		MyRow(final SoundManagerItem item, final Integer index) {
			this.item = item;
			this.index = index;
		}

		public String getName() {
			return item.name;
		}

		public String getPath() {
			return item.path;
		}

		Integer getIndex() {
			return index;
		}

		SoundManagerItem getItem() {
			return item;
		}

		@Override
		public String toString() {
			return "column1: " + item.name + " column2; " + item.path;
		}
	}
}
