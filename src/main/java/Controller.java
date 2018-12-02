import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import javax.swing.*;
import javax.swing.filechooser.FileSystemView;
import java.io.File;
public class Controller {
    @FXML
    private TextField path;
    @FXML
    private CheckBox toStem;
    Model model;
    @FXML
    private TextField save;
    @FXML
    private ChoiceBox <String> language;


    @FXML
    public void initialize() {
        language.getItems().removeAll(language.getItems());
        language.getItems().addAll("English", "Hebrew ", "Arabic", "Spanish","Italian","Romanian","Russian","Other");
        language.getSelectionModel().select("English");
    }
    public void setModel(Model model) {
        this.model = model;
    }
    @FXML
    public void getFileChooser(ActionEvent actionEvent) {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Select Directory Of Stop-Words & Corpus");
        File file = chooser.showDialog(new Stage());
        if (file != null) {
            path.setText(file.getAbsolutePath());
        }
    }

    @FXML
    public void getFileChooserSave(ActionEvent actionEvent) {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Select Directory To Save Posting In");
        File file = chooser.showDialog(new Stage());
        if (file != null) {
            save.setText(file.getAbsolutePath());
        }
    }
    public void reset(ActionEvent actionEvent) {
        model.Reset();
    }
    public void startProcess(ActionEvent actionEvent) {
        model.Start(toStem.isSelected(),path.getText(),save.getText());
    }
}