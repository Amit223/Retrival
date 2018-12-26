import javafx.util.Pair;
import sun.awt.Mutex;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class Ranker {

    private ExecutorService _pool = Executors.newFixedThreadPool(8);
    private static double _avgldl;
    private static int _numOfIndexedDocs;
    private static double _k = 1.25;
    private static double _b = 0.75;

    private PriorityQueue<Pair<Integer, Double>> _RankedDocs = new PriorityQueue(new Comparator<Pair<Integer, Double>>() {
        @Override
        public int compare(Pair<Integer, Double> o1, Pair<Integer, Double> o2) {
            if (o1.getValue() <= o2.getValue()) {
                return -1;
            } else //if(o1.getValue()<o2.getValue())
                return 1;
            //else return 0;

        }
    }); // Rank-Doc
    private Mutex _RankDocsMutex = new Mutex();

    public void addItem(double rank, Integer doc) {
        if (rank == 0) return;
        _RankDocsMutex.lock();
        if (_RankedDocs.size() > 49) {
            Double lowest = _RankedDocs.peek().getValue();
            if (rank > (lowest)) {
                _RankedDocs.poll();
                _RankedDocs.add(new Pair<Integer, Double>(doc, rank));
            }
        } else _RankedDocs.add(new Pair<Integer, Double>(doc, rank));

        _RankDocsMutex.unlock();
    }

    /**
     *
     * @param priortyQ by rank
     * @return collection of the docs
     */
    private Collection<Integer> PQToCollection(PriorityQueue<Pair<Integer, Double>> priortyQ) {
        Collection<Integer> collection =  new Vector<>();
        while (!priortyQ.isEmpty()){
            collection.add(priortyQ.poll().getKey());
        }
        return collection;
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
    public Collection<Integer> Rank(HashMap<Integer, Vector<Pair<String, Integer>>> docsToRank,
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
            threads[i] = new ThreadedRank(doc, docsToRank.get(doc), doc_size.get(doc), term_docsNumber);
            _pool.execute(threads[i]);
            i++;
        }
        return PQToCollection(_RankedDocs);

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
        private final Map<String, Integer> _docsNumberforTerm;
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
        public ThreadedRank(Integer doc, Vector<Pair<String, Integer>> termInDocAndTF, Integer docSize, Map<String, Integer> docsNumberforTerm) {
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
         * {@link #_docsNumberforTerm} {@link #_avgldl} {@link #_numOfIndexedDocs} {@link #_k} {@link #_b}
         * and put the document's Rank and document's number into {@link #addItem(double, Integer)} into {@link #_RankedDocs}
         */
        public void run() {
            double rank = 0;
            for (int i = 0; i < _termInDocAndTF.size(); i++) {
                String term = _termInDocAndTF.get(i).getKey();
                int numOfDocsForTerm = _docsNumberforTerm.get(term);
                double idf = Math.log(_numOfIndexedDocs / numOfDocsForTerm);
                double tf = _termInDocAndTF.get(i).getValue() / _docSize;
                double mone = idf * tf;
                double mechane = tf + _k * (1 - _b + _b * (_docSize / _avgldl));
                rank = rank + mone / mechane;
            }
            addItem(rank, _doc);
        }
    }

}
