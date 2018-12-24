import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.util.Collection;
import java.util.Iterator;

public class ResultsController {
    @FXML
    private  TableView<Integer> tableView;
    private     Collection<Integer> data;



    public void setModel(Collection<Integer> data) {
        Iterator<Integer> it = data.iterator();
        while (it.hasNext()){
            tableView.getItems().add(it.next());
        }
    }
}
