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
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.stage.Modality;
import javafx.stage.Stage;

class AlarmManager {
	private final ArrayList<AlarManagerItem> items;
	private final TableView<MyRow> tableView;
	private MyRow selectedItem;
	private final ArrayList<SoundManager.SoundManagerItem> soundItems;
	private final Button editButton;
	private final Button addButton;
	private final Button deleteButton;

	@SuppressWarnings("java:S106")
	AlarmManager(final MainClass mainClass) {
		final Stage stage = new Stage();
		stage.initOwner(mainClass.getStage());
		stage.initModality(Modality.APPLICATION_MODAL);
		stage.setTitle(MainClass.messages.getString("AlarmManager.title"));

		items = Preferences.getAlarms();

		soundItems = Preferences.getSounds();

		System.out.println("Holla 1 " + soundItems);

		final ObservableList<MyRow> tableItems = FXCollections.observableArrayList();
		tableView = new TableView<>(tableItems);

		rebuildListView();

		tableView.getSelectionModel().selectedItemProperty().addListener((_, _, newValue) -> selectedItem = newValue);

		final double widthButtons = Double.parseDouble(MainClass.messages.getString("buttonwidth"));

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

		final GridPane gridPane = new GridPane();

		Insets insets = new Insets(0, 0, 10, 10);

		int positionX = 0;
		int positionY = 0;

		gridPane.add(scrollPane, positionX, positionY, 1, 3);
		GridPane.setHgrow(scrollPane, Priority.ALWAYS);

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

		setListener(stage, mainClass);

		final Scene scene = new Scene(gridPane);
		stage.setScene(scene);

		tableView.prefWidthProperty().bind(scene.widthProperty().add(600));

		okButton.setOnAction(_ -> stage.hide());
		stage.showAndWait();
	}

	@SuppressWarnings("java:S3776")
	private void setListener(Stage stage, MainClass mainClass) {
		editButton.setOnAction(_ -> {
			if (selectedItem != null) {
				AddEditAlarmManagerItem addEditAlarmManagerItem = new AddEditAlarmManagerItem(selectedItem.getName(),
						selectedItem.getTime(), selectedItem.getAlarmSoundData(), false, items, stage, this);

				if (addEditAlarmManagerItem.isOk()) {
					AlarManagerItem item = new AlarManagerItem(addEditAlarmManagerItem.getName(),
							addEditAlarmManagerItem.getTime(), addEditAlarmManagerItem.getAlarmSoundData());
					item.adjustAlarmSoundData(soundItems);
					if (selectedItem != null) {
						items.set(selectedItem.index, item);
					}
					rebuildListView();
					selectedItem(item);
					Preferences.setAlarms(items);
					mainClass.showStoredAlarms();

				}
			}
		});

		deleteButton.setOnAction(_ -> {
			if (selectedItem == null)
				return;

			final Alert alert = new Alert(AlertType.CONFIRMATION);
			alert.setTitle("");
			alert.setHeaderText(MainClass.messages.getString("AlarmManager.shure.deleting.entry"));
			alert.setContentText(null);

			final Optional<ButtonType> result = alert.showAndWait();

			if (result.isPresent() && result.get() == ButtonType.OK) {
				items.remove(selectedItem.getItem());
				rebuildListView();
				Preferences.setAlarms(items);
				mainClass.showStoredAlarms();
				deleteButton.setDisable(true);
				editButton.setDisable(true);
			}

		});

		addButton.setOnAction(_ -> {
			AddEditAlarmManagerItem addEditAlarmManagerItem = new AddEditAlarmManagerItem("", "", null, true, items,
					stage, this);
			if (addEditAlarmManagerItem.isOk()) {
				AlarManagerItem item = new AlarManagerItem(addEditAlarmManagerItem.getName(),
						addEditAlarmManagerItem.getTime(), addEditAlarmManagerItem.getAlarmSoundData());
				item.adjustAlarmSoundData(soundItems);
				items.add(item);
				rebuildListView();
				selectedItem(item);
				Preferences.setAlarms(items);
				mainClass.showStoredAlarms();
			}
		});
	}

