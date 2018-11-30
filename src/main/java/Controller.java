import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;

import java.io.File;

public class Controller {
    @FXML
    private TextField path;
    @FXML
    private CheckBox toStem;

    Model model;

    public void setModel(Model model) {
        this.model = model;
    }


    @FXML
    public void getFileChooser(ActionEvent actionEvent) {
        final FileChooser fileChooser = new FileChooser();
        File file = fileChooser.showOpenDialog(model.getMainStage());
        path.setText(file.getAbsolutePath());

    }

    public void reset(ActionEvent actionEvent) {
        model.Reset();
    }


    public void startProcess(ActionEvent actionEvent) {
        model.Start(toStem.isSelected(),path.getText());
    }
}
