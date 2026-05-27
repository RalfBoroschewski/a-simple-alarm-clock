package com.ralf.asac;

import java.util.ArrayList;
import java.util.List;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;

class AddEditAlarmManagerItem {

	private final TextField nameTextField;
	private final TimeDurationField timeTextField;
	private final Button okButton;
	private final ComboBox<AlarmSounds.AlarmSoundData> alarmSoundDatasComboBox;
	private final Button manageSoundsButton;
	private final Label errorLabel;
	private final ArrayList<AlarmManager.AlarmManagerItem> alarmManagerItems;
	private final boolean isNewEntry;
	private boolean isOkAttribute;
	private final String name;

	AddEditAlarmManagerItem(final String name, final String time, final AlarmSounds.AlarmSoundData alarmSoundData,
			final boolean isNewEntry, final ArrayList<AlarmManager.AlarmManagerItem> alarmManagerItems,
			final Stage ownerStage, final AlarmManager alarmManager) {
		this.name = name;
		final Stage stage = new Stage();
		this.isNewEntry = isNewEntry;
		this.alarmManagerItems = alarmManagerItems;

		stage.setTitle(MainClass.messages
				.getString(isNewEntry ? "AddEditAlarmManagerItem.title.new" : "AddEditAlarmManagerItem.title.edit"));

		nameTextField = new TextField(name);
		timeTextField = new TimeDurationField(time);
		alarmSoundDatasComboBox = new ComboBox<>();
		manageSoundsButton = new Button(MainClass.messages.getString("AddEditAlarmManagerItem.manage.sounds"));

		okButton = new Button(MainClass.messages.getString("ok"));
		okButton.setDisable(name.isBlank() || time.isBlank());

		final Button cancelButton = new Button(MainClass.messages.getString("cancel"));

		errorLabel = new Label();
		errorLabel.setTextFill(Color.RED);
		errorLabel.setVisible(false);

		manageSoundsButton.setOnAction(_ -> new SoundManager(this, stage, alarmManagerItems, alarmManager));

		okButton.setOnAction(_ -> {
			isOkAttribute = true;
			stage.close();
		});
		cancelButton.setOnAction(_ -> stage.close());

		final VBox vBoxName = new VBox();
		vBoxName.getChildren().addAll(new Label(MainClass.messages.getString("AddEditAlarmManagerItem.name")),
				nameTextField);

		final VBox vBoxTime = new VBox();
		vBoxTime.getChildren().addAll(new Label(MainClass.messages.getString("AddEditAlarmManagerItem.time")),
				timeTextField);
		VBox.setMargin(timeTextField, new Insets(0, 10, 0, 0));

		final HBox valuesHBox = new HBox(10);
		valuesHBox.getChildren().addAll(vBoxName, vBoxTime);
		HBox.setMargin(vBoxName, new Insets(0, 0, 0, 10));

		final HBox buttonsHBox = new HBox(20);
		buttonsHBox.getChildren().addAll(okButton, cancelButton);
		HBox.setMargin(okButton, new Insets(0, 0, 10, 10));

		final Label alarmSoundsCaption = new Label(MainClass.messages.getString("AddEditAlarmManagerItem.sound"));

		buildAlarmsComboBox();

		final VBox soundComboBoxVBox = new VBox();
		soundComboBoxVBox.getChildren().addAll(alarmSoundsCaption, alarmSoundDatasComboBox);
		VBox.setMargin(alarmSoundsCaption, new Insets(10, 0, 0, 10));
		VBox.setMargin(alarmSoundDatasComboBox, new Insets(0, 0, 20, 10));
		alarmSoundDatasComboBox.setValue(alarmSoundData);

		final HBox alarms = new HBox(10);
		alarms.getChildren().addAll(soundComboBoxVBox, manageSoundsButton);

		final VBox vBox = new VBox();
		vBox.getChildren().addAll(valuesHBox, alarmSoundsCaption, alarms, buttonsHBox, errorLabel);

		nameTextField.setOnKeyReleased(_ -> checkTextFields());
		timeTextField.setOnKeyReleased(_ -> checkTextFields());

		stage.initModality(Modality.APPLICATION_MODAL);
		stage.initOwner(ownerStage);
		final Scene scene = new Scene(vBox);
		stage.setScene(scene);
		stage.showAndWait();
	}

	void checkTextFields() {
		boolean entryAlreadyExists = false;

		final String currentName = nameTextField.getText();
		if (isNewEntry) {
			entryAlreadyExists = testOnNameAlreadyExist();
		} else {
			if (!currentName.equals(name)) {
				entryAlreadyExists = testOnNameAlreadyExist();
			}
		}

		if (entryAlreadyExists) {
			errorLabel.setText(MainClass.messages.getString("AddEditAlarmManagerItem.entry.already.exists"));
			errorLabel.setVisible(true);
		} else {
			errorLabel.setVisible(false);
		}

		okButton.setDisable(entryAlreadyExists || currentName.isBlank() || timeTextField.getText().isEmpty());
	}

	boolean testOnNameAlreadyExist() {
		final String currentName = nameTextField.getText();
		for (AlarmManager.AlarmManagerItem item : alarmManagerItems) {
			if (currentName.equals(item.getName())) {
				return true;
			}
		}
		return false;
	}

	void buildAlarmsComboBox() {
		final List<AlarmSounds.AlarmSoundData> list = new AlarmSounds().getAlarmSoundDatas();

		alarmSoundDatasComboBox.getItems().clear();
		alarmSoundDatasComboBox.getItems().addAll(list);
	}

	String getName() {
		return nameTextField.getText();
	}

	String getTime() {
		return timeTextField.getText();
	}

	AlarmSounds.AlarmSoundData getAlarmSoundData() {
		return alarmSoundDatasComboBox.getValue();
	}

	boolean isOk() {
		return isOkAttribute;
	}
}