	void selectedItem(AlarManagerItem item) {

		int row = 0;
		for (AlarManagerItem tmpItem : items) {
			if (item.name.equals(tmpItem.name)) {
				tableView.getSelectionModel().select(row);
				break;
			}
			row++;
		}
	}

	@SuppressWarnings("unchecked")
	void buildTableView() {
		final TableColumn<MyRow, String> tableColumn1 = new TableColumn<>(
				MainClass.messages.getString("AlarmManager.name"));
		tableColumn1.setCellValueFactory(new PropertyValueFactory<>("name"));

		final TableColumn<MyRow, Integer> tableColumn2 = new TableColumn<>(
				MainClass.messages.getString("AlarmManager.time"));
		tableColumn2.setCellValueFactory(new PropertyValueFactory<>("time"));

		final TableColumn<MyRow, Integer> tableColumn3 = new TableColumn<>(
				MainClass.messages.getString("AlarmManager.sound"));
		tableColumn3.setCellValueFactory(new PropertyValueFactory<>("alarmSoundData"));

		tableView.getColumns().addAll(tableColumn1, tableColumn2, tableColumn3);

		rebuildListView();

		tableView.getSelectionModel().selectedItemProperty().addListener((_, _, newValue) -> {
			if (newValue != null) {
				selectedItem = newValue;
			}
		});

	}

	void rebuildListView() {
		final ArrayList<MyRow> tableData = new ArrayList<>();
		int index = 0;
		for (AlarManagerItem item : items) {
			tableData.add(new MyRow(item, index++));
		}
		final ObservableList<MyRow> data = FXCollections.observableArrayList(tableData);
		tableView.setItems(data);
	}

	static class AlarManagerItem {
		private final String name;
		private final String time;
		private AlarmSounds.AlarmSoundData alarmSoundData;

		AlarManagerItem(final String name, final String time, final AlarmSounds.AlarmSoundData alarmSoundData) {
			this.name = name;
			this.time = time;
			this.alarmSoundData = alarmSoundData;
		}

		private void adjustAlarmSoundData(ArrayList<SoundManager.SoundManagerItem> soundItems) {
			new Exception().printStackTrace();

			boolean hasExistingSound = checkWhetherAlarmExists(alarmSoundData.getName(), soundItems);

			if (!hasExistingSound || alarmSoundData.getPath() == null || alarmSoundData.getPath().isBlank()) {
				this.alarmSoundData = new AlarmSounds.AlarmSoundData(MainClass.messages.getString("path.default"));
			}
		}

		boolean checkWhetherAlarmExists(final String name, ArrayList<SoundManager.SoundManagerItem> soundItems) {
			if (soundItems != null) {
				for (SoundManager.SoundManagerItem soundItem : soundItems) {
					if (name.equals(soundItem.getName())) {
						return true;
					}
				}
			}
			return false;
		}

		String getName() {
			return name;
		}

		String getTime() {
			return time;
		}

		AlarmSounds.AlarmSoundData getAlarmSoundData() {
			return alarmSoundData;
		}

		void setAlarmSoundData(AlarmSounds.AlarmSoundData alarmSoundData) {
			this.alarmSoundData = alarmSoundData;
		}

		@Override
		public String toString() {
			return "Name: " + name + " Time: " + time + " Sound name: " + alarmSoundData.getName() + " Sound path: "
					+ alarmSoundData.getPath();
		}
	}

	public static class MyRow {
		final AlarManagerItem item;
		private final Integer index;

		@SuppressWarnings("exports")
		public MyRow(AlarManagerItem item, Integer index) {
			this.item = item;
			this.index = index;
		}

		public String getName() {
			return item.name;
		}

		public String getTime() {
			return item.time;
		}

		@SuppressWarnings("exports")
		public AlarmSounds.AlarmSoundData getAlarmSoundData() {
			return item.alarmSoundData;
		}

		public Integer getIndex() {
			return index;
		}

		AlarManagerItem getItem() {
			return item;
		}

		@Override
		public String toString() {
			return "column1: " + item.name + " column2; " + item.time + " column3; " + item.alarmSoundData.getName();
		}
	}

}
