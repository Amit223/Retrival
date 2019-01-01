import javafx.util.Pair;
import sun.awt.Mutex;

import java.io.*;
import java.nio.charset.Charset;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;


public class Ranker {

    private double _avgldl;
    private int _numOfIndexedDocs;
    private HashMap<Integer,Double> docsAndRanks;

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

    /**
     *
     * @return the top 50 docs
     */
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
     * @param name- in bytes
     * @return name in string
     */
    private String convertByteToString(byte[] name) {
        String s=new String(name, Charset.forName("UTF-8"));
        String out="";
        boolean flag=true;
        for (int i = 0; i < s.length()&&flag; i++) {
            if(s.charAt(i)!='#')
                out=out+s.charAt(i);
            else flag=false;
        }
        return out;
    }
    private void getAllName(){
        File f=new File("pls.txt");
        try {
            BufferedWriter writer=new BufferedWriter(new FileWriter(f,true));
            RandomAccessFile reader=new RandomAccessFile("D:\\documents\\users\\ammo\\posting true\\Documents.txt","r");
            Iterator<Integer> iterator=docsAndRanks.keySet().iterator();
            while (iterator.hasNext()){
                int doc=iterator.next();
                byte[] name=new byte[16];
                reader.seek(54*doc);
                reader.read(name);
                String nameString=convertByteToString(name);
                writer.write(nameString);
                writer.newLine();
            }
            writer.close();
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
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
     *
     * @return table of docs and their rank
     */
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
    private Collection<Integer> RankAllDocuments(HashMap<Integer, Vector<Pair<String, Integer>>> docsToRank, HashMap<Integer, Integer> doc_size,
                                                HashMap<String, Integer> term_docsNumber){
        Set<Integer> docs = docsToRank.keySet();
        Iterator<Integer> docsIt = docs.iterator();
        int i = 0;
        int doc;
        while (docsIt.hasNext()) {
            doc = docsIt.next();
            rankDoc(doc,docsToRank.get(doc),doc_size.get(doc),term_docsNumber);
        }
        getAllName();
        return get50BestDocs();

    }

    /**
     *
     * @param doc-the line of doc
     * @param docsToRank-the list of docs and thier info
     * @param doc_size - the size of doc
     * @param term_docsNumber-the info of term- how many docs
     * ranks the doc and puts in the docs and ranks table
     */
    private void rankDoc(int doc,Vector<Pair<String, Integer>> _termInDocAndTF, //docsToRank.get(doc)
                         Integer _docSize,
                           HashMap<String, Integer> term_docsNumber){
        double _k = 1.25;
        double _b = 0.75;
        double rank = 0;

        for (int i = 0; i < _termInDocAndTF.size(); i++) {
            String term = _termInDocAndTF.get(i).getKey();
            int numOfDocsForTerm = term_docsNumber.get(term);
            double idf = Math.log(_numOfIndexedDocs / numOfDocsForTerm);
            double moneOfTf=_termInDocAndTF.get(i).getValue();
            double tf = moneOfTf / _docSize;
            double mone = idf * tf;
            double mechane = tf + _k * (1 - _b + _b * (_docSize / _avgldl));
            rank = rank +( mone / mechane);
        }
      //  if(doc==93570)
    //    System.out.println(doc);
    //    System.out.println(doc);
        docsAndRanks.put(doc,rank);
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
                                                    HashMap<Integer, Integer> doc_size,
                                                     int numOfIndexedDocs,
                                                     HashMap<String, Integer> term_docsNumber,
                                                     double avgldl,String path) {
        docsAndRanks=new HashMap<>();
        _numOfIndexedDocs = numOfIndexedDocs;
        _avgldl = avgldl;
        return RankAllDocuments(docsToRank,doc_size,term_docsNumber);
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