import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.Callback;
import javafx.util.Pair;

import javax.print.Doc;
import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;
import java.util.Vector;

public class ResultsController {
    @FXML
    private TableView<Document> resultsTable;
    @FXML
    private TableColumn<Document, String> NameCol;
    @FXML
    private TableColumn rankedCol;

    @FXML
    public void initialize() {
        NameCol.setCellValueFactory(new PropertyValueFactory<Document, String>("_name"));
        //.setCellValueFactory(new PropertyValueFactory<Document,String>("__docNum"));

        TableColumn actionCol = new TableColumn("Entities");
        actionCol.setCellValueFactory(new PropertyValueFactory<>("DUMMY"));
        Callback<TableColumn<Document, String>, TableCell<Document, String>> cellFactory
                = //
                new Callback<TableColumn<Document, String>, TableCell<Document, String>>() {
                    @Override
                    public TableCell call(final TableColumn<Document, String> param) {
                        final TableCell<Document, String> cell = new TableCell<Document, String>() {

                            final Button btn = new Button("Show \n entities");

                            @Override
                            public void updateItem(String item, boolean empty) {
                                super.updateItem(item, empty);
                                if (empty) {
                                    setGraphic(null);
                                    setText(null);
                                } else {
                                    btn.setOnAction(event -> {
                                                Document doc = getTableView().getItems().get(getIndex()); //Vacation vacation = getTableView().getItems().get(getIndex());
                                                Vector<Pair<String, Integer>> entities = doc.get_entities();
                                                Alert success = new Alert(Alert.AlertType.INFORMATION);
                                                success.setHeaderText("Entities");
                                                String entitiesString="";
                                                boolean thereIsEntity = false;
                                                for (int i = 0; i < entities.size(); i++) {
                                                    if(!entities.get(i).getKey().equals("X")) {
                                                        entitiesString += "Entity: " + entities.get(i).getKey() + ", Rank: " + entities.get(i).getValue()+".\n";
                                                        thereIsEntity = true;
                                                    }
                                                    }
                                                    if(!thereIsEntity || entities.size()==0 ||entitiesString.equals("") ){
                                                        entitiesString="Sorry, there is not entities in that document.";
                                                    }
                                                success.setContentText(entitiesString);
                                                success.show();
                                            }
                                    );
                                    setGraphic(btn);
                                    setText(null);
                                }
                            }
                        };
                        return cell;
                    }
                };
        actionCol.setCellFactory(cellFactory);
        resultsTable.getColumns().addAll(actionCol);
        ObservableList<Document> docsList = FXCollections.observableArrayList();
        resultsTable.setItems(docsList);
    }

    public void setModel(Collection<Document> data) {
        ObservableList<Document> DocList = FXCollections.observableArrayList();
        for (Document doc : data) {
            DocList.add(doc);

        }
        resultsTable.setItems(DocList);
    }
}



