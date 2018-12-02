import javafx.stage.Stage;
import javafx.util.Pair;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.testng.internal.thread.ExecutorAdapter;
import sun.awt.Mutex;
import sun.nio.ch.ThreadPool;

import java.util.HashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


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


    public void Start(boolean toStem,String path,String toSave){
        indexer=new Indexer(toStem,toSave);
        String s=ReadFile.readStopWords(path);
        StopWords.setStopwords(s);
        ExecutorService pool= Executors.newFixedThreadPool(50);
        //todo - use the pool and send to start of thread

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
           String text=element.getElementsByTag("TEXT").text();
           String name=element.getElementsByTag("TI").text();
           Elements Felements=element.getElementsByTag("F");
           String city="";
           String language="";
           for (Element element1: Felements){
               if(element1.attr("P").equals("104")){//city
                   city=element1.text();
               }
               else if(element1.attr("P").equals("105"))//language
               {
                   language=element1.text();
               }
           }
           parser.Parse(text,toStem,city);//return termlist
           termList=parser.getTerms();
           indexer.Index(termList,parser.getLocations(),name,city,parser.getWordCount());
       }
    }
}
