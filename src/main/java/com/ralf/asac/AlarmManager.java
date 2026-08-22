package com.ralf.asac;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.ralf.asac.AlarmManager.AlarmManagerItem;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

class AlarmManager {
	private final ArrayList<AlarmManagerItem> alarmManagerItems;
	private final TableView<MyRow> tableView;
	private MyRow selectedItem;
	private final ArrayList<SoundManager.SoundManagerItem> soundItems;
	private final Button editButton;
	private final Button addButton;
	private final Button deleteButton;
	private final Button manageSoundsButton;
	private final Button defaultSoundButton;
	private final CheckBox minimizeToSystrayCheckBox;

	@SuppressWarnings("java:S106")
	AlarmManager(final MainClass mainClass) {
		final Stage stage = new Stage();
		stage.initOwner(mainClass.getStage());
		stage.initModality(Modality.APPLICATION_MODAL);
		stage.setTitle(MainClass.messages.getString("AlarmManager.title"));

		alarmManagerItems = Preferences.getAlarms();

		soundItems = Preferences.getSounds();

		final ObservableList<MyRow> tableItems = FXCollections.observableArrayList();
		tableView = new TableView<>(tableItems);

		rebuildListView();

		tableView.getSelectionModel().selectedItemProperty()
				.addListener((observable, oldValue, newValue) -> selectedItem = newValue);

		final double widthButtons = Double.parseDouble(MainClass.messages.getString("buttonwidth"));
		final double widthDefaultSoundButton = Double
				.parseDouble(MainClass.messages.getString("AlarmManager.default.sound.button.width"));
		final double widthManageSoundButton = Double
				.parseDouble(MainClass.messages.getString("AlarmManager.manage.sound.button.width"));
		final double widthMinimizeToSystrayCheckBox = Double
				.parseDouble(MainClass.messages.getString("AlarmManager.minimize.to.systray.width"));

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

		manageSoundsButton = new Button(MainClass.messages.getString("AlarmManager.manage.sounds"));
		manageSoundsButton.setPrefWidth(widthManageSoundButton);
		manageSoundsButton.setMinWidth(widthManageSoundButton);

		defaultSoundButton = new Button(MainClass.messages.getString("AlarmManager.default.sound.button"));
		defaultSoundButton.setPrefWidth(widthDefaultSoundButton);
		defaultSoundButton.setMinWidth(widthDefaultSoundButton);

		minimizeToSystrayCheckBox = new CheckBox(MainClass.messages.getString("AlarmManager.minimize.to.systray"));
		Preferences.SystrayMode systrayMode = Preferences.getSystrayMode();
		minimizeToSystrayCheckBox.setPrefWidth(widthMinimizeToSystrayCheckBox);
		minimizeToSystrayCheckBox.setMinWidth(widthMinimizeToSystrayCheckBox);
		if (systrayMode == Preferences.SystrayMode.MINIMIZE_TO_SYSTRAY) {
			minimizeToSystrayCheckBox.setSelected(true);
		}

		final Button okButton = new Button(MainClass.messages.getString("ok"));
		okButton.setPrefWidth(widthButtons);
		okButton.setMinWidth(widthButtons);

		buildTableView();

		final ScrollPane scrollPane = new ScrollPane();
		scrollPane.setContent(tableView);

		final GridPane gridPane = new GridPane();

		final Insets insets = new Insets(0, 0, 10, 10);

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

		final VBox vBox = new VBox(10);
		vBox.getChildren().addAll(manageSoundsButton, defaultSoundButton);

		gridPane.add(vBox, positionX, positionY, 3, 1);
		GridPane.setMargin(vBox, new Insets(50, 10, 0, 10));

		if (mainClass.getSystray().hasSystray()) {
			positionY++;

			gridPane.add(minimizeToSystrayCheckBox, positionX, positionY, 2, 1);
			GridPane.setMargin(minimizeToSystrayCheckBox, insets);
		}

		positionY++;

		gridPane.add(okButton, positionX, positionY, 1, 1);
		GridPane.setMargin(okButton, insets);

		tableView.getSelectionModel().selectedItemProperty().addListener(event -> {
			deleteButton.setDisable(false);
			editButton.setDisable(false);
		});

		setListener(mainClass.getMainPanel());

		final Scene scene = new Scene(gridPane);
		stage.setScene(scene);

		tableView.prefWidthProperty().bind(scene.widthProperty().add(600));

		okButton.setOnAction(event -> stage.hide());
		stage.showAndWait();
	}

