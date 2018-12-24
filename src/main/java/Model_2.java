import javax.xml.stream.events.StartDocument;
import java.util.HashSet;
import java.util.Vector;

public class Model_2 {

    private Searcher searcher;
    private double avgldl;
    private int numOfIndexedDocs;
    /**
     * This function is the main function of the program.
     */
    public void Start(String path, Vector<String> cities){
        HashSet<String> citieshash = new HashSet<>(cities);
        readIndexerInfo();
        searcher=new Searcher(avgldl,numOfIndexedDocs,path,citieshash);
    }

    private void readIndexerInfo() {
    }
}

