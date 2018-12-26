import javafx.util.Pair;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;
import java.util.Vector;

/**
 * this class read file, (include the stopwords in the first time)
 * separate the file to documents using {@link #readAndSeperateFile(String)}
 * keep the documents in the file in docs
 * */
public class ReadFile {

    private static Elements docs;

     public static Vector<Pair<String,String>> readQueriesFile(Path filePath) throws IOException {
        Document document = null;
        document = Jsoup.parse(new String(Files.readAllBytes(filePath)));
        Elements queries = document.getElementsByTag("top");
         Vector<Pair<String,String>> queriesTitels = new Vector<>();
        for(Element query: queries){
            String id = query.getElementsByTag("num").get(0).childNode(0).toString().trim().split(":")[1];
            String title = query.getElementsByTag("title").get(0).text();
            String desc = query.getElementsByTag("desc").get(0).childNode(0).toString().trim().split(":")[1];
            String narr = query.getElementsByTag("narr").get(0).text();
            queriesTitels.add(new Pair<String,String>(id,title));
        }
        return queriesTitels;
    }
    /**
     * separate the file to documents using {@link #readAndSeperateFile(String)}
     * keep the documents in the file in {@link #docs}
     * @param folderPath - read the documents from the file in this folder.
     */
    public static void read(String folderPath){
        try {
            docs = new Elements();
            File folder = new File(folderPath);
            //File[] listOfFolders = folder.listFiles();
            File[] files = folder.listFiles();
            for (int i = 0; i < files.length; i++) {
                File file = files[i];
                readAndSeperateFile(file.getPath());
            }

        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * use for {@link #read(String)}
     * this function add to the docs all the documents from a single file
     * @param fullFilePath- the file path
     */
    private static void readAndSeperateFile(String fullFilePath){
        try {
            File f = new File(fullFilePath);
            Document document = Jsoup.parse(new String(Files.readAllBytes(f.toPath())));
            Elements elements = document.getElementsByTag("DOC");
            for (Element element : elements){
                docs.add(element);
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * read the stop words from the file.
     * @param path - to the stop words folder
     */
    public static String readStopWords(String path){
        String stopwords="";
        try {
            File folder = new File(path);
            File[] listOfFiles = folder.listFiles();
            for(File file:listOfFiles){//should be only 1 but just in case
                if (file.isFile()) {
                    Scanner sc = new Scanner(file);
                    while (sc.hasNextLine())
                        stopwords=stopwords+sc.nextLine()+',';
                    return stopwords.substring(0,stopwords.length()-1);//only one file
                }
            }
        }
        catch (Exception e){
            return stopwords;
        }
        return stopwords;
    }


    /**
     * use for {@link ThreadedIndex#run()}
     * @return docs - for a spesific file.
     */
    public static Elements getDocs(){
        Elements elems= docs;
        docs=new Elements();
        return elems;
    }

}
