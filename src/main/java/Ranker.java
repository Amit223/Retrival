import javafx.util.Pair;

import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

public class Ranker {

    //todo priority queue documents grade of 50
    /**
     * this function give a grade to any doc and add it to priority queue of 50.
     * @param docsToRank
     * @return rankedDocs
     */
    public Vector<String> Rank(ConcurrentHashMap<Integer, Vector<Pair<String, Integer>>> docsToRank) {
        Vector<String>rankedDocs=new Vector<>();
        return rankedDocs;
    }
}
