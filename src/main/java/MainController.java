import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;

public class MainController {

    @FXML
    private AnchorPane ap;


    /**
     *
     * @param actionEvent
     * open part a of project
     */
    public void partAactivate(ActionEvent actionEvent) {
        FXMLLoader fxmlLoader=new FXMLLoader();
        Parent root = null;
        try {
            root = fxmlLoader.load(getClass().getResource("/View.fxml").openStream());
            Controller controller =fxmlLoader.getController();
            controller.setModel();
            Scene scene = new Scene(root, 500, 350);
            Stage primaryStage = new Stage();
            primaryStage.initModality(Modality.APPLICATION_MODAL);
            primaryStage.setTitle("Retrieval Project - Part A");
            primaryStage.setScene(scene);
            primaryStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     *
     * @param actionEvent
     * open part B of project
     */
    public void partBactivate(ActionEvent actionEvent) {
        FXMLLoader fxmlLoader=new FXMLLoader();
        Parent root = null;
        try {
            root = fxmlLoader.load(getClass().getResource("/View_2.fxml").openStream());
            Controller_2 controller =fxmlLoader.getController();
            controller.setModel();
            Scene scene = new Scene(root);
            Stage primaryStage = new Stage();
            primaryStage.initModality(Modality.APPLICATION_MODAL);
            primaryStage.setTitle("Retrieval Project - Part B");
            primaryStage.setScene(scene);
            primaryStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
