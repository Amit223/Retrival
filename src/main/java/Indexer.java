
import ParseObjects.Number;
import sun.awt.Mutex;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * this class index all documents in corpus to 28 files: 'a'-'z' , '0' (to digit and signs) and to city posting the city details.
 */
public class Indexer {

    private Map<String,Vector<Integer>> dictionary;

    //docs
    private File documents;//docName(16 bytes)|city(16)|language(10)|maxtf(4)|num of terms(4)|words(4)|-54 bytes
    private ListOfByteArrays docsToWrite;
    private Set<String> languages;
    private AtomicInteger _AtomicNumlineDocs;

    //posting
    // lineNum(4 bytes)|tf(4 Bytes)|pte next(4 Bytes) ==12 bytes
    private AtomicInteger _numOfFiles;
    private String _path;
    private boolean _toStem;
    private ListOfByteArrays [] postingLines;
    private int numOfTerms=0;
    private  AtomicInteger _wordCount;//the average length

    private AtomicInteger [] lineCounter;

    //citys
    private Map <String,Vector<String>> cityDictionary;
    private File citysPosting;//docLine(4 B)|loc1|loc2|loc3|
    private ListOfByteArrays cityLines;

    //postingMutex
    private Mutex docMutex;
    private Mutex docFileMutex;
    private Mutex cityMutex;
    private Mutex cityFileMutex;
    private Mutex cityDictionaryMutex;
    private Mutex dictionaryMutex;
    private Mutex [] mutexesPosting; //mutexs of the posting files.
    private Mutex [] mutexesList; //mutexs of the posting files.

    public double getAvgldl() {
        return ((double)_wordCount.get())/_numOfFiles.get();
    }

    public  int getNumberOfDocs() {
        return _numOfFiles.get();
    }

    public int getNumberOfTerms() {
        return this.numOfTerms;
    }



