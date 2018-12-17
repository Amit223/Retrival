import ADT.HashSetIgnoreCase;
import javafx.util.Pair;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * this class get query (string with spaces), parse it to final terms dictionary @link {@link Parser#_termList}.
 * for this dictionary<term,tf> this class return list of 50 most relevant documents using {@link Ranker}
 * the main function in this class is {@link #Search(String, boolean)}
 */
public class Searcher {

    private Ranker _ranker = new Ranker();
    private int _numOfIndexedDocs;
    private double _avgldl =0; //average document length
    private String _path;
    private HashSet<String> _chosenCities; //if (_chosenCities.size()==0|| _chosenCities.contains(city)) add it. else- not


    /**
     * hashmap of
     * docline in {@link Indexer#documents} to Vector of Pairs of term-tf.
     */
    private ConcurrentHashMap<Integer, Vector<Pair<String, Integer>>> _doc_termPlusTfs; //using vector to save memory. todo concurrent Vector
    private static ConcurrentHashMap<Integer, Integer> _doc_size = new ConcurrentHashMap<>(); //using vector to save memory. todo concurrent Vector
    private static ConcurrentHashMap<String, Integer> _term_docsCounter = new ConcurrentHashMap<>(); //using vector to save memory. todo concurrent Vector



    public Searcher(double avgldl,int numOfIndexedDocs, String path, HashSet<String> chosenCities) {
        _numOfIndexedDocs=numOfIndexedDocs;
        _avgldl=avgldl;//_indexer.getAvgldl();
                _doc_termPlusTfs=new ConcurrentHashMap<>();
        _doc_size=new ConcurrentHashMap<>();
        _path=path;
        _chosenCities=chosenCities;
    }

    /**
     * this class get query (string with spaces), parse it to final terms dictionary @link {@link Parser#_termList}.
     * for this dictionary<term,tf> this class return list of 50 most relevant documents using {@link Ranker}
     *
     * @param query  - to retrieve
     * @param toStem - if true stem, else not.
     * @return list of relevant docs.
     */
    public Vector<String> Search(String query, boolean toStem) {
        Vector<String> ans = new Vector<>();
        build_doc_termPlusTfs(query,toStem);
        build_doc_size();
        ans=_ranker.Rank(_doc_termPlusTfs,_doc_size,_numOfIndexedDocs, _term_docsCounter, _avgldl); // return only 50 most relvante
        return ans;
    }

    /**
     * load
     * @return
     */
    /**
     * build {@link #_doc_termPlusTfs} using {@link #addDocsTo_doc_termPlusTfs(String)}
     * @param query - query to search
     * @param toStem if stem is needed
     */
    private void build_doc_termPlusTfs(String query, boolean toStem) {
        Parser queryParser = new Parser();
        queryParser.Parse(query, toStem, "");
        HashMap<String, Integer> termList = queryParser.getTerms();
        Set<String> terms = termList.keySet();
        Iterator<String> termsIt = terms.iterator();
        String term = "";
        while (termsIt.hasNext()) {
            term = termsIt.next();
            addDocsTo_doc_termPlusTfs(term);
        }
    }

    /**
     * add docs that the term appear in them to our {@link #_doc_termPlusTfs} using {@link #readTermDocs(String)}
     * @param term - the term that adding the docs that include it.
     *             working in concurrency
     */
    private void addDocsTo_doc_termPlusTfs(String term) {
        Vector<Pair<Integer, Integer>> doc_tf = readTermDocs(term);
        _term_docsCounter.put(term,doc_tf.size());
        for (int i = 0; i < doc_tf.size(); i++) {
            Integer docNum = doc_tf.get(i).getKey();
            Integer termTfInDoc = doc_tf.get(i).getValue();
            if (_doc_termPlusTfs.containsKey(docNum)) {
                _doc_termPlusTfs.get(docNum).add(new Pair<String, Integer>(term, termTfInDoc)); //todo - future error - if not update, do put after getting copy.
            }
            else {
                _doc_termPlusTfs.put(docNum, new Vector<Pair<String, Integer>>());
                _doc_termPlusTfs.get(docNum).add(new Pair<String, Integer>(term, termTfInDoc)); //todo - future error - if not update, do put after getting copy.
            }
        }
    }

    /**
     * using {@link #_chosenCities} and not insert doc that their city is one of the cities given from the user
     * @param term - the docs that include  the term
     * @return doc_tf - list of "doc-tf"s for the above term
     */
    private Vector<Pair<Integer,Integer>> readTermDocs(String term) {
        Vector<Pair<Integer,Integer>> doc_tf= new Vector<>();
        //todo amit - use choseCities:
        //while (there is more docs for this term)
        // if (_chosenCities.size()==0|| _chosenCities.contains(city)) doc_tf.add(docInt, tf);
        // else- nothing.

        return doc_tf;
    }

    /**
     * build {@link #_doc_size}
     */
    private void build_doc_size() {
        Set<Integer> docsSet = _doc_termPlusTfs.keySet();
        Iterator<Integer> docsIt = docsSet.iterator();
        Integer doc = 0;
        while (docsIt.hasNext()) {
            doc = docsIt.next();
            int size=0;
            //todo amit
            _doc_size.put(doc,size);
        }
    }
}