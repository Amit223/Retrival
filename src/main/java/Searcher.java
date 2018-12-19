import javafx.util.Pair;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
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
    private HashMap<Integer, Vector<Pair<String, Integer>>> _doc_termPlusTfs;
    private static ConcurrentHashMap<Integer, Integer> _doc_size = new ConcurrentHashMap<>();
    private ConcurrentHashMap<Integer, Collection<String>> _doc_Entities = new ConcurrentHashMap<>();
    private static ConcurrentHashMap<String, Integer> _term_docsCounter = new ConcurrentHashMap<>();
    private String _path = "";


    public Searcher(double avgldl, int numOfIndexedDocs, String path, HashSet<String> chosenCities) {
        _numOfIndexedDocs = numOfIndexedDocs;
        _avgldl = avgldl;//_indexer.getAvgldl();no!
        _doc_termPlusTfs = new HashMap<>();
        _doc_size = new ConcurrentHashMap<>();
        _chosenCities = chosenCities;
        _path=path;
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
     * @param ans-the doclines of top documents
     * @return _RankedEntities - the 5 most dominant Entities, filter the fake entities using {@link #dictionary}
     */



    public void getEntities(Collection<Integer> ans) {
        try {
            RandomAccessFile raf=new RandomAccessFile(_path+"/Entities.txt","r");
            for (Integer docNum : ans) {
                byte[]line=new byte[100];
                raf.seek(docNum);
                raf.read(line);//20 bytes each term!
                Vector<String> Entities = findEntities(line);
                if (!_doc_Entities.containsKey(docNum)) _doc_Entities.put(docNum, Entities);
            }
            raf.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     *
     * @param line-byte[100] - 5 entities in it
     * @return vector of the 5 entities in the line
     */
    private Vector<String> findEntities(byte[] line) {
        byte [] e1=new byte[20];
        byte [] e2=new byte[20];
        byte [] e3=new byte[20];
        byte [] e4=new byte[20];
        byte [] e5=new byte[20];
        for(int i=0;i<20;i++){
            e1[i]=line[i];
            e2[i]=line[i+20];
            e3[i]=line[i+40];
            e4[i]=line[i+60];
            e5[i]=line[i+80];
        }
        String entity1=new String(e1);
        String entity2=new String(e2);
        String entity3=new String(e3);
        String entity4=new String(e4);
        String entity5=new String(e5);
        //substring(###)
        entity1=entity1.substring(0,entity1.indexOf("#"));
        entity2=entity2.substring(0,entity2.indexOf("#"));
        entity3=entity3.substring(0,entity3.indexOf("#"));
        entity4=entity4.substring(0,entity4.indexOf("#"));
        entity5=entity5.substring(0,entity5.indexOf("#"));
        Vector<String> entities=new Vector<>();
        entities.add(entity1);
        entities.add(entity2);
        entities.add(entity3);
        entities.add(entity4);
        entities.add(entity5);
        return entities;
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
        FilterDocsByCitys();
        ans = _ranker.Rank(_doc_termPlusTfs, _doc_size, _numOfIndexedDocs, _term_docsCounter, _avgldl); // return only 50 most relvante
        getEntities(ans);
        return ans;
    }

    private void FilterDocsByCitys() {
        try {
            RandomAccessFile raf=new RandomAccessFile(_path+"/Documents.txt","r");
            Vector<Integer> toDelete=new Vector<>();
            Iterator<Integer> iterator=_doc_termPlusTfs.keySet().iterator();
            while(iterator.hasNext()){
                Integer docLine=iterator.next();
                //filter stage
                if(_chosenCities.size()>0) {
                    raf.seek(docLine * 54 + 16);//to get to the city
                    byte[] city_bytes = new byte[16];
                    raf.read(city_bytes);
                    String city  = new String(city_bytes, StandardCharsets.UTF_8);
                    if(city.contains("#"))
                        city = city.substring(0, city.indexOf("#"));
                    if(city.equals("")||!_chosenCities.contains(city)){
                        toDelete.add(docLine);
                    }
                }
            }
            //delete all the toDelete
            for(int i=0;i<toDelete.size();i++){
                int lineNum=toDelete.get(i);
                _doc_termPlusTfs.remove(lineNum);
            }
            iterator=_doc_termPlusTfs.keySet().iterator();//all the remain ones
            //add to each doc it's size
            while (iterator.hasNext()){
                int docLine=iterator.next();
                raf.seek(docLine * 54 + 50);//to get to the size
                byte[] length_bytes=new byte[4];
                raf.read(length_bytes);
                int size=convertByteToInt(length_bytes);
                _doc_size.put(docLine,size);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private int convertByteToInt(byte[] b)
    {

        int pomAsInt = ByteBuffer.wrap(b).getInt();
        return pomAsInt;

    }
}



