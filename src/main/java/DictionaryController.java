import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Text;
import javafx.util.Pair;


import java.io.*;
import java.util.*;

public class DictionaryController {
    @FXML
    private  Label dictionary;
    @FXML
    private  Label toShow;
    @FXML
    private AnchorPane pane;
    @FXML
    private ScrollPane scroller;

    private TableView<Record> tab;

    Map<String,Integer> map=new HashMap<>();

    @FXML
    public  void showDictionary(String path,Map dictionary) {
        if (dictionary == null) {
            try {

                BufferedReader reader = new BufferedReader(new FileReader(path));
                String line = reader.readLine();
                String [] lines=line.split("=");
                for(int i=0;i<lines.length;i++){
                    String []strings=lines[i].split("--->");
                    if(strings.length==2){
                        String [] values=strings[1].split("&");
                        String tf_string=values[1].substring(0,values[1].length());
                        int tf=Integer.parseInt(tf_string);
                        map.put(strings[0],tf);

                    }
                }
                reader.close();

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
                Vector<Integer> details= (Vector<Integer>) dictionary.get(key);
                map.put(key, details.get(1));
            }


        }

        //write to table
        //term column
        TableColumn<Record,String> termColumn=new TableColumn<>("Term");
        termColumn.setMinWidth(200);
        termColumn.setCellValueFactory(new PropertyValueFactory("term"));
        termColumn.setSortType(TableColumn.SortType.ASCENDING);

        //tf column
        TableColumn<Record,Integer> tfColumn=new TableColumn<>("Total tf");
        tfColumn.setMinWidth(100);
        tfColumn.setCellValueFactory(new PropertyValueFactory("total_tf"));

        tab=new TableView<>();
        tab.setItems(getRecords());
        tab.getColumns().addAll(termColumn,tfColumn);
        tab.getSortOrder().add(termColumn);

        pane.getChildren().add(tab);

    }

    public ObservableList<Record> getRecords(){
        ObservableList<Record> records= FXCollections.observableArrayList();
        Iterator<String> iterator=map.keySet().iterator();
        while(iterator.hasNext()){
            String key=iterator.next();
            records.add(new Record(key,map.get(key)));
        }
        return records;
    }

}
