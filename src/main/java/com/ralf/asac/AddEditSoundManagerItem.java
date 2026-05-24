package com.ralf.asac;

import java.io.File;
import java.util.ArrayList;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Modality;
import javafx.stage.Stage;

class AddEditSoundManagerItem {
	private final String name;
	private final TextField nameTextField;
	private final TextField pathFieldTextField;
	private final Button selectFileButton;
	private final Button okButton;
	private final Label errorLabel;
	private final boolean isNewEntry;
	private final ArrayList<SoundManager.SoundManagerItem> items;
	private boolean isOkAttribute;

	AddEditSoundManagerItem(final String name, final String path, final boolean isNewEntry,
			ArrayList<SoundManager.SoundManagerItem> items, Stage ownerStage) {
		final Stage stage = new Stage();
		this.name = name;
		this.isNewEntry = isNewEntry;
		this.items = items;

		stage.setTitle(MainClass.messages
				.getString(isNewEntry ? "AddEditSoundManagerItem.title.new" : "AddEditSoundManagerItem.title.edit"));

		nameTextField = new TextField(name);
		pathFieldTextField = new TextField(path);
		selectFileButton = new Button(MainClass.messages.getString("selectFile"));
		okButton = new Button(MainClass.messages.getString("ok"));
		okButton.setDisable(isNewEntry);

		errorLabel = new Label();
		errorLabel.setTextFill(Color.RED);
		errorLabel.setVisible(false);

		final Button cancelButton = new Button(MainClass.messages.getString("cancel"));

		okButton.setOnAction(_ -> {
			isOkAttribute = true;
			stage.close();
		});

		cancelButton.setOnAction(_ -> stage.close());

		final VBox vBoxName = new VBox();
		vBoxName.getChildren().addAll(new Label(MainClass.messages.getString("AddEditSoundManagerItem.name")),
				nameTextField);

		final VBox vBoxPath = new VBox();
		vBoxPath.getChildren().addAll(new Label(MainClass.messages.getString("path")), pathFieldTextField);

		final VBox vBoxPathButton = new VBox();
		vBoxPathButton.getChildren().addAll(new Label(""), selectFileButton);
		VBox.setMargin(selectFileButton, new Insets(0, 10, 0, 0));

		final HBox valuesHBox = new HBox(10);
		valuesHBox.getChildren().addAll(vBoxName, vBoxPath, vBoxPathButton);
		HBox.setMargin(vBoxName, new Insets(0, 0, 20, 10));

		final HBox buttonsHBox = new HBox(10);
		buttonsHBox.getChildren().addAll(okButton, cancelButton);
		HBox.setMargin(okButton, new Insets(0, 0, 10, 10));

		final VBox vBox = new VBox();
		vBox.getChildren().addAll(valuesHBox, buttonsHBox, errorLabel);

		selectFileButton.setOnAction(_ -> {

			final ExtensionFilter filter = new ExtensionFilter("Audio Files", "*.wav", "*.mp3", "*.aac");

			final FileChooser fileChooserOpen = new FileChooser();
			fileChooserOpen.setTitle(MainClass.messages.getString("AddEditSoundManagerItem.select.audio.file"));
			fileChooserOpen.getExtensionFilters().addAll(filter);

			final File file = fileChooserOpen.showOpenDialog(stage);
			if (file != null) {
				pathFieldTextField.setText(file.toString());
				checkTextFields();
			}

		});

		nameTextField.setOnKeyReleased(_ -> checkTextFields());
		pathFieldTextField.setOnKeyReleased(_ -> checkTextFields());

		stage.initModality(Modality.APPLICATION_MODAL);
		stage.initOwner(ownerStage);
		final Scene scene = new Scene(vBox);
		stage.setScene(scene);
		stage.showAndWait();
	}

	private void checkTextFields() {

		boolean entryAlreadyExists = testOnNameAlreadyExist();

		String currentName = nameTextField.getText();
		if (isNewEntry) {
			if (entryAlreadyExists) {
				setErrorLabel(MainClass.messages.getString("AddEditSoundManagerItem.entry.already.exists"));
			} else {
				setErrorLabel(checkFileName());
			}
		} else {
			if (!currentName.equals(name) && entryAlreadyExists) {
				setErrorLabel(MainClass.messages.getString("AddEditSoundManagerItem.entry.already.exists"));
				return;
			}

			if (currentName.isBlank()) {
				okButton.setDisable(true);
			} else {
				setErrorLabel(checkFileName());
			}
		}
	}

	private void setErrorLabel(String errorText) {
		if (errorText != null) {
			errorLabel.setText(errorText);
			errorLabel.setVisible(true);
			okButton.setDisable(true);
		} else {
			errorLabel.setVisible(false);
			okButton.setDisable(false);
		}
	}

	private String checkFileName() {
		String text = pathFieldTextField.getText().strip();
		File file = new File(text);

		if (!file.exists()) {
			return MainClass.messages.getString("AddEditSoundManagerItem.file.does.not.exists");
		}

		if (!file.isFile()) {
			return MainClass.messages.getString("AddEditSoundManagerItem.file.is.not.a.file");
		}

		if (text.endsWith(".wav") || text.endsWith(".mp3") || text.endsWith(".aac")) {
			return null;
		}
		return MainClass.messages.getString("AddEditSoundManagerItem.file.is.not.a.sound.file");
	}

	boolean testOnNameAlreadyExist() {
		String currentName = nameTextField.getText();
		for (SoundManager.SoundManagerItem item : items) {
			if (currentName.equals(item.getName())) {
				return true;
			}
		}
		return false;
	}

	String getName() {
		return nameTextField.getText();
	}

	String getPath() {
		return pathFieldTextField.getText();
	}

	boolean isOk() {
		return isOkAttribute;
	}
}
