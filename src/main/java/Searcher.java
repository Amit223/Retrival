import javafx.util.Pair;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

/**
 * this class get query (string with spaces), parse it to final terms dictionary @link {@link Parser#_termList}.
 * for this dictionary<term,tf> this class return list of 50 most relevant documents using {@link Ranker}
 * the main function in this class is {@link #Search(String, boolean)}
 */
public class Searcher {

    private Ranker _ranker = new Ranker();
    private final Indexer _indexer;
    /**
     * hashmap of
     * docline in {@link Indexer#documents} to Vector of Pairs of term-tf.
     */
    private ConcurrentHashMap<Integer, Vector<Pair<String, Integer>>> _doc_terms; //using vector to save memory. todo concurrent Vector

    public Searcher(Indexer indexer) {
        _indexer = indexer; //todo if searcher is currently, indexer.
        _doc_terms = new ConcurrentHashMap<>();
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
        Parser queryParser = new Parser();
        queryParser.Parse(query, toStem, "");
        HashMap<String, Integer> termList = queryParser.getTerms();
        Set<String> terms = termList.keySet();
        Iterator<String> termsIt = terms.iterator();
        String term = "";
        while (termsIt.hasNext()) {
            term = termsIt.next();
            addDocsTo_doc_terms(term);
        }
        for (int i = 0; i < _doc_terms.size() ; i++) {
            _ranker.Rank(_doc_terms.get(i)); //todo with threading.
        }
        return ans;
    }

    /**
     * add docs that the term appear in them to our _doc_terms
     * @param term - the term that adding the docs that include it.
     *             working in concurrency
     */
    private void addDocsTo_doc_terms(String term) {
        Vector<Pair<Integer, Integer>> doc_tf = _indexer.readTermDocs(term);
        for (int i = 0; i < doc_tf.size(); i++) {
            Integer docNum = doc_tf.get(i).getKey();
            Integer termTfInDoc = doc_tf.get(i).getValue();
            if (_doc_terms.containsKey(docNum)) {
                _doc_terms.get(docNum).add(new Pair<String, Integer>(term, termTfInDoc));
            }
            else {
                _doc_terms.put(docNum, new Vector<Pair<String, Integer>>());
                _doc_terms.get(docNum).add(new Pair<String, Integer>(term, termTfInDoc));
            }
        }
    }
}
