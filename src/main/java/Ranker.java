import javafx.util.Pair;
import sun.awt.Mutex;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;


public class Ranker {

    private double _avgldl;
    private int _numOfIndexedDocs;
    private String _path;
    private Mutex mutex=new Mutex();
    private AtomicReference<Double> minQueue=new AtomicReference<Double>(0.0);
    private AtomicInteger queueSize=new AtomicInteger(0);
    private HashMap<Integer,Double> docsAndRanks=new HashMap<>();

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
    public Collection<Integer> get50BestDocs() {
        Iterator<Integer> docs = docsAndRanks.keySet().iterator();
        int count = 0;
        while (docs.hasNext() && count < 50) {
            Integer doc = docs.next();
            Pair<Integer, Double> doc_rank = new Pair(doc, docsAndRanks.get(doc));
            _RankedDocs.add(doc_rank);
            count += 1;
        }
        while (docs.hasNext()) {//bigger than 50
            int doc = docs.next();
            double rank = docsAndRanks.get(doc);
            Double lowest = _RankedDocs.peek().getValue();
            if (rank > (lowest)) {
                _RankedDocs.poll();
                _RankedDocs.add(new Pair<Integer, Double>(doc, rank));
            }

        }

        return PQToCollection(_RankedDocs);
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
    public Map<Integer,Double> getDocsRanking(){
        return docsAndRanks;
    }


    /**
     *
     * @param docsToRank
     * @param doc_size
     * @param term_docsNumber
     * @return
     */
    private Collection<Integer> test(HashMap<Integer, Vector<Pair<String, Integer>>> docsToRank, ConcurrentHashMap<Integer, Integer> doc_size,
                                     ConcurrentHashMap<String, Integer> term_docsNumber){
        Set<Integer> docs = docsToRank.keySet();
        Iterator<Integer> docsIt = docs.iterator();
        int i = 0;
        int doc;
        while (docsIt.hasNext()) {
            doc = docsIt.next();
            rankDoc(doc,docsToRank,doc_size,term_docsNumber);
        }
        return get50BestDocs();

    }
    private void rankDoc(int doc,HashMap<Integer, Vector<Pair<String, Integer>>> docsToRank,
                           ConcurrentHashMap<Integer, Integer> doc_size,
                           ConcurrentHashMap<String, Integer> term_docsNumber){
        double _k = 1.25;
        double _b = 0.75;
        double rank = 0;
        Vector<Pair<String, Integer>> _termInDocAndTF=docsToRank.get(doc);
        Integer _docSize=doc_size.get(doc);
        Map<String, Integer> _docsNumberforTerm=term_docsNumber;
        for (int i = 0; i < _termInDocAndTF.size(); i++) {
            String term = _termInDocAndTF.get(i).getKey();
            int numOfDocsForTerm = _docsNumberforTerm.get(term);
            double idf = Math.log(_numOfIndexedDocs / numOfDocsForTerm);
            double moneOfTf=_termInDocAndTF.get(i).getValue();
            double tf = moneOfTf / _docSize;
            double mone = idf * tf;
            double mechane = tf + _k * (1 - _b + _b * (_docSize / _avgldl));
            rank = rank +( mone / mechane);
        }
        docsAndRanks.put(doc,rank);
    }
    private void addItem(double rank, Integer doc) {
        if (rank > 0) {
            if (queueSize.get() > 49) {
                _RankDocsMutex.lock();
                Double lowest = _RankedDocs.peek().getValue();
                if (rank > (lowest)) {
                    _RankedDocs.poll();
                    _RankedDocs.add(new Pair<Integer, Double>(doc, rank));
                    minQueue.set(_RankedDocs.peek().getValue());

                    //}
                    _RankDocsMutex.unlock();
                } else {
                    _RankDocsMutex.lock();
                    _RankedDocs.add(new Pair<Integer, Double>(doc, rank));
                    _RankDocsMutex.unlock();
                    queueSize.getAndAdd(1);
                    if (rank < minQueue.get() || queueSize.get() == 0) {
                        minQueue.set(rank);
                    }

                }

            }
        }
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
                                                     double avgldl,String path) {
        _numOfIndexedDocs = numOfIndexedDocs;
        _avgldl = avgldl;
        _path=path;
        return test(docsToRank,doc_size,term_docsNumber);
        /**
        Thread[] threads = new ThreadedRank[docsToRank.size()];
        Set<Integer> docs = docsToRank.keySet();
        Iterator<Integer> docsIt = docs.iterator();
        Integer doc = 0;
        int i = 0;
        ExecutorService _pool = Executors.newFixedThreadPool(8);
        while (docsIt.hasNext()) {
            doc = docsIt.next();
            threads[i] = new ThreadedRank(doc, docsToRank.get(doc), doc_size.get(doc), term_docsNumber,docsAndRanks,numOfIndexedDocs,avgldl,mutex);
            i++;
        }
        for(int j=0;j<threads.length;j++){
            _pool.execute(threads[j]);
        }
        try {
            boolean flag = false;
            while (!flag)
                flag = _pool.awaitTermination(500, TimeUnit.MILLISECONDS);

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return get50BestDocs();
        //return PQToCollection(_RankedDocs);
         **/
    }



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

    private Integer _doc;
    private Vector<Pair<String, Integer>> _termInDocAndTF;
    private Integer _docSize;
    private Map<String, Integer> _docsNumberforTerm;
    private HashMap<Integer,Double> _docsAndRanks;
    private double _avgldl;
    private int _numOfIndexedDocs;
    private double _k = 1.25;
    private double _b = 0.75;
    private Mutex _mutex;

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
    public ThreadedRank(Integer doc, Vector<Pair<String, Integer>> termInDocAndTF, Integer docSize, Map<String, Integer> docsNumberforTerm,
                        HashMap<Integer,Double> docsAndRanks,int numOfIndexedDocs, double avgldl,Mutex mutex) {
        _doc = doc;
        _termInDocAndTF = termInDocAndTF;
        _docSize = docSize;
        _docsNumberforTerm = docsNumberforTerm;
        _docsAndRanks=docsAndRanks;
        _numOfIndexedDocs=numOfIndexedDocs;
        _avgldl=avgldl;
        _mutex=mutex;
    }

    /**
     * this function:
     * in BM25
     * using this variables:
     * {@link #_doc} {@link #_termInDocAndTF} {@link #_docSize}
     * {@link #_docsNumberforTerm} {@link #_avgldl} {@link #_numOfIndexedDocs} {@link #_k} {@link #_b}
     * and put the document's Rank and document's number into {@link #_docsAndRanks}
     */
    public void run() {
        double rank = 0;
        for (int i = 0; i < _termInDocAndTF.size(); i++) {
            String term = _termInDocAndTF.get(i).getKey();
            int numOfDocsForTerm = _docsNumberforTerm.get(term);
            double idf = Math.log(_numOfIndexedDocs / numOfDocsForTerm);
            double moneOfTf=_termInDocAndTF.get(i).getValue();
            double tf = moneOfTf / _docSize;
            double mone = idf * tf;
            double mechane = tf + _k * (1 - _b + _b * (_docSize / _avgldl));
            rank = rank +( mone / mechane);
        }
        _mutex.lock();
        _docsAndRanks.put(_doc,rank);
        _mutex.unlock();
        // if(!(minQueue.get()>rank&&queueSize.get()==50))
        //   addItem(rank, _doc);
    }
}