    /**
     * the constructor.,
     * @param toStem to stem - to decide the path of the file- so we have different files to each option.
     * @param path -the path of the directory of the posting files the user wants to save on.
     * initialize all files, mutexes,lists ADT used in indexer(only 1 indexer for program!)
     */
    public Indexer(boolean toStem,String path) {
        try {
            _path=path;
            _toStem=toStem;
            lineCounter= new AtomicInteger[28];//'0', 'a'-'z' , city
            for (int i=0;i<28;i++){
                lineCounter[i]=new AtomicInteger(0);
            }
            if(toStem) {
                documents = new File(path+"/Documents.txt");//docName(20 bytes)|city(18)|maxtf(4)|num of terms(4)|words(4)-50 bytes
                //citys
                citysPosting = new File(path+"/CityPosting.txt");
            }
            else{//not to stem
                documents = new File(path+"/NotStemDocuments.txt");//docName(20 bytes)|city(18)|maxtf(4)|num of terms(4)|words(4)-50 bytes
                //citys
                citysPosting = new File(path+"/NotStemCityPosting.txt");
            }
            documents.createNewFile();
            dictionary = new HashMap<>();
            _AtomicNumlineDocs = new AtomicInteger(0);
            _numOfFiles=new AtomicInteger(0);
            docsToWrite=new ListOfByteArrays();
            languages=new HashSet<>();

            //citys
            cityDictionary = new HashMap<>();
            citysPosting.createNewFile();
            cityLines=new ListOfByteArrays();

            //mutexes&&posting lines
            cityMutex=new Mutex();
            cityFileMutex=new Mutex();
            cityDictionaryMutex=new Mutex();
            docMutex=new Mutex();
            docFileMutex=new Mutex();
            dictionaryMutex=new Mutex();
            mutexesPosting =new Mutex[27];
            mutexesList=new Mutex[27];
            postingLines = new ListOfByteArrays[27];
            for(int i=0; i<mutexesPosting.length;i++){

                mutexesPosting[i]=new Mutex();
                mutexesList[i]=new Mutex();
                postingLines[i]=new ListOfByteArrays();

            }

            //files
            File file;
            for(char c='a';c<='z';c++){
                file=new File(_path+"/"+c+_toStem+".txt");
                file.createNewFile();
            }
            file=new File(_path+"/"+'0'+_toStem+".txt");
            file.createNewFile();
        }
        catch (IOException e){

        }
    }
    /**
     * create inverted index and posting files.
     * @param terms- dictionary of terms, term frequency of the document
     * @param locations - vector of locations of the city
     * @param nameOfDoc
     * @param cityOfDoc
     * @param numOfWords - the number of word in the document
     *
     * * 1) it takes all information needed from {@link Parser}: the dictionary of each doc- list of unique terms and their frequency,
     * vector of location of the city of the documents, name of the document, name of the city, number of words(not unique) in doc,
     * 2)writes to the list of documents details in bytes:docName(16 bytes)|city(16)|language(10)|maxtf(4)|num of terms(4)|words(4)|-54 bytes
     * 3)for each term in dictionary: writes to the dictionary if new term, add to doc frequency and total frequency if exist. Next write the
     * term details: the line in the doc file that represent the document it was found in, it's term frequency and itself so we can
     * use it on stage 6
     * It writes each line to the list of the term first letter-'a'-'z' or if digit or sign writes to the list of '0'
     * 4)for the city of doc find it's details on geobytes, write to the dictionary of citys all the details:the city,
     * the population and the coin if new.
     * next writes to the list of citys - the city, 3 locations and line of document (if there is more than 3,
     *                   it will make another line. it will be used later when we write the details in bytes and then can
     *                   access directly to the line(fixed size)
     *5)every few docs(1000 to terms and 5000 to citys and docs), it will write the lists to the file and erase the lists.
     *6) when we done indexing from outside we will sort each file-the posting
     *               and citys(not docs!)
     *
     *
     */
    public void Index(Map<String,Integer> terms,Vector<Integer> locations,String nameOfDoc,String cityOfDoc,
                      int numOfWords,String language){

        //docs
        Set<String> keys=terms.keySet();
        int maxtf=0;
        if(terms.values()!=null&&terms.values().size()>0)
            maxtf=getMaxTf(terms.values());
        docMutex.lock();
        writeToDocumentsList(nameOfDoc,cityOfDoc,maxtf,terms.size(),numOfWords,language); //
        docMutex.unlock();
        _AtomicNumlineDocs.getAndAdd(1);
        int lineNumDocs = _AtomicNumlineDocs.get()-1;
        if(_numOfFiles.get()%5000==0){
            writeDocsToFile();
        }

        //posting
        Iterator<String>termsKeys=keys.iterator();
        while (termsKeys.hasNext()) {
            String term = termsKeys.next();
            dictionaryMutex.lock();
            ifExistUpdateTF(term,terms.get(term)); //updates dictionary df/ add new term
            dictionaryMutex.unlock();
            int index = Character.toLowerCase(term.charAt(0)) - 'a' + 1;
            if (index <0||index>26)//^^
                index = 0;
            writeToPostingList(lineNumDocs,terms.get(term),postingLines[index],term);
        }
        terms.clear();
        if(_numOfFiles.get()%1000==0)
            writeListToPosting();



        //citys
        if(cityOfDoc.length()!=0) {
            if(!cityDictionary.containsKey(cityOfDoc)) {
                String details = getCityDetails(cityOfDoc);
                String[] strings = details.split("-");
                cityDictionaryMutex.lock();
                toCitysDictionary(cityOfDoc, strings[0], strings[1], strings[2]);
                cityDictionaryMutex.unlock();
            }
            if (locations.size() > 0 ) {//need only if in file to add
                toCityPostingList(cityOfDoc,locations, lineNumDocs);
            }
        }
        locations.clear();
        if(_numOfFiles.get()%5000==0){
            writeCityList();
        }

        _numOfFiles.getAndAdd(1);

    }

