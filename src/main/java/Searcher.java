import javafx.util.Pair;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * this class get query (string with spaces), parse it to final terms dictionary @link {@link Parser#_termList}.
 * for this dictionary<term,tf> this class return list of 50 most relevant documents using {@link Ranker}
 * the main function in this class is {@link #Search(String, String, boolean)}
 */
public class Searcher {

    private Ranker _ranker = new Ranker();
    private int _numOfIndexedDocs;
    private double _avgldl = 0; //average document length
    private HashSet<String> _chosenCities; //if (_chosenCities.size()==0|| _chosenCities.contains(city)) add it. else- not
    private Map<String, Vector<Integer>> dictionary;
    private boolean toStem;


    /**
     * hashmap of
     * docline in {@link Indexer#documents} to Vector of Pairs of term-tf.
     */
    private HashMap<Integer, Vector<Pair<String, Integer>>> _doc_termPlusTfs;
    private static ConcurrentHashMap<Integer, Integer> _doc_size = new ConcurrentHashMap<>();
    private ConcurrentHashMap<Integer, Map<String,Integer>> _doc_Entities = new ConcurrentHashMap<>();
    private static ConcurrentHashMap<String, Integer> _term_docsCounter = new ConcurrentHashMap<>();
    private String _path = "";
    private boolean isSemantic; //todo what defualt ?


    public Searcher(double avgldl, int numOfIndexedDocs, String path, HashSet<String> chosenCities,boolean toStem) {
        _numOfIndexedDocs = numOfIndexedDocs;
        _avgldl = avgldl;//_indexer.getAvgldl();no!
        _doc_termPlusTfs = new HashMap<>();
        _doc_size = new ConcurrentHashMap<>();
        _chosenCities = chosenCities;
        _path=path;
        this.toStem=toStem;
        loadDictionaryToMemory(toStem); //using for "Entities"
    }

    /**Auxiliary functions for Search**/

    /**
     * build {@link #_doc_termPlusTfs} using {@link #addDocsTo_doc_termPlusTfs(String)}
     *
     * @param query  - query to search
     * @param toStem if stem is needed
     */
    private void build_doc_termPlusTfs(String query, boolean toStem) {
        if(isSemantic)
            query= treatSemantic(query);
        StopWords.setStopwords("");
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
     * return the query with semantic similiar words appended.
     * @param query
     * @return
     */
    private String treatSemantic(String query) {
        if(isSemantic){
            String queryWithPluses=  queryWithPluses(query);
            try{
                String urlString = "https://api.datamuse.com/words?ml=" + queryWithPluses;
                URL url = new URL(urlString);
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod("GET");
                InputStreamReader iSR =new InputStreamReader((con.getInputStream()));
                BufferedReader bufferedReader =new BufferedReader(iSR);
                String inputLine;
                StringBuffer similiarSemanticWords= new StringBuffer();
                while((inputLine=bufferedReader.readLine())!=null){
                    int index =0;
                    String [] words = inputLine.substring(2).split("\\{");
                    for(String word: words){
                        if(index==15) break; //todo all ?????
                        String [] wordStruct=  word.split(",")[0].split(":");
                        similiarSemanticWords.append(wordStruct[1].substring(1).replace('"',' '));
                        index++;
                    }
                }
                bufferedReader.close();
                iSR.close();
                return query + " " + similiarSemanticWords.toString();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        else return query;
        return query;
    }

    private String queryWithPluses(String query) {
        String [] words=query.split("\\s+");
        String queryWithPluses="";
        for (int i = 0; i < words.length ; i++) {
            if(i==0)
                queryWithPluses=words[i];
            else{
                queryWithPluses+= ("+" + words[i]);
            }
        }
        return query;
    }


    /**
     * Auxiliary function for {@link #build_doc_termPlusTfs(String, boolean)}
     * add docs that the term appear in them to our {@link #_doc_termPlusTfs} using {@link #readTermDocs(String)}
     *
     * @param term - the term that adding the docs that include it.
     *             working in concurrency
     */
    private void addDocsTo_doc_termPlusTfs(String term) {
        Map<Integer, Integer> doc_tf = readTermDocs(term);
        _term_docsCounter.put(term, doc_tf.size());
        Iterator<Integer>docs=doc_tf.keySet().iterator();
        while (docs.hasNext()) {
            Integer docNum = docs.next();
            Integer termTfInDoc = doc_tf.get(docNum);
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
    private Map<Integer, Integer> readTermDocs(String term) {
        Map<Integer, Integer> doc_tf = new HashMap<>();
        char letter = term.charAt(0);
        if (Character.isUpperCase(letter))
            letter = Character.toLowerCase(letter);
        else if (Character.isDigit(letter) || letter == '-')
            letter = '0';
        String fullPath = _path + '/'+letter + toStem + "Done.txt";
        if (!dictionary.containsKey(term)) {//the term is not in the dictionary
            return doc_tf;
        }
        try {
            RandomAccessFile raf = new RandomAccessFile(new File(fullPath), "r");
            int lineNum = dictionary.get(term).elementAt(2);//the pointer;
            int numOfDocs = dictionary.get(term).elementAt(0);//num of docs
            for (int i = 0; i < numOfDocs; i++) {
                byte[] fullLine=new byte[8];
                raf.seek((lineNum + i) * 8);
                raf.read(fullLine);
                byte[] docLine_bytes = new byte[4];
                docLine_bytes[0]=fullLine[0];
                docLine_bytes[1]=fullLine[1];
                docLine_bytes[2]=fullLine[2];
                docLine_bytes[3]=fullLine[3];
                byte[] tf_bytes = new byte[4];
                tf_bytes[0]=fullLine[4];
                tf_bytes[1]=fullLine[5];
                tf_bytes[2]=fullLine[6];
                tf_bytes[3]=fullLine[7];
                int docLine = byteToInt(docLine_bytes);
                int tf = byteToInt(tf_bytes);
                if(!doc_tf.keySet().contains(docLine))
                    doc_tf.put(docLine,tf);
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
     * using for {@link #Search(String, String, boolean)}
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
     *
     * @param ans
     * put all entities from Entities+toStem+.txt to _doc_Entities
     */
    public void getEntities(Collection<Integer> ans) {
        try {
            RandomAccessFile raf=new RandomAccessFile(_path+"/Entities"+toStem+".txt","r");
            for (Integer docNum : ans) {
                byte[]line=new byte[120];
                raf.seek(docNum*120);
                raf.read(line);//120 bytes each term!
                Map<String,Integer> Entities = findEntities(line);
                if(Entities.size()==1&&Entities.containsKey("X")){
                    if (!_doc_Entities.containsKey(docNum))
                        _doc_Entities.put(docNum, new HashMap<>());//no entities for doc
                }
                else {
                    if (!_doc_Entities.containsKey(docNum))
                        _doc_Entities.put(docNum, Entities);
                }
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
    private Map<String,Integer> findEntities(byte[] line) {
        byte [] e1=new byte[20];
        byte [] e2=new byte[20];
        byte [] e3=new byte[20];
        byte [] e4=new byte[20];
        byte [] e5=new byte[20];
        byte [] f1=new byte[4];
        byte [] f2=new byte[4];
        byte [] f3=new byte[4];
        byte [] f4=new byte[4];
        byte [] f5=new byte[4];
        for(int i=0;i<20;i++){
            //entity1
            e1[i]=line[i];
            //entity2
            e2[i]=line[i+24];
            //entity3
            e3[i]=line[i+48];
            //entity4
            e4[i]=line[i+72];
            //entity5
            e5[i]=line[i+96];
        }
        for(int i=0;i<4;i++){
            f1[i]=line[i+20];
            f2[i]=line[i+44];
            f3[i]=line[i+68];
            f4[i]=line[i+92];
            f5[i]=line[i+116];

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
        //tfs
        int tf1=byteToInt(f1);
        int tf2=byteToInt(f2);
        int tf3=byteToInt(f3);
        int tf4=byteToInt(f4);
        int tf5=byteToInt(f5);

        Map<String,Integer> entities=new HashMap<>();
        if(!entity1.equals(""))
            entities.put(entity1,tf1);
        if(!entity2.equals(""))
            entities.put(entity2,tf2);
        if(!entity3.equals(""))
            entities.put(entity3,tf3);
        if(!entity4.equals(""))
            entities.put(entity4,tf4);
        if(!entity5.equals(""))
            entities.put(entity5,tf5);
        return entities;
    }


    /**
     * this class get query (string with spaces), parse it to final terms dictionary @link {@link Parser#_termList}.
     * for this dictionary<term,tf> this class return list of 50 most relevant documents using {@link Ranker}
     *
     *
     * @param id
     * @param query  - to retrieve
     * @param toTreatSemantic
     * @return list of relevant docs.
     */
    public Collection<Document> Search(String id, String query, boolean toTreatSemantic, boolean firstQuery) {
        Collection<Document> docs= new Vector<>();
        isSemantic=toTreatSemantic;
        build_doc_termPlusTfs(query, toStem);
        FilterDocsByCitys();
        Collection<Integer>  docNums = _ranker.Rank(_doc_termPlusTfs, _doc_size, _numOfIndexedDocs, _term_docsCounter, _avgldl,_path); // return only 50 most relvante
        getEntities(docNums);
        Collection<String> docNames = docNumToNames(docNums,id,firstQuery);
        Iterator<Integer> docNumIt= docNums.iterator();
        Iterator<String> docNameIt= docNames.iterator();
        while (docNumIt.hasNext()) {
            Integer docNum= docNumIt.next();
            Map<String, Integer> entities;
            if(_doc_Entities.contains(docNum)){
               entities = _doc_Entities.get(docNum);
            }
            else  entities = new ConcurrentHashMap<>();
            docs.add(new Document(docNum,docNameIt.next(),entities));
        }
        return docs;
    }

    /**
     *
     * @param ans- doclines
     * @return name of documents
     */
    private Collection<String> docNumToNames(Collection<Integer> ans,String id,boolean flag) {
        //get doc names!
        Set<String> documentsToReturn=new HashSet<>();
        Map<Integer,Double> docLine_rank=_ranker.getDocsRanking();
        Map<String,Double> docs_rank=new HashMap<>();
        try {
            RandomAccessFile raf=new RandomAccessFile(new File(_path+"/Documents.txt"),"r");
            Iterator<Integer> docsIterator=ans.iterator();
            while(docsIterator.hasNext()){
                int lineNum=docsIterator.next();
                raf.seek(lineNum*54);
                byte [] nameInBytes=new byte[16];
                raf.read(nameInBytes);
                String name=convertByteToString(nameInBytes);
                documentsToReturn.add(name);
                docs_rank.put(name,docLine_rank.get(lineNum));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        WriteToQueryFile(docs_rank,id,flag);
        return documentsToReturn;
    }

    private void WriteToQueryFile(Map<String, Double> docs_rank,String id,boolean createNew) {
        File file=new File("results.txt");
        try {
            if(createNew)
                file.createNewFile();
            BufferedWriter writer=new BufferedWriter(new FileWriter(file));
            Iterator<String> docs=docs_rank.keySet().iterator();
            while(docs.hasNext()){
                String document=docs.next();
                String line=id+"   "+0+"   "+document+"   "+docs_rank.get(document)+"42.38   mt";
                writer.write(line);
                writer.newLine();
            }
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

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



