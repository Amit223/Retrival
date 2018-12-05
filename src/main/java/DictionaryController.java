import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Text;


import java.io.*;
import java.util.Iterator;
import java.util.Map;
import java.util.Scanner;

public class DictionaryController {
    @FXML
    private  Label dictionary;
    @FXML
    private  Label toShow;
    @FXML
    private AnchorPane pane;
    @FXML
    private ScrollPane scroller;

    @FXML
    public  void showDictionary(String path,Map dictionary) {
        String toShow="";
        if (dictionary == null) {
            try {

                BufferedReader reader = new BufferedReader(new FileReader(path));
                String line = reader.readLine();
                toShow = line.replaceAll("=", "\n");
            } catch (FileNotFoundException e) {

                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setContentText("Dictionary.txt to show not found in the given path!\nPlease make sure it is there!");
                alert.show();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        else{//exist
            StringBuilder stringBuilder=new StringBuilder();
            Iterator<String> iterator=dictionary.keySet().iterator();
            while (iterator.hasNext()){
                String key=iterator.next();
                stringBuilder.append(key+"--->"+dictionary.get(key)+"\n");
            }
            toShow=stringBuilder.toString();
        }

        Text text = new Text(100, 100, "Term=Doc Frequency\n" + toShow);
        text.setStyle("-fx-font: 16 arial;");
        scroller.setFitToWidth(true);
        scroller.setContent(text);
    }

}
