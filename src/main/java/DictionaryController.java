import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Text;
import javafx.util.Pair;


import java.io.*;
import java.util.Iterator;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;

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
    private TableView tab;

    @FXML
    public  void showDictionary(String path,Map dictionary) {
        String toShow="";
        Map<String,String> map=new TreeMap();
        if (dictionary == null) {
            try {

                BufferedReader reader = new BufferedReader(new FileReader(path));
                String line = reader.readLine();
                //toShow = line.replaceAll("=", "\n");
                String [] lines=line.split("=");
                for(int i=0;i<lines.length;i++){
                    String []strings=lines[i].split("--->");
                    if(strings.length==2){
                        String [] values=strings[1].split("&");
                        int tf=Integer.parseInt(values[1].substring(0,values[1].length()-1));
                        map.put(strings[0],String.valueOf(tf));

                    }
                }
                String s=map.toString().replaceAll("=","--->");
                s=s.replaceAll(",","");
                toShow=s.substring(1,s.length()-1);

            } catch (Exception e) {

                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setContentText("Dictionary.txt to show not found in the given path!\nPlease make sure it is there!");
                alert.show();
            }

        }
        else{//exist
            Iterator<String> iterator=dictionary.keySet().iterator();
            while (iterator.hasNext()){
                String key=iterator.next();
                Pair<Integer, Integer> pair= (Pair<Integer, Integer>) dictionary.get(key);
                map.put(key, String.valueOf(pair.getValue()));
            }
            String s=map.toString().replaceAll("=","--->");
            s=s.replaceAll(" ","\n");
            s=s.replaceAll(",","");
            toShow=s.substring(1,s.length()-1);

        }
        tab.getItems().setAll(map);
        //Text text = new Text(100, 100, "Term=Term Frequency\n" + toShow);
        //text.setStyle("-fx-font: 16 arial;");
        //scroller.setFitToWidth(true);
        //scroller.setContent(text);
    }

}
