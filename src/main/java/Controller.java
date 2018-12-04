import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

public class Controller {
    @FXML
    private TextField path;
    @FXML
    private CheckBox toStem;
    @FXML
    private TextField save;
    @FXML
    private ChoiceBox<String> language;
    @FXML
    private Button reset;
    @FXML
    private Button showDictionary;
    @FXML
    private Button loadDictionaryToMemory;
    private String pathS;
    Model model;


    @FXML
    public void initialize() {
        language.getItems().removeAll(language.getItems());
        language.getItems().addAll("English", "Hebrew ", "Arabic", "Spanish", "Italian", "Romanian", "Russian", "Other");
        language.getSelectionModel().select("English");
        reset.setDisable(true);
    }


    public void setModel(Stage stage) {
        model = new Model(stage);
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
        if (path.getText().equals("") || save.getText().equals("")) {
            pathS=path.getText();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText("Can't start process without all paths filled!\n Choose the folders and try again! ");
            alert.show();
        } else {
            System.out.println("Started");
            long startTime = System.nanoTime();
            model.Start(toStem.isSelected(), path.getText(), save.getText());
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            long endTime = System.nanoTime(); //(endTime - startTime)
            long elapsedTime = endTime - startTime;
            double seconds = (double)elapsedTime / 1_000_000_000.0;
            alert.setContentText("Number of files that were indexed: +" + model.getNumberOfDocs()+ "\n" +
                    "Number of files that were unique terms: "+ model.getNumberOfTerms()+ "\n"+
                    "RunTime: "+ elapsedTime);
            alert.show();
            reset.setDisable(false);
            System.out.println("Took "+(endTime - startTime)/1000000000 + " s");
        }
    }

    @FXML
    public void showDictionary1(ActionEvent actionEvent) {
        FXMLLoader fxmlLoader = new FXMLLoader();
        Parent root = null;
        try {
            root = fxmlLoader.load(getClass().getResource("/DictionaryView.fxml").openStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
        Stage DicStage = new Stage();
        DictionaryController controller = fxmlLoader.getController();
        controller.showDictionary(pathS);
        Scene scene = new Scene(root, 500, 500);
        DicStage.setTitle("Retrival Project - Dictionary");
        DicStage.setScene(scene);
        DicStage.show();
    }

    @FXML
    public void loadDictionaryToMemory(ActionEvent actionEvent){
        if (path.getText().equals("") || save.getText().equals("")) {
            pathS=path.getText();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText("Can't start process without all paths filled!\nChoose the folders and try again! ");
            alert.show();
        } else {
            if(model.loadDictionaryToMemory()){
                //success
            }
            else{
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setContentText("Dictionary.txt to load not found in the given path!\nPlease make sure it is there!");
                alert.show();
            }
        }
    }
}