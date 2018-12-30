import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainGUI extends Application {
    public static void main(String[] args) {
        launch(args);
    }
    @Override
    /**
     * start function of gui
     */
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader fxmlLoader=new FXMLLoader();
        Parent root = fxmlLoader.load(getClass().getResource("/MainView.fxml").openStream());
        Scene scene = new Scene(root);

        primaryStage.setTitle("Retrieval Project");
        primaryStage.setScene(scene);
        primaryStage.show();
    }}