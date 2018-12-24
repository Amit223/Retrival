import java.util.Collection;
import java.util.HashSet;
import java.util.Vector;

public class Model_2 {

    private Searcher searcher;
    private double avgldl;
    private int numOfIndexedDocs;
    /**
     * This function is the main function of the program.
     */
    public Collection<Integer> Start(String path, Vector<String> cities, String query, boolean toStem, boolean toTreatSemantic){
        HashSet<String> citieshash = new HashSet<>(cities);
        readIndexerInfo();
        StopWords.setStopwords(ReadFile.readStopWords("d:\\documents\\users\\liadber\\Downloads\\corpus")); //todo fix
        searcher=new Searcher(avgldl,numOfIndexedDocs,path,citieshash);
        return searcher.Search(query, toStem, toTreatSemantic);
    }

    private void readIndexerInfo() {
    }
}

