import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
public class GUI extends Application {
    public static void main(String[] args) {
        launch(args);
    }
    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader fxmlLoader=new FXMLLoader();
        Parent root = fxmlLoader.load(getClass().getResource("/View.fxml").openStream());
        Controller controller =fxmlLoader.getController();
        controller.setModel(primaryStage);
        Scene scene = new Scene(root, 500, 350);
        primaryStage.setTitle("Retrival Project");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}