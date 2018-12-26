import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.util.Pair;

import java.util.Collection;
import java.util.Iterator;
import java.util.Vector;

public class ResultsController {
    @FXML
    private  TableView<Integer> tableView;

    public void setModel(Collection<Integer> data) {
        Iterator<Integer> it = data.iterator();
        while (it.hasNext()){
            tableView.getItems().add(it.next());
        }
    }


}
