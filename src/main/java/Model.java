import javafx.stage.Stage;
import javafx.util.Pair;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import sun.awt.Mutex;

import java.util.HashMap;


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

class ThreadedIndex extends Thread{

    private String path;
    private boolean toStem;
    private Indexer indexer;

    public ThreadedIndex(String path, boolean toStem,Indexer indexer) {
        this.path = path;
        this.toStem = toStem;
        this.indexer=indexer;
    }

    public void run(){
        Parser parser=new Parser();
       ReadFile.read(path);
       Elements elements=ReadFile.getDocs();
        HashMap<String, Integer> termList;
       for(Element element:elements){
           parser.Parse(element.text());
           //termList=parser.;//return termlist
           //indexer.Index(termList,parser.getLocations,...,...,parser.getWordCount());
       }
    }
}
