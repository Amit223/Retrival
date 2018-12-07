import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Set;

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
    private Button start;
    @FXML
    private Button showDictionary;
    @FXML
    private Button loadDictionaryToMemory;




    private String pathS;
    Model model;


    @FXML
    public void initialize() {
        language.setDisable(true);
        reset.setDisable(true);
        showDictionary.setDisable(true);
        loadDictionaryToMemory.setDisable(true);
    }

    /**
     * set languages on the list in gui
     */
    private void setLanguages(){
        Set<String>langs= model.getLanguages();
        language.getItems().removeAll(language.getItems());
        Iterator<String> iterator=langs.iterator();
        if(iterator.hasNext()) {
            String languageString = iterator.next();
            while (iterator.hasNext()) {
                if (!Character.isDigit(languageString.charAt(0)) || languageString.charAt(0) != ' ')
                    language.getItems().add(languageString);
                languageString = iterator.next();

            }
            language.getSelectionModel().selectFirst();
        }
        else{
            language.getItems().add("No languges :(");
            language.getSelectionModel().selectFirst();

        }
    }

    /**
     *sets model
     */
    public void setModel() {
        model = new Model();
    }

    @FXML
    /**
     * do a directory chooser of path of corpus and stop words
     */
    public void getDirChooser(ActionEvent actionEvent) {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Select Directory Of Stop-Words & Corpus");
        File file = chooser.showDialog(new Stage());
        if (file != null) {
            path.setText(file.getAbsolutePath());
        }
    }

    @FXML
    /**
     * do a directory chooser of path of corpus and stop words
     */
    public void getDirChooserSave(ActionEvent actionEvent) {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Select Directory To Save Posting In");
        File file = chooser.showDialog(new Stage());
        if (file != null) {
            save.setText(file.getAbsolutePath());
        }
    }

    /**
     *
     * @param actionEvent
     *
     * resets all posting and dictionary files
     */
    public void reset(ActionEvent actionEvent) {
        boolean flag=model.Reset();
        if(!flag){//didnt succeed
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText("Deletion failed. Please try again later :( ");
            alert.show();
        }
        else{//succeeded
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setContentText("Deletion of all the posting files, dictionary and memory succeeded!");
            alert.show();
            showDictionary.setDisable(true);
            loadDictionaryToMemory.setDisable(true);
            language.setDisable(true);
            start.setDisable(false);
            reset.setDisable(true);
        }
    }

    /**
     *
     * @param actionEvent
     * start indexing process
     */
    public void startProcess(ActionEvent actionEvent) {
        if (path.getText().equals("") && save.getText().equals("")) {
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
            String timeInString=String.valueOf(seconds);
            if(timeInString.contains(".")){
                timeInString=timeInString.substring(0,timeInString.indexOf(".")+4);
            }
            String numOfDocs=String.valueOf(model.getNumberOfDocs());
            alert.setContentText("Number of files that were indexed: " + numOfDocs+ "\n" +
                    "Number of files that were unique terms: "+ model.getNumberOfTerms()+ "\n"+
                    "RunTime: "+ timeInString + " seconds!");
            alert.show();
            reset.setDisable(false);
            loadDictionaryToMemory.setDisable(false);
            showDictionary.setDisable(false);
            language.setDisable(false);
            setLanguages();
            start.setDisable(true);

        }
    }

    @FXML
    /**
     * shows dictionary to user
     */
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
        controller.showDictionary(save.getText()+"/"+toStem.isSelected()+"Dictionary.txt",model.getDictionary());
        Scene scene = new Scene(root, 500, 500);
        DicStage.setTitle("Retrival Project - Dictionary");
        DicStage.setScene(scene);
        DicStage.show();
    }

    @FXML
    /**
     * load dictionary to memory
     */
    public void loadDictionaryToMemory(ActionEvent actionEvent){
        if (path.getText().equals("") || save.getText().equals("")) {
            pathS=path.getText();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText("Can't start process without all paths filled!\nChoose the folders and try again! ");
            alert.show();
        } else {
            if(model.loadDictionaryToMemory()){
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setContentText("Load was successful ");
                alert.show();
            }
            else{
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setContentText("Dictionary.txt to load not found in the given path!\nPlease make sure it is there!");
                alert.show();
            }
        }
    }
}