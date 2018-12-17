import javafx.util.Pair;
import sun.awt.Mutex;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class Ranker {

    private ExecutorService _pool = Executors.newFixedThreadPool(8);
    ;
    private static double _avgldl;
    private static int _numOfIndexedDocs;
    private static double _k = 0;
    private static double _d = 0;
    private SortedMap<Integer,Integer> _RankedDocs = new TreeMap<>(); // Rank-Doc
    private Mutex _RankDocsMutex = new Mutex();

    public void addItem(Integer rank,Integer doc) {
        if(rank==0) return;
        _RankDocsMutex.lock();
        if (_RankedDocs.size() > 50) {
            Integer lowest = _RankedDocs.firstKey();
            if (rank>(lowest)) {
                _RankedDocs.remove(lowest);
                _RankedDocs.put(rank,doc);
            }
        }else _RankedDocs.put(rank,doc);

        _RankDocsMutex.unlock();
    }

    /**
     * this function give a grade to any doc and add it to priority queue of 50.
     *
     * @param docsToRank
     * @param doc_size
     * @param numOfIndexedDocs
     * @param term_docsNumber
     * @param avgldl
     * @return rankedDocs
     */
    public Collection<Integer> Rank(ConcurrentHashMap<Integer, Vector<Pair<String, Integer>>> docsToRank,
                                    ConcurrentHashMap<Integer, Integer> doc_size,
                                    int numOfIndexedDocs,
                                    ConcurrentHashMap<String, Integer> term_docsNumber,
                                    double avgldl) {
        _numOfIndexedDocs = numOfIndexedDocs;
        _avgldl = avgldl;
        Thread[] threads = new ThreadedRank[docsToRank.size()];
        Set<Integer> docs = docsToRank.keySet();
        Iterator<Integer> docsIt = docs.iterator();
        Integer doc = 0;
        int i = 0;
        while (docsIt.hasNext()) {
            doc = docsIt.next();
            threads[i] = new ThreadedRank(doc, docsToRank.get(doc), doc_size.get(doc), term_docsNumber.get(doc));
            _pool.execute(threads[i]);
            i++;
        }
        return _RankedDocs.values();

    }

    /**
     * used for the {@link #start()}
     * this class extend thread and implements a thread that:
     * read file using {@link ReadFile}
     * get the relevant information from the tags
     * parse the text to #termList
     * and index the files using it.
     */
    class ThreadedRank extends Thread {

        private final Integer _doc;
        private final Vector<Pair<String, Integer>> _termInDocAndTF;
        private final Integer _docSize;
        private final int _docsNumberforTerm;
        /** in the Ranker class
         * private double _avgldl;
         * private  int _numOfIndexedDocs;
         * private static double _k=0;
         * private static double _d=0; **/

        /**
         * constructor
         *
         * @param doc
         * @param termInDocAndTF
         * @param docSize
         * @param docsNumberforTerm
         */
        public ThreadedRank(Integer doc, Vector<Pair<String, Integer>> termInDocAndTF, Integer docSize, int docsNumberforTerm) {
            _doc = doc;
            _termInDocAndTF = termInDocAndTF;
            _docSize = docSize;
            _docsNumberforTerm = docsNumberforTerm;
        }

        /**
         * this function:
         * in BM25
         * using this variables:
         * {@link #_doc} {@link #_termInDocAndTF} {@link #_docSize}
         * {@link #_docsNumberforTerm} {@link #_avgldl} {@link #_numOfIndexedDocs} {@link #_k} {@link #_d}
         * and put the document's Rank and document's number into {@link #addItem(Integer, Integer)} into {@link #_RankedDocs}
         */
        public void run() {
            int rank=0;
            //todo amit
            addItem(rank,_doc);
        }
    }

}