	@SuppressWarnings("java:S3776")
	private void setListener(final MainPanel mainPanel) {
		Stage stage = mainPanel.getStage();

		editButton.setOnAction(event -> {
			if (selectedItem != null) {
				final AddEditAlarmManagerItem addEditAlarmManagerItem = new AddEditAlarmManagerItem(
						selectedItem.getName(), selectedItem.getTime(), selectedItem.getAlarmSoundData(), false,
						alarmManagerItems, stage);

				if (addEditAlarmManagerItem.isOk()) {
					final AlarmManagerItem alarmManagerItem = new AlarmManagerItem(addEditAlarmManagerItem.getName(),
							addEditAlarmManagerItem.getTime(), addEditAlarmManagerItem.getAlarmSoundData());
					alarmManagerItem.adjustAlarmSoundData(soundItems, false);
					if (selectedItem != null) {
						alarmManagerItems.set(selectedItem.index, alarmManagerItem);
					}
					rebuildListView();
					selectedItem(alarmManagerItem);
					Preferences.setAlarms(alarmManagerItems);
					mainPanel.getAlarmsComboBox().showStoredAlarms();

				}
			}
		});

		deleteButton.setOnAction(event -> {
			if (selectedItem == null)
				return;

			final Alert alert = new Alert(AlertType.CONFIRMATION);
			alert.setTitle("");
			alert.setHeaderText(MainClass.messages.getString("AlarmManager.shure.deleting.entry"));
			alert.setContentText(null);

			final Optional<ButtonType> result = alert.showAndWait();

			if (result.isPresent() && result.get() == ButtonType.OK) {
				alarmManagerItems.remove(selectedItem.getAlarmManagerItem());
				rebuildListView();
				Preferences.setAlarms(alarmManagerItems);
				mainPanel.getAlarmsComboBox().showStoredAlarms();
				deleteButton.setDisable(true);
				editButton.setDisable(true);
			}

		});

		addButton.setOnAction(event -> {
			AddEditAlarmManagerItem addEditAlarmManagerItem = new AddEditAlarmManagerItem("", "", null, true,
					alarmManagerItems, stage);
			if (addEditAlarmManagerItem.isOk()) {
				final AlarmManagerItem item = new AlarmManagerItem(addEditAlarmManagerItem.getName(),
						addEditAlarmManagerItem.getTime(), addEditAlarmManagerItem.getAlarmSoundData());
				item.adjustAlarmSoundData(soundItems, true);
				alarmManagerItems.add(item);
				rebuildListView();
				selectedItem(item);
				Preferences.setAlarms(alarmManagerItems);
				mainPanel.getAlarmsComboBox().showStoredAlarms();
			}
		});

		manageSoundsButton.setOnAction(event -> new SoundManager(stage, alarmManagerItems, this));

		defaultSoundButton.setOnAction(event -> {
			final Stage stageDefaultSound = new Stage();
			stageDefaultSound.setTitle(MainClass.messages.getString("AlarmManager.default.sound.window.title"));
			final List<AlarmSounds.AlarmSoundData> list = new AlarmSounds().getAlarmSoundDatas();

			final Label label = new Label(MainClass.messages.getString("AlarmManager.default.sound.title"));

			final ComboBox<AlarmSounds.AlarmSoundData> alarmSounds = new ComboBox<>();
			alarmSounds.getItems().clear();
			alarmSounds.getItems().addAll(list);

			alarmSounds.setValue(Preferences.getDefaultSound());

			final Button okButton = new Button(MainClass.messages.getString("ok"));
			final Button cancelButton = new Button(MainClass.messages.getString("cancel"));

			final HBox hBox = new HBox();
			hBox.getChildren().addAll(okButton, cancelButton);

			final VBox vBox = new VBox();
			vBox.getChildren().addAll(label, alarmSounds, hBox);

			okButton.setOnAction(event1 -> {
				Preferences.setDefaultSound(alarmSounds.getValue());
				stageDefaultSound.close();
			});

			cancelButton.setOnAction(event1 -> stageDefaultSound.close());

			stageDefaultSound.initModality(Modality.APPLICATION_MODAL);
			stageDefaultSound.initOwner(stage);
			final Scene scene = new Scene(vBox);
			stageDefaultSound.setScene(scene);
			stageDefaultSound.showAndWait();
		});

		minimizeToSystrayCheckBox.setOnAction(event -> Preferences
				.setSystrayMode(minimizeToSystrayCheckBox.isSelected() ? Preferences.SystrayMode.MINIMIZE_TO_SYSTRAY
						: Preferences.SystrayMode.NOT_IN_SYSTRAY)

		);
	}

