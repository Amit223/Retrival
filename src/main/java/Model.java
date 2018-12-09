import javafx.stage.Stage;
import javafx.util.Pair;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import sun.awt.Mutex;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;


public class Model {


    private Indexer indexer;

    /**
     *
     * @return true if load of dictionary to memort successful and false otherwise
     */
    public boolean loadDictionaryToMemory(){
        return indexer.loadDictionaryToMemory();
    }


    /**
     *
     * @return true if reset successful and false otherwise
     */
    public boolean Reset(){
        return indexer.delete();

    }

    /**
     *
     * @return number of documents indexed
     */
    public int getNumberOfDocs(){
        return indexer.getNumberOfDocs();
    }

    /**
     *
     * @return number of unique terms
     */
    public int getNumberOfTerms(){
        return indexer.getNumberOfTerms();
    }

    /**
     *
     * @return the languages of files found
     */
    public Set<String> getLanguages(){
        return indexer.getLanguages();
    }

    /**
     *
     * @return dictionary
     */
    public Map<String, Pair<Integer, Integer>> getDictionary() {
        return indexer.getDictionary();
    }

    /**
     *
     * @param toStem
     * @param path of corpus and stop words
     * @param toSave- path that posting files will be saved in
     *
     * this function set stopwords and do the index process
     */
    public void Start(boolean toStem,String path,String toSave){
        indexer=new Indexer(toStem,toSave);
        String s=ReadFile.readStopWords(path);
        StopWords.setStopwords(s);
        ExecutorService pool= Executors.newFixedThreadPool(8);
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
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
        }
        indexer.push();
        indexer.loadDictionaryToFile();
        indexer.loadCityDictionaryToFile();
        StopWords.reset();
        indexer.sort();

        //delets unwanted files:
        File f;
        for (char c='a'; c<='z'; c++){
            f=new File(c+c+"2");
            f.delete();
            f=new File(c+c+"4");
            f.delete();
            f=new File(c+c+"6");
            f.delete();
            f=new File(c+c+"8");
            f.delete();
        }
        f=new File("002");
        f.delete();
        f=new File("004");
        f.delete();
        f=new File("006");
        f.delete();
        f=new File("008");
        f.delete();
    }
}

/**
 * this class extend thread
 */
class ThreadedIndex extends Thread{

    private String path;
    private boolean toStem;
    private Indexer indexer;

    /**
     * constructor
     * @param path of the file
     * @param toStem
     * @param indexer
     *
     */
    public ThreadedIndex(String path, boolean toStem,Indexer indexer) {
        this.path = path;
        this.toStem = toStem;
        this.indexer=indexer;
    }

    /**
     * for each file seperato to docs and parse and index each one
     */
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
    }
}