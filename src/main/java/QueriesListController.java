import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.util.Pair;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Vector;

public class QueriesListController {
    @FXML
    private TableView<Query> resultsTable;
    @FXML
    TableColumn<Document, String> queryId;

    @FXML
    public void initialize() {
        queryId.setCellValueFactory(new PropertyValueFactory<Document, String>("_queryId"));
        //.setCellValueFactory(new PropertyValueFactory<Document,String>("__docNum"));

        TableColumn actionCol = new TableColumn("Results");
        actionCol.setCellValueFactory(new PropertyValueFactory<>("DUMMY"));
        Callback<TableColumn<Query, String>, TableCell<Query, String>> cellFactory
                = //
                new Callback<TableColumn<Query, String>, TableCell<Query, String>>() {
                    @Override
                    public TableCell call(final TableColumn<Query, String> param) {
                        final TableCell<Query, String> cell = new TableCell<Query, String>() {

                            final Button btn = new Button("Show \nquery's \nresults");

                            @Override
                            public void updateItem(String item, boolean empty) {

                                super.updateItem(item, empty);
                                if (empty) {
                                    setGraphic(null);
                                    setText(null);
                                } else {
                                    btn.setOnAction(event -> {
                                                Query q = getTableView().getItems().get(getIndex());
                                        ResultsController controller;
                                        FXMLLoader fxmlLoader=new FXMLLoader();


                                        Parent root = null;
                                        try {
                                            root = fxmlLoader.load(getClass().getResource("/ResultsView.fxml").openStream());
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                        controller = fxmlLoader.getController();
                                        controller.setModel(q.get_documents());
                                                Scene scene = new Scene(root);
                                                Stage stage = new Stage();
                                                stage.initModality(Modality.APPLICATION_MODAL);
                                                stage.setTitle("Search - Documents list for - " + q.get_queryId());
                                                stage.setScene(scene);
                                                stage.show();
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
        resultsTable.setPlaceholder(new Label("no queries in the file queries."));

        ObservableList<Query> queriesList = FXCollections.observableArrayList();
        resultsTable.setItems(queriesList);
    }

    public void setModel(Vector<Pair<String, Collection<Document>>> pairs_QueryID_Documents) {
        ObservableList<Query> queriesList = FXCollections.observableArrayList();
        for (int i = 0; i < pairs_QueryID_Documents.size(); i++) {
            Pair<String, Collection<Document>> query_Docs = pairs_QueryID_Documents.get(i);
            queriesList.add(new Query(query_Docs.getKey(), query_Docs.getValue()));
        }
        resultsTable.setItems(queriesList);
    }
}
