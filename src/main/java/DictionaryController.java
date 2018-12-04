import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Scanner;

public class DictionaryController {
    @FXML
    private  Label dictionary;
    @FXML
    private AnchorPane pane;

    @FXML
    public  void showDictionary(String path){
        try {
            String dicString="";
            FileReader f= new FileReader( path+"/Dictionary.txt");
            Scanner sc = new Scanner(f);
            while (sc.hasNextLine())
                dicString=dicString+sc.nextLine()+"\n";
            dictionary.setText(dicString);
            double []arr={dictionary.getPrefWidth(),dictionary.getPrefHeight()};
            pane.setPrefWidth(dictionary.getPrefWidth());
            pane.setPrefHeight(dictionary.getPrefHeight());

        } catch (FileNotFoundException e) {

            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText("Dictionary.txt to show not found in the given path!\nPlease make sure it is there!");
            alert.show();
        }

    }
}
