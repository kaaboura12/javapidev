package Controllers.backforum;

import Controllers.Basebackcontroller;
import Models.Badword;
import Services.ServiceBadword;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.GridPane;
import javafx.util.Callback;

import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

public class BadWordsController implements Initializable, Basebackcontroller.BaseController {

    @FXML private TextField wordField;
    @FXML private TextField replacementField;
    @FXML private Button addButton;
    @FXML private TableView<Badword> badWordsTable;
    @FXML private TableColumn<Badword, String> wordColumn;
    @FXML private TableColumn<Badword, String> replacementColumn;
    @FXML private TableColumn<Badword, Void> actionsColumn;

    private ServiceBadword badwordService;
    private ObservableList<Badword> badwordsList;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        badwordService = new ServiceBadword();
        badwordsList = FXCollections.observableArrayList();

        // Initialize table columns
        wordColumn.setCellValueFactory(new PropertyValueFactory<>("word"));
        replacementColumn.setCellValueFactory(new PropertyValueFactory<>("replacement"));
        setupActionsColumn();

        // Load bad words
        loadBadWords();
    }

    private void loadBadWords() {
        badwordsList.clear();
        badwordsList.addAll(badwordService.getAllBadwords());
        badWordsTable.setItems(badwordsList);
    }

    @FXML
    private void handleAddWord() {
        String word = wordField.getText().trim();
        String replacement = replacementField.getText().trim();

        if (word.isEmpty() || replacement.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error", "Invalid Input", 
                     "Please enter both a word and its replacement.");
            return;
        }

        Badword newBadword = new Badword(word, replacement);
        if (badwordService.addBadword(newBadword)) {
            showAlert(Alert.AlertType.INFORMATION, "Success", "Word Added", 
                     "The bad word has been added successfully.");
            wordField.clear();
            replacementField.clear();
            loadBadWords();
        } else {
            showAlert(Alert.AlertType.ERROR, "Error", "Add Failed", 
                     "Failed to add the bad word. Please try again.");
        }
    }

    private void setupActionsColumn() {
        actionsColumn.setCellFactory(column -> new TableCell<>() {
            private final Button editButton = new Button("Edit");
            private final Button deleteButton = new Button("Delete");
            private final HBox buttonsBox = new HBox(5, editButton, deleteButton);

            {
                editButton.getStyleClass().add("edit-button");
                deleteButton.getStyleClass().add("delete-button");

                editButton.setOnAction(event -> {
                    Badword badword = getTableView().getItems().get(getIndex());
                    handleEdit(badword);
                });

                deleteButton.setOnAction(event -> {
                    Badword badword = getTableView().getItems().get(getIndex());
                    handleDelete(badword);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(buttonsBox);
                }
            }
        });
    }

    private void handleEdit(Badword badword) {
        Dialog<Badword> dialog = new Dialog<>();
        dialog.setTitle("Edit Bad Word");
        dialog.setHeaderText("Edit bad word and its replacement");

        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        TextField wordField = new TextField(badword.getWord());
        TextField replacementField = new TextField(badword.getReplacement());

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.add(new Label("Word:"), 0, 0);
        grid.add(wordField, 1, 0);
        grid.add(new Label("Replacement:"), 0, 1);
        grid.add(replacementField, 1, 1);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                badword.setWord(wordField.getText());
                badword.setReplacement(replacementField.getText());
                return badword;
            }
            return null;
        });

        Optional<Badword> result = dialog.showAndWait();
        result.ifPresent(updatedBadword -> {
            if (badwordService.updateBadword(updatedBadword)) {
                loadBadWords();
                showAlert(Alert.AlertType.INFORMATION, "Success", "Update Successful", 
                         "The bad word has been updated.");
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Update Failed", 
                         "Failed to update the bad word.");
            }
        });
    }

    private void handleDelete(Badword badword) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Delete");
        alert.setHeaderText("Delete Bad Word");
        alert.setContentText("Are you sure you want to delete this bad word?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            if (badwordService.deleteBadword(badword.getId())) {
                loadBadWords();
                showAlert(Alert.AlertType.INFORMATION, "Success", "Delete Successful", 
                         "The bad word has been deleted.");
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Delete Failed", 
                         "Failed to delete the bad word.");
            }
        }
    }

    private void showAlert(Alert.AlertType alertType, String title, String header, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @Override
    public void setCurrentUser(String username) {
        // Not needed for this controller
    }
} 