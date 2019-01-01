import ParseObjects.Between;
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
import javafx.stage.Modality;
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
    @FXML
    private TextField pathSave;
    @FXML
    private ChoiceBox<String> language;


    /**
     * sets model
     */
    public void setModel() {
        model = new Model_2();
        listView.setOnMouseClicked(new EventHandler<Event>() {

            @Override
            public void handle(Event event) {
                if (listView.getSelectionModel().getSelectedItem() != null) {
                    selectedItem = listView.getSelectionModel().getSelectedItem().toString();
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
                if (listView2.getSelectionModel().getSelectedItem() != null) {
                    unselectedItem = listView2.getSelectionModel().getSelectedItem().toString();
                    listView.getItems().add(unselectedItem);
                    listView2.getItems().remove(unselectedItem);
                    listView.getItems().sort(Comparator.naturalOrder());
                    listView2.getItems().sort(Comparator.naturalOrder());

                }
            }
        });
        listView2.setPlaceholder(new Label("If empty, not filter."));
        query.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(final ObservableValue<? extends String> observable, final String oldValue, final String newValue) {
                selectWrite(new ActionEvent());
            }
        });
    }


    public String removeFromTheTermUndefindSigns(String termS) { //like: "dfsdfdsf
        try {
            int startIndex = -1;// the index that the _token begin.
            int endIndex = termS.length(); //the index that the _token ends
            if (termS != null && !termS.equals("")) {
                for (int i = 0; i < termS.length() && startIndex == -1; i++) {
                    if ( Character.isLetter(termS.charAt(i))) {
                        startIndex = i;
                        break;
                    }
                }
                for (int i = termS.length() - 1; 0 <= i && endIndex == termS.length(); i--) {
                    if ( Character.isLetter(termS.charAt(i))) {
                        endIndex = i;
                        break;
                    }
                }
                if (termS.length() > 1 && startIndex >= endIndex) return "";
                if ((startIndex != 0) || (endIndex != termS.length())) {
                    termS = termS.substring(startIndex, endIndex + 1);
                }
                return termS;
            }
            return "";
        } catch (Exception e) {
            return "";
        }
    }

    private Vector<String> getLanguages(String path) throws Exception {
        Vector<String> langs = new Vector<>();

            BufferedReader reader = new BufferedReader(new FileReader(new File(path + "/Languages.txt")));
            String lang = reader.readLine();
            while (lang != null && !lang.equals("")) {
                lang = removeFromTheTermUndefindSigns(lang);
                if (!lang.equals("")) {
                    if (!langs.contains(lang)) {
                        langs.add(lang);
                    }
                }
                lang=reader.readLine();
            }
        return langs;
    }

    /**
     * @param path_from_user
     * @return the languages from corpus
     */
    private Vector<String> getCitys(String path_from_user) {
        loadCityDictionaryToMemory(path_from_user);
        Iterator<String> iterator = cityDictionary.keySet().iterator();
        Vector<String> citys = new Vector<>();
        while (iterator.hasNext()) {
            citys.add(iterator.next());
        }
        cityDictionary.clear();//to not have memory issues
        return citys;
    }

    /**
     * @param path load the dictionary from disk to memory
     */
    private void loadCityDictionaryToMemory(String path) {
        try {
            cityDictionary = new HashMap<>();
            BufferedReader bufferedReader;
            try {
                bufferedReader = new BufferedReader(new FileReader(path + "/" + "CityDictionary.txt"));
            } catch (Exception e) {
                return;
            }
            String line = bufferedReader.readLine();
            String[] lines = line.split("=");
            for (int i = 0; i < lines.length; i++) {
                String[] pair = lines[i].split("--->");
                if (pair.length == 2) {
                    String[] values = pair[1].split(",");
                    Vector<String> vector = new Vector();
                    vector.add(values[0].substring(1, values[0].length()));//the country
                    vector.add(values[1]);//the coin
                    vector.add(values[2].substring(0, values[2].length() - 1));//population
                    cityDictionary.put(pair[0], vector);
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
    public void includeAllCities(ActionEvent ae) {
        listView2.getItems().addAll(listView.getItems());
        listView.getItems().removeAll(listView.getItems());
        listView.getItems().sort(Comparator.naturalOrder());
        listView2.getItems().sort(Comparator.naturalOrder());
    }

    @FXML
    public void ExcludeAllCities(ActionEvent ae) {
        listView.getItems().addAll(listView2.getItems());
        listView2.getItems().removeAll(listView2.getItems());
        listView.getItems().sort(Comparator.naturalOrder());
        listView2.getItems().sort(Comparator.naturalOrder());
    }

    @FXML
    /**
     * do a directory chooser of path of corpus and stop words
     */
    public void getDirChooser(ActionEvent actionEvent) {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Select directory of posting files");
        File file = chooser.showDialog(new Stage());
        if (file != null) {
            path.setText(file.getAbsolutePath());
            ObservableList<String> ObserCityList = FXCollections.observableArrayList();
            ObservableList<String> ObserLangList = FXCollections.observableArrayList();
            Vector<String> cities = getCitys(path.getText());
            File cityDic = new File(path.getText() + "/" + "CityDictionary.txt");
            boolean thereIsLangFile = true;
            Vector<String> languages = new Vector<>();
            try{
                languages=getLanguages(file.getAbsolutePath());
            }
            catch (Exception e){
                thereIsLangFile=false;
            }
            if (thereIsLangFile && cityDic.exists()) {
                filterbycity.setDisable(false);
                language.setDisable(false);
                for (String city : cities) {
                    ObserCityList.add(city);
                }
                for (String lang: languages ){
                    ObserLangList.add(lang);
                }
                listView.setItems(ObserCityList);
                listView.getItems().sort(Comparator.naturalOrder());
                language.setItems(ObserLangList);
                language.getItems().sort(String::compareTo);
            } else {
                if(!thereIsLangFile && !cityDic.exists()) {
                    path.setText("");
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setContentText("There are no \"CityDictionary.txt\" and \"Languages.txt\".");
                    alert.show();
                }
               else if(!cityDic.exists()) {
                    path.setText("");
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setContentText("There is no \"CityDictionary.txt\".");
                    alert.show();
                }
                else{// if(!thereIsLangFile){
                    path.setText("");
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setContentText("There is no \"Languages.txt\".");
                    alert.show();
                }
            }
        } else {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setContentText("You will not be able to Run before choosing a posting files' folder.");
            alert.show();
        }

    }



    @FXML
    /**
     * do a directory chooser of path of corpus and stop words
     */
    public void getDirSaveChooser(ActionEvent actionEvent) {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Select directory to save search results");
        File file = chooser.showDialog(new Stage());
        if (file != null) {
            pathSave.setText(file.getAbsolutePath());
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
    public void Run(ActionEvent actionEvent) {
        try {
            //check input:
            if (path.getText() == null || path.getText().length() == 0) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setContentText("Please select a path of posting files."); //todo
                alert.show();
                return;
            }
            if (writeQuery.isSelected() && (query.getText() == null || query.getText().length() == 0)) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setContentText("You select to write query so please write one in the query area.");
                alert.show();
                return;
            }
            if (browseButton.isSelected() && (queriesFilepath.getText() == null || queriesFilepath.getText().length() == 0)) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setContentText("You select to browse file of queries, so please browse a path of queries' file.");
                alert.show();
                return;
            }

            Vector<String> cities = new Vector<>(listView2.getItems());
            Parent root = null;
            FXMLLoader fxmlLoader = new FXMLLoader();
            if (writeQuery.isSelected()) {

                try {
                    root = fxmlLoader.load(getClass().getResource("/ResultsView.fxml").openStream());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                ResultsController controller = fxmlLoader.getController();
                controller.setModel((model.Start(path.getText(), cities, query.getText(),
                        toStem.isSelected(), toTreatSemantic.isSelected(), (pathSave.getText() != null ? pathSave.getText() : ""))));
                Scene scene = new Scene(root);
                Stage stage = new Stage();
                String queryName = "";
                {
                    String[] queryTemp = query.getText().trim().split("\\s+");
                    queryName = "";
                    for (int i = 0; i < queryTemp.length && i < 10; i++) {
                        if (i == 0) {
                            queryName += "\"" + queryTemp[i];
                        } else queryName += " " + queryTemp[i];
                        if (i == 9 && i < queryTemp.length) queryName += "...";
                        if (i == 9 || i == queryTemp.length - 1) queryName += "\"";
                    }
                }
                stage.setTitle("Search - " + queryName);
                stage.initModality(Modality.APPLICATION_MODAL);
                stage.setScene(scene);
                stage.show();
            } else { //if browseButton.isSelected()
                try {
                    try {
                        root = fxmlLoader.load(getClass().getResource("/queriesList.fxml").openStream());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    QueriesListController controller = fxmlLoader.getController();
                    controller.setModel((model.Start(path.getText(), cities, Paths.get(queriesFilepath.getText()),
                            toStem.isSelected(), toTreatSemantic.isSelected(), (pathSave.getText() != null ? pathSave.getText() : ""))));
                } catch (IOException e) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setContentText("You select to browse file of queries, and there is a problem with the file or with the file path.");
                    alert.show();
                }
                Scene scene = new Scene(root);
                Stage stage = new Stage();
                stage.initModality(Modality.APPLICATION_MODAL);
                stage.setTitle("Search - Queries list for - \"" + queriesFilepath.getText() + "\"");
                stage.setScene(scene);
                stage.show();
            }
        }
        catch(Exception e){
            
        }
    }


}
