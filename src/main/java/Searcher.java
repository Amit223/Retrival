import javafx.util.Pair;

import java.io.*;
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
     * build {@link #_doc_termPlusTfs} using {@link #addDocsTo_doc_termPlusTfs(String,boolean)}
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
            addDocsTo_doc_termPlusTfs(term,toStem);
        }
    }


    /**
     * Auxiliary function for {@link #build_doc_termPlusTfs(String, boolean)}
     * add docs that the term appear in them to our {@link #_doc_termPlusTfs} using {@link #readTermDocs(String,boolean)}
     *
     * @param term - the term that adding the docs that include it.
     *             working in concurrency
     */
    private void addDocsTo_doc_termPlusTfs(String term,boolean toStem) {
        Vector<Pair<Integer, Integer>> doc_tf = readTermDocs(term,toStem);
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
    private Vector<Pair<Integer, Integer>> readTermDocs(String term, boolean isStemmed) {
        Vector<Pair<Integer, Integer>> doc_tf = new Vector<>();
        char letter = term.charAt(0);
        if (Character.isUpperCase(letter))
            letter = Character.toLowerCase(letter);
        else if (Character.isDigit(letter) || letter == '-')
            letter = '0';
        String fullPath = _path + '/'+letter + isStemmed + "Done.txt";
        if (!dictionary.containsKey(term)) {//the term is not in the dictionary
            return doc_tf;
        }
        try {
            RandomAccessFile raf = new RandomAccessFile(new File(fullPath), "r");
            int lineNum = dictionary.get(term).elementAt(2);//the pointer;
            int numOfDocs = dictionary.get(term).elementAt(0);//num of docs
            for (int i = 0; i < numOfDocs; i++) {
                raf.seek((lineNum + i) * 8);
                byte[] docLine_bytes = new byte[4];
                raf.read(docLine_bytes);
                raf.seek((lineNum + i) * 8 + 4);
                byte[] tf_bytes = new byte[4];
                raf.read(tf_bytes);
                int docLine = byteToInt(docLine_bytes);
                int tf = byteToInt(tf_bytes);
                Pair<Integer, Integer> pair = new Pair<>(docLine, tf);
                if (!_doc_size.contains(docLine)) {
                    _doc_size.put(docLine, -1);
                }
            }
            raf.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return doc_tf;
    }

    /**
     * @param bytes
     * @return take byte[4] and turn into int- will use next part of project
     */
    private int byteToInt(byte[] bytes) {
        int val = 0;
        for (int i = 0; i < 4; i++) {
            val = val << 8;
            val = val | (bytes[i] & 0xFF);
        }
        return val;
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



    private void getEntities(Collection<Integer> ans) {
        for (Integer docNum : ans) {
            Vector<String> Entities = new Vector<>(); //todo amit - get from the postings.
            if (!_doc_Entities.containsKey(docNum)) _doc_Entities.put(docNum, Entities);
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



