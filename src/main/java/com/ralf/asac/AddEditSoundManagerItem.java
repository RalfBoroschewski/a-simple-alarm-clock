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

		final double pathFieldTextFieldWidth = 400;
		final double marginLeftWindow = 20;
		final double marginRightWindow = 20;
		final double marginTopPathItems = 20;
		final double marginTopCaptionFields = 20;

		pathFieldTextField.setMinWidth(pathFieldTextFieldWidth);
		pathFieldTextField.setPrefWidth(pathFieldTextFieldWidth);
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

		VBox.setMargin(nameTextField, new Insets(marginTopCaptionFields, marginRightWindow, 0, marginLeftWindow));
		Label nameLabel = new Label(MainClass.messages.getString("AddEditSoundManagerItem.name"));
		VBox.setMargin(nameLabel, new Insets(0, marginRightWindow, 0, marginLeftWindow));
		vBoxName.getChildren().addAll(nameLabel, nameTextField);

		final VBox vBoxPath = new VBox();
		Label pathLabel = new Label(MainClass.messages.getString("path"));
		vBoxPath.getChildren().addAll(pathLabel, pathFieldTextField);
		VBox.setMargin(pathLabel, new Insets(marginTopPathItems, marginRightWindow, 0, marginLeftWindow));
		VBox.setMargin(pathFieldTextField, new Insets(marginTopCaptionFields, marginRightWindow, 0, marginLeftWindow));

		final VBox vBoxPathButton = new VBox();
		Label emptyLabel = new Label("");
		vBoxPathButton.getChildren().addAll(emptyLabel, selectFileButton);
		VBox.setMargin(emptyLabel, new Insets(marginTopPathItems, 0, 0, 0));
		VBox.setMargin(selectFileButton, new Insets(marginTopCaptionFields, marginRightWindow, 0, 0));

		final HBox hBoxPathItems = new HBox();
		hBoxPathItems.getChildren().addAll(vBoxPath, vBoxPathButton);

		final HBox buttonsHBox = new HBox(10);
		buttonsHBox.getChildren().addAll(okButton, cancelButton);
		HBox.setMargin(okButton, new Insets(20, 0, 10, marginLeftWindow));
		HBox.setMargin(cancelButton, new Insets(20, 0, 10, 100));

		final VBox vBox = new VBox();
		vBox.getChildren().addAll(vBoxName, hBoxPathItems, buttonsHBox, errorLabel);

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