    public void UpdateDictionary(){
        Thread[] threads = new ThreadedUpdate[27];
        ExecutorService pool = Executors.newFixedThreadPool(8);

        int i = 1;

        threads[0] = new ThreadedUpdate(_path + "/" + '0' + _toStem + ".txt",
                _path + "/" + '0' + _toStem +"Done"+ ".txt",
                dictionary,dictionaryMutex);
        pool.execute(threads[0]);
        for (char c = 'a'; c <= 'z'; c++) {
            threads[i] =new ThreadedUpdate(_path + "/" + c + _toStem + ".txt",
                    _path + "/" + c + _toStem +"Done"+ ".txt",
                    dictionary,dictionaryMutex);;
            i++;
        }
        //the city
        for(int j=1;j<=26;j++){
                pool.execute(threads[j]);
        }


        pool.shutdown();

        try {
            boolean flag = false;
            while (!flag)
                flag = pool.awaitTermination(500, TimeUnit.MILLISECONDS);

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    /**
     * using for {@link Model}
     * @return dictionary- can be null if loaded to disk
     */
    public Map<String, Vector<Integer>> getDictionary() {
        return dictionary;
    }


    /**
     *
     * @return city dictionary- can be null
     */
    public Map<String, Vector<String>> getCityDictionary() {
        loadCityDictionaryToMemory();
        return cityDictionary;
    }

    /**
     * using for {@link Model}
     * this function load to dictionary ( if not loaded yet) the information from dictionary file.
     * return true if successful, false otherwise
     */
    public boolean loadDictionaryToMemory() {
        if(dictionary==null) {
            try {
                dictionary = new TreeMap<>();
                BufferedReader bufferedReader = new BufferedReader(new FileReader(_path + "/" + _toStem + "Dictionary.txt"));
                String line = bufferedReader.readLine();
                String[] lines = line.split("=");
                for (int i = 0; i < lines.length; i++) {
                    String[] pair = lines[i].split("--->");
                    if(pair.length==2){
                        String [] values=pair[1].split("&");
                        int df=Integer.parseInt(values[0].substring(1,values[0].length()));
                        int tf=Integer.parseInt(values[1]);
                        int ptr=Integer.parseInt(values[2].substring(0,values[2].length()-1));
                        Vector<Integer> vector=new Vector<>();
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
     * using for {@link Model}
     * load the dictionary to the file and deletes it
     */
    public void loadDictionaryToFile(){
        StringBuilder stringBuilder=new StringBuilder();
        Iterator<String> iterator=dictionary.keySet().iterator();
        int i=0;
        BufferedWriter writer = null;
        try {

            writer = new BufferedWriter(new FileWriter(_path + "/" + _toStem + "Dictionary.txt"));
            while (iterator.hasNext()) {
                String key = iterator.next();
                stringBuilder.append(key + "--->{" + dictionary.get(key).elementAt(0) + "&" + dictionary.get(key).elementAt(1)
                        + "&" + dictionary.get(key).elementAt(2) + "}=");
                if (i % 5000 == 0){
                    writer.write(stringBuilder.toString());
                    stringBuilder.setLength(0);

                }
                i++;
            }
            writer.write(stringBuilder.toString());
            writer.flush();
            writer.close();
            numOfTerms = dictionary.size();
            dictionary.clear();
            dictionary = null;

        }
        catch ( IOException e)
        {
        }


    }

    /**
     * load
     * @return
     */
    public boolean loadCityDictionaryToMemory() {
        if(cityDictionary==null) {
            try {
                cityDictionary = new HashMap<>();
                BufferedReader bufferedReader = new BufferedReader(new FileReader(_path + "/" + "CityDictionary.txt"));
                String line = bufferedReader.readLine();
                String[] lines = line.split("=");
                for (int i = 0; i < lines.length; i++) {
                    String[] pair = lines[i].split("--->");
                    if(pair.length==2){
                        String[] values=pair[1].split(",");
                        Vector<String> vector=new Vector();
                        vector.add(values[0].substring(1,values[0].length()));//the country
                        vector.add(values[1]);//the coin
                        vector.add(values[2].substring(0,values[2].length()-1));//population
                        cityDictionary.put(pair[0],vector);
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
     * using for {@link Model#Start(boolean, String, String)}
     * load the city dictionary to the file and deletes it
     */
    public void loadCityDictionaryToFile(){
        StringBuilder stringBuilder=new StringBuilder();
        if(cityDictionary.size()>0) {
            Iterator<String> iterator = cityDictionary.keySet().iterator();
            while (iterator.hasNext()) {
                String key = iterator.next();
                if (cityDictionary.get(key) != null) {
                    String value = cityDictionary.get(key).toString();
                    stringBuilder.append(key + "--->" + value + "=");
                } else {
                }
            }
            BufferedWriter writer = null;
            try {
                writer = new BufferedWriter(new FileWriter(_path + "/" + "CityDictionary.txt"));
                writer.write(stringBuilder.toString());
                writer.flush();
                writer.close();
                cityDictionary.clear();
                cityDictionary = null;


            } catch (IOException e) {
            }

        }
    }



    //write functions
    /**
     * using for {@link #push()} and {@link #Index(Map, Vector, String, String, int, String)}
     * this functiob creates 27 {@link ThreadedWrite} objects that each one write to it's own file.
     *
     */
    private void writeListToPosting() {
        Thread [] threads=new ThreadedWrite[27];
        ExecutorService pool= Executors.newFixedThreadPool(8);

        int i=1;
        File f=new File(_path + "/" + '0' + _toStem+".txt");
        threads[0]=new ThreadedWrite(f,postingLines[0],mutexesPosting[0],mutexesList[0],lineCounter[0]);
        pool.execute(threads[0]);
        for(char c='a';c<='z';c++){
            f=new File(_path + "/" + c + _toStem+".txt");
            threads[i]=new ThreadedWrite(f,postingLines[i],mutexesPosting[i],mutexesList[i],lineCounter[i]);
            pool.execute(threads[i]);
            i++;
        }
        pool.shutdown();

        try {
            boolean flag = false;
            while (!flag)
                flag = pool.awaitTermination(1000, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    /**
     * call {@link ThreadedWrite} to write the list of files to end of documents, erase the list.
     */
    private void writeDocsToFile() {
        Thread t=new ThreadedWrite(documents,docsToWrite,docMutex,docFileMutex,new AtomicInteger(0));//dont care
        t.start();
        try {
            t.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    /**
     * call {@link ThreadedWrite} to write the list of citys to end of city posting, erase the list.
     */
    private void writeCityList() {
        Thread t=new ThreadedWrite(citysPosting,cityLines,cityMutex,cityFileMutex,lineCounter[27]);
        t.start();
        try {
            t.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * this function activated at the end of the index process, activates all the write functions - writeListToPosting(),
     * writeDocsToFile(),writeCityList() so data isn't lost.
     */
    public void push(){
        writeListToPosting();
        writeCityList();
        writeDocsToFile();


    }


    /**
     *
     * @return the languages set
     */
    public Set<String> getLanguages(){
        return languages;
    }

    /**
     * delete all the files- posting and dictionarys
     */
    public boolean delete(){
        try {
            //docs
            _AtomicNumlineDocs.set(0); ;
            documents.delete();
            //city
            citysPosting.delete();
            //files
            for(char c='a';c<='z';c++){
                File f=new File(_path + "/" + c + _toStem+".txt");
                f.delete();

            }
            File f=new File(_path + "/" + '0' + _toStem+".txt");
            f.delete();
            //the dictionarys
            f=new File(_path+"/"+_toStem+"Dictionary.txt");
            f.delete();
            f=new File(_path+"/CityDictionary.txt");
            f.delete();

            if(dictionary!=null) {
                dictionary.clear();
                dictionary=null;
            }
            if(cityDictionary!=null) {
                cityDictionary.clear();
                cityDictionary=null;
            }
            languages.clear();
            return true;
        }
        catch (Exception e){
            System.out.println("problem in indexer->delete");
        }
        return false;
    }


    /**
     *
     * @return num of files the were indexed
     */
    public int get_numOfFiles(){
        return _numOfFiles.get();
    }


    /**
     *
     * @param locations of city in the doc
     *
     * adds all vector of location to the city posting list ( not to file)
     */
    private void toCityPostingList(String city, Vector<Integer> locations, int lineNumDoc) {

        StringBuilder toWrite=new StringBuilder();
        int index=locations.size();
        int iterator=0;
        while(index>3){
            toWrite.append(city);
            toWrite.append(" ");
            toWrite.append(lineNumDoc);
            toWrite.append(" ");
            toWrite.append(locations.get(iterator));
            iterator++;
            toWrite.append(" ");
            toWrite.append(locations.get(iterator));
            iterator++;
            toWrite.append(" ");
            toWrite.append(locations.get(iterator));
            iterator++;
            toWrite.append("\n");
            index=index-3;
        }
        toWrite.append(city);
        toWrite.append(" ");
        toWrite.append(lineNumDoc);
        toWrite.append(" ");
        //first
        if(iterator<locations.size()) {
            toWrite.append(locations.get(iterator));
            toWrite.append(" ");
            iterator++;
        }
        else{
            toWrite.append("#");
            toWrite.append(" ");
        }
        //seconds
        if(iterator<locations.size()) {
            toWrite.append(locations.get(iterator));
            toWrite.append(" ");
            iterator++;
        }
        else{
            toWrite.append("#");
            toWrite.append(" ");
        }
        //third-last
        if(iterator<locations.size()) {
            toWrite.append(locations.get(iterator));
            iterator++;
            toWrite.append("\n");
        }
        else{
            toWrite.append("#");
            toWrite.append("\n");
        }
        cityMutex.lock();
        cityLines.add(toWrite.toString());
        cityMutex.unlock();
    }


    /**
     *
     * @param lineOfDoc- line in doc file of document of term
     * @param tf of term in document
     * @param postingLine the lists we
     * @param term we want to add
     *
     *            writes to the list the term line
     */
    private void writeToPostingList(int lineOfDoc, int tf, ListOfByteArrays postingLine, String term ) {
        StringBuilder toWrite=new StringBuilder();
        toWrite.append(term);
        toWrite.append("&");
        toWrite.append(lineOfDoc);
        toWrite.append("-");
        toWrite.append(tf);
        toWrite.append("\n");
        int index = Character.toLowerCase(term.charAt(0)) - 'a' + 1;
        if (index <0||index>26)//^^
            index = 0;
        mutexesList[index].lock();
        postingLine.add(toWrite.toString());
        mutexesList[index].unlock();

    }

    /**
     *
     * @param nameOfDoc
     * @param cityOfDoc
     * @param maxtf
     * @param size- num of unique words in document
     * @param numOfWords - num of total words in doc
     *
     * writes to the docs file the document details-
     * docName(20 bytes)|city(18)|maxtf(4)|num of terms(4)|words(4)-50 bytes

     */
    private void writeToDocumentsList(String nameOfDoc, String cityOfDoc, int maxtf, int size, int numOfWords,String language) {
        _wordCount.getAndAdd( numOfWords); //todo - future error-  not added .
        //docName(16 bytes)|city(16)|language(16)|maxtf(4)|num of terms(4)|words(4)-50 bytes
        byte[] name=stringToByteArray(nameOfDoc,16);
        byte[] city=stringToByteArray(cityOfDoc,16);
        byte [] lang_bytes=stringToByteArray(language,10);
        if(language.length()>0){
            languages.add(language);
        }
        byte [] maxtf_bytes=toBytes(maxtf);
        byte [] size_bytes=toBytes(size);
        byte [] words_bytes=toBytes(numOfWords);
        for (int i = 0; i < 16; i++) {
            docsToWrite.add(name[i]);
        }
        for (int i = 0; i <16 ; i++) {
            docsToWrite.add(city[i]);
        }
        for (int i = 0; i <10 ; i++) {
            docsToWrite.add(lang_bytes[i]);
        }

        for (int i = 0; i < 4; i++) {
            docsToWrite.add(maxtf_bytes[i]);
        }
        for (int i = 0; i <4 ; i++) {
            docsToWrite.add(size_bytes[i]);
        }

        for (int i = 0; i < 4; i++) {
            docsToWrite.add(words_bytes[i]);
        }


    }

    /**
     *
     * @param cityOfDoc
     * @param country
     * @param coin
     * @param population
     * add to dictionary of citys so key is city and value is vector of it's info
     */
    private void toCitysDictionary(String cityOfDoc, String country, String coin, String population) {
        if(!cityDictionary.containsKey(cityOfDoc)) {
            Vector<String> v = new Vector<>();
            v.add(country);
            v.add(coin);
            v.add(population);
            cityDictionary.put(cityOfDoc, v);
        }
        //else- exist dont need to add
    }

    /**
     *
     * @param city
     * @return state, coin, population of the city from API-geobytes
     */
    private String getCityDetails(String city){
        String s="http://getcitydetails.geobytes.com/GetCityDetails?fqcn=";
        URL url;
        try {
            // get URL content
            url = new URL(s+city);
            URLConnection conn = url.openConnection();

            // open the stream and put it into BufferedReader
            BufferedReader br = new BufferedReader(
                    new InputStreamReader(conn.getInputStream()));
            String string=br.readLine();
            String currency=string.substring(string.indexOf("\"geobytescurrencycode\":")+24,string.indexOf("geobytestitle")-3);
            if(currency.length()==0)
                currency="X";
            String country=string.substring(string.indexOf("\"geobytescountry\":")+19,string.indexOf("geobytesregionlocation")-3);
            if(country.length()==0)
                currency="X";
            String population=string.substring(string.indexOf("\"geobytespopulation\":")+22,string.indexOf("geobytesnationalityplural")-3);
            if(population.length()==0)
                population="X";
            else {
                population = Number.Parse(population);
                if (population.contains(".")) {
                    char mod = population.charAt(population.length() - 1);
                    double num = Double.parseDouble(population.substring(0, population.length() - 1));
                    num = Math.round(num * 100.0) / 100.0;
                    population = Double.toString(num) + mod;
                }
            }
            br.close();
            return country+"-"+currency+"-"+population;

        } catch (Exception e) {
            return "X-X-X";
        }
    }



    /**
     *
     * @param term - update df if exist- the df-++, else add to dictionary
     */
    private void ifExistUpdateTF(String term, int tf) {
        if(dictionary.containsKey(term)){//just add to df and return the line in posting
            Vector<Integer> termDetails= dictionary.get(term);
            int df=termDetails.elementAt(0)+1;
            int tottf=termDetails.elementAt(1)+tf;
            int ptr=-1;
            Vector v=new Vector();
            v.add(df);
            v.add(tf);
            v.add(ptr);
            dictionary.remove(term);
            dictionary.put(term,v);
        }
        //check if exist in the dictionary in diffrent way.
        else if(dictionary.containsKey(Reverse(term))){//the reversed term in dictionary need to put the uppercase one
            String newTerm;
            if(Character.isLowerCase(Reverse(term).charAt(0))){//the term in dictionary is the lowercase one
                newTerm=Reverse(term);
            }
            else{//the new term is the lower case one!
                newTerm=term;
            }
            //take the details of its if exist.
            Vector<Integer> termDetails = dictionary.get(Reverse(term));
            int df=termDetails.elementAt(0)+1;
            int tottf=termDetails.elementAt(1)+tf;
            int ptr=-1;
            Vector v=new Vector();
            v.add(df);
            v.add(tf);
            v.add(ptr);
            dictionary.remove(term);
            dictionary.put(newTerm,v);
        }
        else {//new term completely
            Vector v=new Vector();
            v.add(1);
            v.add(tf);
            v.add(-1);
            dictionary.put(term,v);

        }
    }

    /**
     *
     * @param term
     * @return term in lowercase if is uppercase and lower case otherwise
     */
    public static String Reverse(String term) {
        int offset = 'a' - 'A';
        char c=term.charAt(0);
        if(c >= 'A' && c <= 'Z'){//turn to small
            term=term.toLowerCase();
        }
        if((c >= 'a' && c <= 'z'))//turn to big letters
        {
            term=term.toUpperCase();
        }
        return term;
    }


    /**
     *
     * @param bytes
     * @return take byte[4] and turn into int- will use next part of project
     */
    private int byteToInt(byte[] bytes) {
        int val = 0;
        for (int i = 0; i < 4; i++) {
            val=val<<8;
            val=val|(bytes[i] & 0xFF);
        }
        return val;
    }

    /**
     *
     * @param i-integer
     * @return integer in byte array of size 4
     */
    public static byte[] toBytes(int i)
    {
        byte[] result = new byte[4];

        result[0] = (byte) (i >> 24);
        result[1] = (byte) (i >> 16);
        result[2] = (byte) (i >> 8);
        result[3] = (byte) (i /*>> 0*/);

        return result;
    }


    /**
     *
     * @param elements
     * @return the max tf of term in doc
     */
    private int getMaxTf(Collection<Integer> elements) {
        Iterator<Integer> iterator=elements.iterator();
        int max=iterator.next();
        while(iterator.hasNext()){
            int num=iterator.next();
            if(num>max)
                max=num;
        }
        return max;
    }

    /**
     *
     * @param term
     * @return converts string into byte array of size length
     */
    private byte[] stringToByteArray(String term,int length){
        byte [] stringInByte=term.getBytes(StandardCharsets.UTF_8);
        byte [] fullByteArray=new byte[length];
        for (int i = 0; i<length ; i++) {
            if(i<stringInByte.length)
                fullByteArray[i]=stringInByte[i];
            else
                fullByteArray[i]=35;//# in ascii
        }
        return fullByteArray;

    }


    /**
     * SORT EACH FILE-
     * calls {@link ThreadedSort} for each file- posting and citys
     */
    public void sort() {

        Thread[] threads = new ThreadedSort[28];
        ExecutorService pool = Executors.newFixedThreadPool(2);

        int i = 1;

        threads[0] = new ThreadedSort(_path + "/" + '0' + _toStem + ".txt",lineCounter[0].intValue(),'0');
        pool.execute(threads[0]);
        for (char c = 'a'; c <= 'z'; c++) {
            threads[i] = new ThreadedSort(_path + "/" + c + _toStem + ".txt",lineCounter[i].intValue(),c);
            i++;
        }
        //the city
        threads[27]=new ThreadedSort(_path+"/CityPosting.txt",lineCounter[27].intValue(),'A');
        for(int j=1;j<=27;j++){
            if(j!=20){
                pool.execute(threads[j]);
            }
        }


        pool.shutdown();

        try {
            boolean flag = false;
            while (!flag)
                flag = pool.awaitTermination(500, TimeUnit.MILLISECONDS);
            threads[20].run();
            try {
                threads[20].join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }
}

/**
 * this class extends thread and execute write to file
 */
class ThreadedWrite extends Thread{

    File file;
    ListOfByteArrays list;
    Mutex postingMutex;
    Mutex listMutex;
    AtomicInteger counter;

    /**
     *
     * @param file to write to
     * @param list to write from
     * @param postingMutex
     * @param listMutex
     */
    public ThreadedWrite(File file,ListOfByteArrays list,Mutex postingMutex,Mutex listMutex,AtomicInteger lineCount) {
        this.file=file;
        this.list=list;
        this.postingMutex =postingMutex;
        this.listMutex=listMutex;
        this.counter=lineCount;
    }

    /**
     * write the list to the file
     */
    public void run(){
        try {
            listMutex.lock();
            postingMutex.lock();

            StringBuilder stringBuilder=new StringBuilder();
            for(int i=0;i<list.size();i++){

                stringBuilder.append(list.get(i));
            }
            String list_=stringBuilder.toString();
            BufferedWriter bufferedWriter=new BufferedWriter(new FileWriter(file.getAbsolutePath(),true));
            bufferedWriter.write(stringBuilder.toString());
            bufferedWriter.flush();
            bufferedWriter.close();
            counter.addAndGet(list.size());
            postingMutex.unlock();
            list.delete();
            listMutex.unlock();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}

/**
 * this class extends thread and execute sort of file
 */
class ThreadedSort extends Thread{

    String file;
    int lineCount;
    char letter;


    /**
     * Constructor
     * @param file- to sort
     */
    public ThreadedSort(String file,int lineCount,char letter) {
        this.file = file;
        this.lineCount=lineCount;
        this.letter=letter;
    }

    /**
     * this function split the file into 4 sorted files, merges them into the original one(file)
     */
    public void run() {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(new File(file)));
            File f=new File(letter+"1"+"_"+letter+"2");
            f.createNewFile();
            f=new File(letter+"3"+"_"+letter+"4");
            f.createNewFile();
            f=new File(letter+"5"+"_"+letter+"6");
            f.createNewFile();
            f=new File(letter+"7"+"_"+letter+"8");
            f.createNewFile();
            String write1=SortFileByPart(letter+"1",letter+"2",letter+"1"+"_"+letter+"2",reader,lineCount/4);
            String write2=SortFileByPart(letter+"3",letter+"4",letter+"3"+"_"+letter+"4",reader,lineCount/4);
            String write3=SortFileByPart(letter+"5",letter+"6",letter+"5"+"_"+letter+"6",reader,lineCount/4);
            String write4=SortFileByPart(letter+"7",letter+"8",letter+"7"+"_"+letter+"8",reader,lineCount);
            reader.close();

            String s1=write1+"_"+write2;
            String s2=write3+"_"+write4;
            f=new File(s1);
            f.createNewFile();
            f=new File(s2);
            f.createNewFile();
            Merge(write1,write2,s1);
            Merge(write3,write4,s2);

            PrintWriter printWriter=new PrintWriter(new File(file));
            printWriter.write("");
            printWriter.close();
            Merge(s1,s2,file);
        }
        catch(Exception e){
        }
    }

    /**
     *
     * @param firsthalf name of file of firsthalf
     * @param lasthalf name of file of lasthalf
     * @param toWrite the file we will write to
     * @param reader the reader of main file
     * @param size we want to read
     * @return string of file we wrote to
     * this function read size of lines from main file , split to 2 sorted files, merges to 1.
     */
    private String SortFileByPart(String firsthalf,String lasthalf,String toWrite,BufferedReader reader,int size){
        try {
            //first half:
            Map<String, String> mapCapital = new TreeMap<>();
            Map<String, String> mapNot = new TreeMap<>();
            String line;
            int i = 0;
            for (line = reader.readLine(); line != null && i < (int)size/2; line = reader.readLine()) {
                if (Character.isUpperCase(line.charAt(0))) {
                    mapCapital.put(getField(line), line);
                } else {
                    mapNot.put(getField(line), line);

                }
                i++;
            }

            BufferedWriter writer = new BufferedWriter(new FileWriter(firsthalf, true));
            Iterator<String> capital = null;
            Iterator<String> not = null;
            String capitalS = "";
            String notS = "";
            if (mapCapital.size() > 0) {
                capital = mapCapital.keySet().iterator();
                capitalS = capital.next();
            }
            if (mapNot.size() > 0) {
                not = mapNot.keySet().iterator();
                notS = not.next();
            }
            while ((capital != null && capital.hasNext()) && (not != null && not.hasNext())) {
                String toLower = capitalS.toLowerCase();
                if (toLower.compareTo(notS) < 0) {
                    //capital first
                    writer.write(mapCapital.get(capitalS) + '\n');
                    capitalS = capital.next();

                } else {
                    writer.write(mapNot.get(notS) + '\n');
                    notS = not.next();
                }
            }
            while (not != null && not.hasNext()) {//need to write the small letters
                writer.write(mapNot.get(notS) + '\n');
                notS = not.next();
            }
            while (capital != null && capital.hasNext()) {//need to write the capitals
                writer.write(mapCapital.get(capitalS) + '\n');
                capitalS = capital.next();
            }
            mapCapital.clear();
            mapNot.clear();
            writer.flush();
            writer.close();


            //second half:
            mapCapital = new TreeMap<>();
            mapNot = new TreeMap<>();
            for (line = reader.readLine(); line != null&&i<size; line = reader.readLine()) {
                if (Character.isUpperCase(line.charAt(0))) {
                    mapCapital.put(getField(line), line);
                } else {
                    mapNot.put(getField(line), line);

                }
                i++;
            }


            writer = new BufferedWriter(new FileWriter(lasthalf, true));
            capital = null;
            not = null;
            capitalS = "";
            notS = "";
            if (mapCapital.size() > 0) {
                capital = mapCapital.keySet().iterator();
                capitalS = capital.next();
            }
            if (mapNot.size() > 0) {
                not = mapNot.keySet().iterator();
                notS = not.next();
            }
            while ((capital != null && capital.hasNext()) && (not != null && not.hasNext())) {
                String toLower = capitalS.toLowerCase();
                if (toLower.compareTo(notS) < 0) {
                    //capital first
                    writer.write(mapCapital.get(capitalS) + '\n');
                    capitalS = capital.next();

                } else {
                    writer.write(mapNot.get(notS) + '\n');
                    notS = not.next();
                }
            }
            while (not != null && not.hasNext()) {//need to write the small letters
                writer.write(mapNot.get(notS) + '\n');
                notS = not.next();
            }
            while (capital != null && capital.hasNext()) {//need to write the capitals
                writer.write(mapCapital.get(capitalS) + '\n');
                capitalS = capital.next();
            }
            mapCapital.clear();
            mapNot.clear();
            writer.flush();
            writer.close();
            //merge to 1 file /f1,/f2
            Merge(firsthalf,lasthalf,firsthalf+"_"+lasthalf);
            return firsthalf+"_"+lasthalf;
        }
        catch (Exception e){
        System.out.println("thats why");

    }
    return "";
    }
    private void Merge(String firstHalf,String lastHalf,String toWrite) {
        try {
            BufferedReader bufferedReader1 = new BufferedReader(new FileReader(firstHalf));
            BufferedReader bufferedReader2 = new BufferedReader(new FileReader(lastHalf));
            BufferedWriter bufferedWriter=new BufferedWriter(new FileWriter(toWrite));

            String file1=bufferedReader1.readLine();
            String file2=bufferedReader2.readLine();
            while(file1!=null&&file2!=null){
                if(file1.compareToIgnoreCase(file2)<0){
                    bufferedWriter.write(file1+'\n');
                    file1=bufferedReader1.readLine();
                }
                else{
                    bufferedWriter.write(file2+'\n');
                    file2=bufferedReader2.readLine();
                }
            }
            while(file1!=null){//need to write the small letters
                bufferedWriter.write(file1+'\n');
                file1=bufferedReader1.readLine();
            }
            while(file2!=null){//need to write the capitals
                bufferedWriter.write(file2+'\n');
                file2=bufferedReader2.readLine();
            }
            bufferedReader1.close();
            bufferedReader2.close();
            bufferedWriter.flush();
            bufferedWriter.close();
            File f1=new File(firstHalf);
            File f2=new File(lastHalf);
            f1.delete();
            f2.delete();
        }
        catch (Exception e){

        }
    }


    private static String getField(String line) {
        if(line.contains("-")) {
            String[] values = line.split("&");//valuse[0]-term, values[1]docLine-tf
            String[] line_tf = values[0].split("-");
            String returnedValue = values[0] + "&" + line_tf[0];//term&line
            return returnedValue;
        }
        return line;
    }



}


/**
 *
 */
class ThreadedUpdate extends Thread{
    String fileName;
    String newName;
    Map<String,Vector<Integer>> dictionary;
    Mutex dictionaryMutex;

    public ThreadedUpdate(String fileName,String newName,Map<String,Vector<Integer>> dictionary,Mutex mutex) {
        this.fileName=fileName;
        this.newName=newName;
        this.dictionary=dictionary;
        this.dictionaryMutex=mutex;
    }

    /**
     * this function updates pointer in dictionary to each word
     */
    public void run(){
        try {
            int lineNum=0;
            String prevWord="#";
            File newFile=new File(newName);
            newFile.createNewFile();
            RandomAccessFile writer=new RandomAccessFile(newFile,"rw");
            BufferedReader reader=new BufferedReader(new FileReader(fileName));
            String line=reader.readLine();
            while(line!=null){
                String[] strings=line.split("&");
                if(!prevWord.equalsIgnoreCase(strings[0])||!prevWord.equals(strings[0])){//the term wasn't touched
                    if(dictionary.containsKey(strings[0])||dictionary.containsKey(Indexer.Reverse(strings[0]))) {//the term in dictionary and new
                        prevWord = strings[0];
                        String term;
                        if(dictionary.containsKey(strings[0]))
                            term=strings[0];
                        else
                            term=Indexer.Reverse(strings[0]);
                        //update dictionary
                        if(term.equals("-01-11"))
                            System.out.println("g");
                        dictionaryMutex.lock();
                        Vector<Integer> details = dictionary.get(term);
                        details.remove(2);//the ptr
                        details.add(lineNum);
                        dictionary.remove(term);
                        dictionary.put(term, details);
                        dictionaryMutex.unlock();
                        //write to the new file in bytes
                        String[] postDetails = strings[1].split("-");
                        byte[] lineDoc = Indexer.toBytes(Integer.valueOf(postDetails[0]));
                        byte[] tf = Indexer.toBytes(Integer.valueOf(postDetails[1]));
                        writer.write(lineDoc);
                        writer.write(tf);
                    }
                }
                line=reader.readLine();
                lineNum+=1;
            }
            reader.close();
            writer.close();
            File f=new File(fileName);
            f.delete();//dont need old one
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}