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
        Controller controller=new Controller();
        Model model=new Model(controller,primaryStage);
        controller.setModel(model);

        FXMLLoader fxmlLoader=new FXMLLoader();
        //System.out.println(GUI.class.getResource("/View.fxml"));
        Parent root = fxmlLoader.load(getClass().getResource("/View.fxml").openStream());
        Scene scene = new Scene(root, 500, 350);

        primaryStage.setTitle("Retrival Project");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}
