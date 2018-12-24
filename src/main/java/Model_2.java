import javax.xml.stream.events.StartDocument;
import java.io.*;
import java.util.HashSet;
import java.util.Vector;

public class Model_2 {

    private Searcher searcher;
    private double avgldl;
    private int numOfIndexedDocs;
    /**
     * This function is the main function of the program.
     */
    public void Start(String path, Vector<String> cities,boolean toStem){
        HashSet<String> citieshash = new HashSet<>(cities);
        readIndexerInfo(path,toStem);
        searcher=new Searcher(avgldl,numOfIndexedDocs,path,citieshash);
    }

    private void readIndexerInfo(String path,boolean toStem) {
        try {
            BufferedReader bufferedReader=new BufferedReader(new FileReader(new File(path+"Details"+toStem+".txt")));
            String avg=bufferedReader.readLine();
            String numOfFiles=bufferedReader.readLine();
            bufferedReader.close();
            avgldl=Double.parseDouble(avg);
            numOfIndexedDocs=Integer.parseInt(numOfFiles);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}

