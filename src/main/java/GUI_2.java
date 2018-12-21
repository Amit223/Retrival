import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
public class GUI_2 extends Application {
    public static void main(String[] args) {
        launch(args);
    }
    @Override
    /**
     * start function of gui
     */
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader fxmlLoader=new FXMLLoader();
        Parent root = fxmlLoader.load(getClass().getResource("/View_2.fxml").openStream());
        Controller_2 controller =fxmlLoader.getController();
        controller.setModel();
        Scene scene = new Scene(root, 500, 350);
        primaryStage.setTitle("Retrival Project");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}