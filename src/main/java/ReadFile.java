import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import sun.awt.Mutex;

import java.io.File;
import java.nio.file.Files;

public class ReadFile {

    private static Elements docs;

    /**
     * public void read(String folderPath){
     try {
     docs = new Elements();
     File mainfolder = new File(folderPath);
     File[] listOfFolders = mainfolder.listFiles();
     for (File folder : listOfFolders) {
     File[] files = folder.listFiles();
     int c=0;
     Thread [] threads= new Thread[files.length];
     for (int i=0;i<files.length;i++) {
     File file=files[i];
     threads[i]=new Thread(new RunnableRead(file.getPath()));
     c++;
     while(c>100);
     threads[i].start();

     }
     for (int i = 0; i <threads.length ; i++) {
     threads[i].join();
     c--;
     }

     }
     }
     catch (Exception e){
     e.printStackTrace();
     }
     }
     *
     */
    public void read(String folderPath){
        try {
            docs = new Elements();
            File folder = new File(folderPath);
            File[] listOfFolders = folder.listFiles();
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