	void selectedItem(final AlarmManagerItem item) {

		int row = 0;
		for (AlarmManagerItem tmpItem : alarmManagerItems) {
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

		tableView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
			if (newValue != null) {
				selectedItem = newValue;
			}
		});

	}

	void rebuildListView() {
		final ArrayList<MyRow> tableData = new ArrayList<>();
		int index = 0;
		for (AlarmManagerItem item : alarmManagerItems) {
			tableData.add(new MyRow(item, index++));
		}
		final ObservableList<MyRow> data = FXCollections.observableArrayList(tableData);
		tableView.setItems(data);
	}

	static class AlarmManagerItem {
		private final String name;
		private final String time;
		private AlarmSounds.AlarmSoundData alarmSoundData;

		AlarmManagerItem(final String name, final String time, final AlarmSounds.AlarmSoundData alarmSoundData) {
			this.name = name;
			this.time = time;
			this.alarmSoundData = alarmSoundData;
		}

		private void adjustAlarmSoundData(final ArrayList<SoundManager.SoundManagerItem> soundItems,
				final boolean isAdd) {
			if (alarmSoundData == null) {
				this.alarmSoundData = new AlarmSounds.AlarmSoundData(MainClass.messages.getString("path.default"));
			} else {
				boolean hasExistingSound = isAdd || checkWhetherAlarmExists(alarmSoundData.getName(), soundItems);
				if (!hasExistingSound || alarmSoundData.getPath() == null || alarmSoundData.getPath().isBlank()) {
					this.alarmSoundData = new AlarmSounds.AlarmSoundData(MainClass.messages.getString("path.default"));
				}
			}
		}

		private boolean checkWhetherAlarmExists(final String name,
				final ArrayList<SoundManager.SoundManagerItem> soundItems) {
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

		void setAlarmSoundData(final AlarmSounds.AlarmSoundData alarmSoundData) {
			this.alarmSoundData = alarmSoundData;
		}

		@Override
		public String toString() {
			final String soundName;
			final String soundPath;
			if (alarmSoundData == null) {
				soundName = "null";
				soundPath = "null";
			} else {
				soundName = alarmSoundData.getName();
				soundPath = alarmSoundData.getPath();
			}
			return "Name: " + name + " Time: " + time + " Sound name: " + soundName + " Sound path: " + soundPath;
		}
	}

	public static class MyRow {
		final AlarmManagerItem alarmManagerItem;
		private final Integer index;

		@SuppressWarnings("exports")
		public MyRow(final AlarmManagerItem item, final Integer index) {
			this.alarmManagerItem = item;
			this.index = index;
		}

		public String getName() {
			return alarmManagerItem.name;
		}

		public String getTime() {
			return alarmManagerItem.time;
		}

		@SuppressWarnings("exports")
		public AlarmSounds.AlarmSoundData getAlarmSoundData() {
			return alarmManagerItem.alarmSoundData;
		}

		public Integer getIndex() {
			return index;
		}

		AlarmManagerItem getAlarmManagerItem() {
			return alarmManagerItem;
		}

		@Override
		public String toString() {
			final String column1;
			final String column2;
			final String column3;

			if (alarmManagerItem == null) {
				column1 = null;
				column2 = null;
				column3 = null;
			} else {
				column1 = alarmManagerItem.name;
				column2 = alarmManagerItem.time;
				if (alarmManagerItem.alarmSoundData == null) {
					column3 = "null";
				} else {
					column3 = alarmManagerItem.alarmSoundData.getName();
				}
			}
			return "column1: " + column1 + " column2; " + column2 + " column3; " + column3;
		}
	}
}
