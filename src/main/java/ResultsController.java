import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.Callback;

import java.util.Collection;
import java.util.Vector;

public class ResultsController {
    @FXML
    private  TableView<Document> resultsTable;
    @FXML
    private TableColumn<Document,String> NameCol;
    @FXML
    private TableColumn rankedCol;

    @FXML
    public void initialize() {

        NameCol.setCellValueFactory(new PropertyValueFactory<Document,String>("_name"));
        //.setCellValueFactory(new PropertyValueFactory<Document,String>("__docNum"));

        TableColumn actionCol = new TableColumn("Entities");
        actionCol.setCellValueFactory(new PropertyValueFactory<>("DUMMY"));
        Callback<TableColumn<String, String>, TableCell<String, String>> cellFactory
                = //
                new Callback<TableColumn<String, String>, TableCell<String, String>>() {
                    @Override
                    public TableCell call(final TableColumn<String, String> param) {
                        final TableCell<String, String> cell = new TableCell<String, String>() {

                            final Button btn = new Button("Show \n entities");

                            @Override
                            public void updateItem(String item, boolean empty) {
                                super.updateItem(item, empty);
                                if (empty) {
                                    setGraphic(null);
                                    setText(null);
                                } else {
                                    btn.setOnAction(event -> {
                                        String doc =getTableView().getItems().get(getIndex()); //Vacation vacation = getTableView().getItems().get(getIndex());
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
 //       ObservableList<Integer> ranked =  FXCollections.observableArrayList();
 //     for (int i = 0; i < data.size() ; i++) {
   //        ranked.add(i);
   //     }
        for (Document doc: data) {
            DocList.add(doc);
        }
        resultsTable.setItems(DocList);
        {

        //    rankedCol.setCellValueFactory(new PropertyValueFactory<>("Ranked"));
          //  resultsTable.setItems(ranked);

        }
    }
}



