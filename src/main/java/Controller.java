import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
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
    @FXML
    private TextField save;
    @FXML
    private ChoiceBox <String> language;
    @FXML
    private Button reset;


    Model model;



    @FXML
    public void initialize() {
        language.getItems().removeAll(language.getItems());
        language.getItems().addAll("English", "Hebrew ", "Arabic", "Spanish","Italian","Romanian","Russian","Other");
        language.getSelectionModel().select("English");
        reset.setDisable(true);
    }


    public void setModel(Stage stage) {
        model=new Model(stage);
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
        if(path.getText().equals("")||save.getText().equals("")){
            Alert alert=new Alert(Alert.AlertType.ERROR);
            alert.setContentText("Can't start process without all paths filled!\n Choose the folders and try again! ");
            alert.show();
        }
        else{
            long startTime = System.nanoTime();
            System.out.println("Started");
            model.Start(toStem.isSelected(),path.getText(),save.getText());
            reset.setDisable(false);
            long endTime = System.nanoTime();
            System.out.println("Took "+(endTime - startTime) + " ns");
        }

    }
}