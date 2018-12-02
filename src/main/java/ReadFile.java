import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import sun.awt.Mutex;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.nio.file.Files;
import java.util.Scanner;

public class ReadFile {

    private static Elements docs;


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
     *
     * @param path to the stop words folder!!
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
     *
     * @param fullFilePath- the file path
     * this function add to the docs all the documents from a single file
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

    public static Elements getDocs(){
        return docs;
    }
    public class RunnableRead implements Runnable{

        private String fullFilePath;
        Mutex m;
        public RunnableRead(String path) {
            this.fullFilePath=path;
            m=new Mutex();
        }
        @Override
        public void run() {
            try {
                System.out.println(fullFilePath);
                File f = new File(fullFilePath);
                Document document = Jsoup.parse(new String(Files.readAllBytes(f.toPath())));
                Elements elements = document.getElementsByTag("DOC");
                for (Element element : elements){
                    m.lock();
                    docs.add(element);
                    m.unlock();
                }
            }
            catch (Exception e){
                e.printStackTrace();
            }

        }
    }
}
