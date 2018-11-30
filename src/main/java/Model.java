import javafx.stage.Stage;
import sun.awt.Mutex;

public class Model {

    private Controller controller;
    private Stage mainStage;
    private Indexer indexer;
    private Mutex mutex;

    public Model(Controller controller, Stage stage) {
        this.controller = controller;
        this.mainStage = stage;
        mutex=new Mutex();
    }

    public Stage getMainStage() {
        return mainStage;
    }

    public void Reset(){
        indexer.delete();
    }

    // todo
    public void Start(boolean toStem,String path){
        indexer=new Indexer(toStem);

    }
}
