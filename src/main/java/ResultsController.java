import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.Callback;
import javafx.util.Pair;

import java.util.Collection;
import java.util.Iterator;
import java.util.Vector;

public class ResultsController {
    @FXML
    private  TableView<Integer> tableView;
    @FXML
    private TableColumn NameCol;
    @FXML
    private TableColumn rankedCol;

    @FXML
    public void initialize() {

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

        ObservableList<Integer> docsList = FXCollections.observableArrayList();

        tableView.setItems(docsList);
        tableView.getColumns().addAll(actionCol);


    }
    public void setModel(Collection<Integer> data) {
        ObservableList<Integer> DocList = FXCollections.observableArrayList();
        ObservableList<Integer> ranked =  FXCollections.observableArrayList();
        for (int i = 0; i < data.size() ; i++) {
            ranked.add(i);
        }
        DocList.addAll(data);
        {

            NameCol.setCellValueFactory(new PropertyValueFactory<>("Document name"));
            tableView.setItems(DocList);

            rankedCol.setCellValueFactory(new PropertyValueFactory<>("Ranked"));
            tableView.setItems(ranked);

        }
    }
}



