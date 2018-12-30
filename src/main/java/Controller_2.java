import com.sun.org.apache.xml.internal.utils.StringComparable;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;


import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class Controller_2 {

    Model_2 model;
    Map<String, Vector<String>> cityDictionary;
    @FXML
    private TextField queriesFilepath;
    @FXML
    TextField path;
    @FXML
    RadioButton browseButton;
    @FXML
    RadioButton writeQuery;
    @FXML
    ListView listView;
    @FXML
    ListView listView2;
    String selectedItem;
    String unselectedItem;
    @FXML
    TitledPane filterbycity;
    @FXML
    private CheckBox toTreatSemantic;
    @FXML
    private CheckBox toStem;
    @FXML
    private TextArea query;




    /**
     *sets model
     */
    public void setModel() {
        model = new Model_2();
        listView.setOnMouseClicked(new EventHandler<Event>() {

            @Override
            public void handle(Event event) {
                if(listView.getSelectionModel().getSelectedItem()!=null){
                    selectedItem =  listView.getSelectionModel().getSelectedItem().toString();
                    listView2.getItems().add(selectedItem);
                    listView.getItems().remove(selectedItem);
                    listView.getItems().sort(Comparator.naturalOrder());
                    listView2.getItems().sort(Comparator.naturalOrder());

                }

            }
        });
        listView2.setOnMouseClicked(new EventHandler<Event>() {

            @Override
            public void handle(Event event) {
               if(listView2.getSelectionModel().getSelectedItem()!=null) {
                   unselectedItem = listView2.getSelectionModel().getSelectedItem().toString();
                   listView.getItems().add(unselectedItem);
                   listView2.getItems().remove(unselectedItem);
                   listView.getItems().sort(Comparator.naturalOrder());
                   listView2.getItems().sort(Comparator.naturalOrder());

               }
            }
        });
        query.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(final ObservableValue<? extends String> observable, final String oldValue, final String newValue) {
                selectWrite(new ActionEvent());
            }
        });
    }


    /**
     *
     * @param path_from_user
     * @return the languages from corpus
     */
    private Vector<String> getCitys(String path_from_user){
       loadCityDictionaryToMemory(path_from_user);
       Iterator<String> iterator=cityDictionary.keySet().iterator();
       Vector<String> citys=new Vector<>();
       while(iterator.hasNext()){
           citys.add(iterator.next());
       }
       cityDictionary.clear();//to not have memory issues
       return citys;
    }
    /**
     *
     * @param path
     * load the dictionary from disk to memory
     */
    private void loadCityDictionaryToMemory(String path){
        try {
            cityDictionary = new HashMap<>();
            BufferedReader bufferedReader = new BufferedReader(new FileReader(path + "/" + "CityDictionary.txt"));
            String line = bufferedReader.readLine();
            String[] lines = line.split("=");
            for (int i = 0; i < lines.length; i++) {
                String[] pair = lines[i].split("--->");
                if(pair.length==2){
                    String[] values=pair[1].split(",");
                    Vector<String> vector=new Vector();
                    vector.add(values[0].substring(1,values[0].length()));//the country
                    vector.add(values[1]);//the coin
                    vector.add(values[2].substring(0,values[2].length()-1));//population
                    cityDictionary.put(pair[0],vector);
                }

            }
            bufferedReader.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    /**
     * do a directory chooser of path of corpus and stop words
     */
    public void getFileCooser(ActionEvent actionEvent) {
        selectBrowse(actionEvent);
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Select file of queries");
        File file = chooser.showOpenDialog(new Stage());
        if (file != null) {
            queriesFilepath.setText(file.getAbsolutePath());
        }
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
            filterbycity.setDisable(false);
            path.setText(file.getAbsolutePath());
            ObservableList<String> list = FXCollections.observableArrayList();
            Vector<String> cities= getCitys(path.getText());
            if(cities.size()!=0) {
                for (String city : cities) {
                    list.add(city);
                }
                listView.setItems(list);
                listView.getItems().sort(Comparator.naturalOrder());
            }
            else{
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setContentText("there is no \"CityDictionary.txt\".");
                alert.show();
            }
        }
    }

    @FXML
    /**
     * flip the radio button
     */
    public void selectBrowse(ActionEvent actionEvent) {
        browseButton.setSelected(true);
        writeQuery.setSelected(false);
    }


    @FXML
    /**
     * flip the radio button
     */
    public void selectWrite(ActionEvent actionEvent) {
        writeQuery.setSelected(true);
        browseButton.setSelected(false);
    }

    @FXML
    public void Run(ActionEvent actionEvent){
        //check input:
        if(writeQuery.isSelected() && ( query.getText()==null || query.getText().length()==0)){
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText("You select to write query so please write one in the query area.");
            alert.show();
            return;
        }
        if(browseButton.isSelected() && (queriesFilepath.getText()==null ||  query.getText().length()==0)){
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText("You select to browse file of queries, so please browse a path of queries' file.");
            alert.show();
            return;
        }

        Vector<String> cities = new Vector<>( listView2.getItems());
        Parent root=null;
        FXMLLoader fxmlLoader=new FXMLLoader();
        if(writeQuery.isSelected()) {

            try {
                root = fxmlLoader.load(getClass().getResource("/ResultsView.fxml").openStream());
            } catch (IOException e) {
                e.printStackTrace();
            }
            ResultsController controller =fxmlLoader.getController();
            controller.setModel((model.Start(path.getText(), cities, query.getText(), toStem.isSelected(), toTreatSemantic.isSelected())));

        }
        else { //if browseButton.isSelected()
            try {
                try {
                    root = fxmlLoader.load(getClass().getResource("/queriesList.fxml").openStream());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                QueriesListController controller =fxmlLoader.getController();
                controller.setModel((model.Start(path.getText(), cities, Paths.get(queriesFilepath.getText()), toStem.isSelected(), toTreatSemantic.isSelected())));
            } catch (IOException e) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setContentText("You select to browse file of queries, and there is a problem with the file or with the file path.");
                alert.show();            }
        }
        Scene scene = new Scene(root);
        Stage stage= new Stage();
        stage.setTitle("Results");
        stage.setScene(scene);
        stage.show();
    }





}
