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
    private double _avgldl = 0; //average document length
    private HashSet<String> _chosenCities; //if (_chosenCities.size()==0|| _chosenCities.contains(city)) add it. else- not
    private Map<String, Vector<Integer>> dictionary;


    /**
     * hashmap of
     * docline in {@link Indexer#documents} to Vector of Pairs of term-tf.
     */
    private ConcurrentHashMap<Integer, Vector<Pair<String, Integer>>> _doc_termPlusTfs;
    private static ConcurrentHashMap<Integer, Integer> _doc_size = new ConcurrentHashMap<>();
    private ConcurrentHashMap<Integer, Collection<String>> _doc_Entities = new ConcurrentHashMap<>();
    private static ConcurrentHashMap<String, Integer> _term_docsCounter = new ConcurrentHashMap<>();
    private String _path = "";


    public Searcher(double avgldl, int numOfIndexedDocs, String path, HashSet<String> chosenCities) {
        _numOfIndexedDocs = numOfIndexedDocs;
        _avgldl = avgldl;//_indexer.getAvgldl();
        _doc_termPlusTfs = new ConcurrentHashMap<>();
        _doc_size = new ConcurrentHashMap<>();
        _chosenCities = chosenCities;
    }

    /**Auxiliary functions for Search**/

    /**
     * build {@link #_doc_termPlusTfs} using {@link #addDocsTo_doc_termPlusTfs(String)}
     *
     * @param query  - query to search
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
     * Auxiliary function for {@link #build_doc_termPlusTfs(String, boolean)}
     * add docs that the term appear in them to our {@link #_doc_termPlusTfs} using {@link #readTermDocs(String)}
     *
     * @param term - the term that adding the docs that include it.
     *             working in concurrency
     */
    private void addDocsTo_doc_termPlusTfs(String term) {
        Vector<Pair<Integer, Integer>> doc_tf = readTermDocs(term);
        _term_docsCounter.put(term, doc_tf.size());
        for (int i = 0; i < doc_tf.size(); i++) {
            Integer docNum = doc_tf.get(i).getKey();
            Integer termTfInDoc = doc_tf.get(i).getValue();
            if (_doc_termPlusTfs.containsKey(docNum)) {
                _doc_termPlusTfs.get(docNum).add(new Pair<String, Integer>(term, termTfInDoc)); //todo - future error - if not update, do put after getting copy.
            } else {
                _doc_termPlusTfs.put(docNum, new Vector<Pair<String, Integer>>());
                _doc_termPlusTfs.get(docNum).add(new Pair<String, Integer>(term, termTfInDoc)); //todo - future error - if not update, do put after getting copy.
            }
        }
    }

    /**
     * Auxiliary function for {@link #build_doc_termPlusTfs(String, boolean)}
     * using {@link #_chosenCities} and not insert doc that their city is one of the cities given from the user
     * if meet doc in the first time, add his entities and his size to their maps( {@link #_doc_size} {@link #_doc_Entities}).
     *
     * @param term - the docs that include  the term
     * @return doc_tf - list of "doc-tf"s for the above term
     */
    private Vector<Pair<Integer, Integer>> readTermDocs(String term) {
        Vector<Pair<Integer, Integer>> doc_tf = new Vector<>();
        while ("there is more docs for this term in the postings.".contains("")) {  //todo amit
            String city = "";  //todo get amit
            int docNum = 0, //todo  get amit
                    docSize = 0, //todo  get amit
                    tf = 0; //todo  get amit

            if (_chosenCities.size() == 0 || _chosenCities.contains(city)) {
                doc_tf.add(new Pair<Integer, Integer>(docNum, tf));
                if (!_doc_size.containsKey(docNum)) _doc_size.put(docNum, docSize);
            } else {//do nothing}
            }
        }
        return doc_tf;
    }


    /**
     * using for {@link #Search(String, boolean)}
     * copy from {@link Indexer#loadDictionaryToMemory()}
     * this function load to dictionary ( if not loaded yet) the information from dictionary file.
     * return true if successful, false otherwise
     */
    private boolean loadDictionaryToMemory(boolean toStem) {
        if (dictionary == null) {
            try {
                dictionary = new TreeMap<>();
                BufferedReader bufferedReader = new BufferedReader(new FileReader(_path + "/" + toStem + "Dictionary.txt"));
                String line = bufferedReader.readLine();
                String[] lines = line.split("=");
                for (int i = 0; i < lines.length; i++) {
                    String[] pair = lines[i].split("--->");
                    if (pair.length == 2) {
                        String[] values = pair[1].split("&");
                        int df = Integer.parseInt(values[0].substring(1, values[0].length()));
                        int tf = Integer.parseInt(values[1]);
                        int ptr = Integer.parseInt(values[2].substring(0, values[2].length() - 1));
                        Vector<Integer> vector = new Vector<>();
                        vector.add(df);
                        vector.add(tf);
                        vector.add(ptr);
                        dictionary.put(pair[0], vector);
                    }
                }
                bufferedReader.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return true;
    }

    /**
     * @param Entities
     * @return _RankedEntities - the 5 most dominant Entities, filter the fake entities using {@link #dictionary}
     */
    public Collection<String> getFinal5Entities(Vector<String> Entities) {
        SortedMap<Integer, String> RankedEntities = new TreeMap<>();
        for (int i = 0; i < Entities.size(); i++) {
            String entity = Entities.get(i);
            if (dictionary.containsKey(entity.toUpperCase())) { //is upper case in all the corpus.
                int tf = dictionary.get(entity.toUpperCase()).get(1); //means tf ,like in the #loadDictionaryToMemory
                if (RankedEntities.size() > 5) {
                    Integer lowestTf = RankedEntities.firstKey();

                    if (tf > (lowestTf)) {
                        RankedEntities.remove(lowestTf);
                        RankedEntities.put(tf, entity);
                    } else { //don't add
                    }
                } else RankedEntities.put(tf, entity);
            }
        }
        return RankedEntities.values();
    }


    private void getEntities(Collection<Integer> ans) {
        for (Integer docNum : ans) {
            Vector<String> Entities = new Vector<>(); //todo amit - get from the postings.
            if (!_doc_Entities.containsKey(docNum)) _doc_Entities.put(docNum, getFinal5Entities(Entities));
        }
    }


    /**
     * this class get query (string with spaces), parse it to final terms dictionary @link {@link Parser#_termList}.
     * for this dictionary<term,tf> this class return list of 50 most relevant documents using {@link Ranker}
     *
     * @param query  - to retrieve
     * @param toStem - if true stem, else not.
     * @return list of relevant docs.
     */
    public Collection<Integer> Search(String query, boolean toStem) {
        Collection<Integer> ans = new Vector<>();
        loadDictionaryToMemory(toStem); //using for "Entities"
        build_doc_termPlusTfs(query, toStem);
        ans = _ranker.Rank(_doc_termPlusTfs, _doc_size, _numOfIndexedDocs, _term_docsCounter, _avgldl); // return only 50 most relvante
        getEntities(ans);
        return ans;
    }
}



