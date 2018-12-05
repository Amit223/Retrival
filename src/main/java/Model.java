import javafx.stage.Stage;
import javafx.util.Pair;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.testng.internal.thread.ExecutorAdapter;
import sun.awt.Mutex;
import sun.nio.ch.ThreadPool;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;


public class Model {


    private Stage mainStage;
    private Indexer indexer;
    private Mutex mutex;


    public Model(Stage stage) {
        this.mainStage = stage;
        mutex=new Mutex();

        this.indexer=new Indexer(false,"C:\\Users\\liadber\\IdeaProjects\\Retrival\\src\\main\\resources");
    }

    public boolean loadDictionaryToMemory(){
        return indexer.loadDictionaryToMemory();
    }


    public boolean Reset(){
        return indexer.delete();

    }

    public int getNumberOfDocs(){
        return indexer.getNumberOfDocs();
    }
    public int getNumberOfTerms(){
        return indexer.getNumberOfTerms();
    }
    public Set<String> getLanguages(){
        return indexer.getLanguages();
    }

    public Map<String, Integer> getDictionary() {
        return indexer.getDictionary();
    }

    public void Start(boolean toStem,String path,String toSave){
        indexer=new Indexer(toStem,toSave);
        String s=ReadFile.readStopWords(path);
        StopWords.setStopwords(s);
        ExecutorService pool= Executors.newFixedThreadPool(10);
        File folder = new File(path);
        File[] listOfFiles = folder.listFiles();

        for(int i=0; i<listOfFiles.length;i++){
            if(listOfFiles[i].isDirectory()){//the corpus!!
                File[] directories=listOfFiles[i].listFiles();//all the folders in corpus
                Thread [] threads=new ThreadedIndex[directories.length];
                for(int j=0;j<directories.length;j++){
                    threads[j]=new ThreadedIndex(directories[j].getAbsolutePath(),toStem,indexer);
                    pool.execute(threads[j]);

                }


                pool.shutdown();

                try {
                    boolean flag = false;
                    while (!flag)
                        flag = pool.awaitTermination(1009, TimeUnit.MILLISECONDS);
                    System.out.println("Finished threadpool");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
        }
        System.out.println("last push!");
        indexer.push();
        System.out.println("Start sorting");
        indexer.loadDictionaryToFile();
        indexer.loadCityDictionaryToFile();
        StopWords.reset();

        /**
        indexer = null;
        System.gc();
        indexer = new Indexer(toStem, toSave);

        indexer.sort();
**/


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
        ReadFile.read(path);
        Elements elements=ReadFile.getDocs();
        HashMap<String, Integer> termList;
        //StopWords.setStopwords(path);
        for(int i=0;i<elements.size();i++){
            if(elements.get(i)!=null&&elements.get(i).getElementsByTag("TEXT")!=null) {
                String text = elements.get(i).getElementsByTag("TEXT").text();
                String name = elements.get(i).getElementsByTag("DOCNO").text();
                Elements Felements = elements.get(i).getElementsByTag("F");
                String city = "";
                String language = "";
                for (Element element1 : Felements) {
                    if (element1.attr("P").equals("104")) {//city
                        city = element1.text();
                        if (city.contains(" "))
                            city = city.substring(0, city.indexOf(" "));
                    } else if (element1.attr("P").equals("105"))//language
                    {
                        language = element1.text();
                    }
                }
                Parser parser = new Parser();
                parser.Parse(text, toStem, city);//return termlist
                termList = parser.getTerms();
                indexer.Index(termList, parser.getLocations(), name, city, parser.getWordCount(), language);
            }
        }
        System.out.println("DONE");
    }
}