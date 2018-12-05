
import ParseObjects.Number;
import sun.awt.Mutex;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * this class index all documents in corpus to 28 files: 'a'-'z' , '0' (to digit and signs) and to city posting the city details.
 */
public class Indexer {

    private Map<String,Integer> dictionary;




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


    //citys
    private Map <String,Vector<String>> cityDictionary;
    private File citysPosting;//docLine(4 B)|loc1|loc2|loc3|
    private ListOfByteArrays cityLines;



    //postingMutex
    private Mutex docMutex;
    private Mutex docFileMutex;
    private Mutex cityMutex;
    private Mutex cityFileMutex;
    private Mutex dictionaryMutex;
    private Mutex [] mutexesPosting; //mutexs of the posting files.
    private Mutex [] mutexesList; //mutexs of the posting files.



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
            docMutex=new Mutex();
            docFileMutex=new Mutex();
            dictionaryMutex=new Mutex();
            mutexesPosting =new Mutex[27];
            mutexesList=new Mutex[27];
            postingLines = new ListOfByteArrays[27];
            for(int i=0; i<mutexesPosting.length;i++){
                if(i==26){
                    System.out.println("");
                }
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
     * the language of the doc.
     * 1)writes to the list of documents details in bytes:docName(16 bytes)|city(16)|language(10)|maxtf(4)|num of terms(4)|words(4)|-54 bytes
     * 2)for each term in dictionary: writes to the dictionary if new term, add to doc frequency if exist. Next write the
     * term details: the line in the doc file that represent the document it was found in, it's term frequency and itself so we can
     * use it on stage 5
     * It writes each line to the list of the term first letter-'a'-'z' or if digit or sign writes to the list of '0'
     * 3)for the city of doc find it's details on geobytes, write to the dictionary of citys all the details:the city,
     * the population and the coin if new.
     * next writes to the list of citys - the city, 3 locations and line of document int the doc file. (if there is more than 3,
     *                   it will make another line. it will be used later when we write the details in bytes and then can
     *                   access directly to the line(fixed size)
     *4)every few docs, it will write the lists to the file and erase it,
     *5) when we done indexing from outside we will sort each file.
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
        writeToDocuments(nameOfDoc,cityOfDoc,maxtf,terms.size(),numOfWords,language); //
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
            ifExistUpdateTF(term); //updates dictionary df/ add new term
            dictionaryMutex.unlock();
            char FirstC = (Character.isDigit(term.charAt(0))||term.charAt(0)=='-') ? '0' : Character.toLowerCase(term.charAt(0));
            writeToPosting(lineNumDocs,terms.get(term),FirstC,postingLines,term);
        }
        terms.clear();
        if(_numOfFiles.get()%1000==0)
            writeListToPosting();



        //citys
        if(cityOfDoc.length()!=0) {
            if(!cityDictionary.containsKey(cityOfDoc)) {
                String details = getCityDetails(cityOfDoc);
                String[] strings = details.split("-");
                toCitysDictionary(cityOfDoc, strings[0], strings[1], strings[2]);
            }
            if (locations.size() > 0 ) {//need only if in file to add
                toCityPosting(cityOfDoc,locations, lineNumDocs);
            }
        }
        locations.clear();
        if(_numOfFiles.get()%5000==0){
            writeCityList();
        }

        _numOfFiles.getAndAdd(1);

    }

    /**
     * call {@link ThreadedWrite} to write the list of files to end of documents, erase the list.
     */
    private void writeDocsToFile() {
        Thread t=new ThreadedWrite(documents,docsToWrite,docMutex,docFileMutex);
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
        Thread t=new ThreadedWrite(citysPosting,cityLines,cityMutex,cityFileMutex);
        t.start();
        try {
            t.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
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
                    if(pair.length==2)
                    dictionary.put(pair[0], Integer.parseInt(pair[1]));

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
     * @return dictionary- can be null if loaded to disk
     */
    public Map<String, Integer> getDictionary() {
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
     * load the dictionary to the file and deletes it
     */
    public void loadDictionaryToFile(){
        StringBuilder stringBuilder=new StringBuilder();
        Iterator<String> iterator=dictionary.keySet().iterator();
        while (iterator.hasNext()){
            String key=iterator.next();
            stringBuilder.append(key+"--->"+dictionary.get(key)+"=");
        }
        BufferedWriter writer = null;
        try
        {
            writer = new BufferedWriter( new FileWriter( _path+"/"+_toStem+"Dictionary.txt"));
            writer.write(stringBuilder.toString());
            writer.flush();
            writer.close();
            numOfTerms=dictionary.size();
            dictionary.clear();
            dictionary=null;



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


    public void loadCityDictionaryToFile(){
        StringBuilder stringBuilder=new StringBuilder();
        Iterator<String> iterator=cityDictionary.keySet().iterator();
        while (iterator.hasNext()){
            String key=iterator.next();
            String value=cityDictionary.get(key).toString();
            stringBuilder.append(key+"--->"+value+"=");
        }
        BufferedWriter writer = null;
        try
        {
            writer = new BufferedWriter( new FileWriter( _path+"/"+"CityDictionary.txt"));
            writer.write(stringBuilder.toString());
            writer.flush();
            writer.close();
            cityDictionary.clear();
            cityDictionary=null;



        }
        catch ( IOException e)
        {
        }


    }



    private void writeListToPosting() {
        Thread [] threads=new ThreadedWrite[27];
        ExecutorService pool= Executors.newFixedThreadPool(27);

        int i=1;
        File f=new File(_path + "/" + '0' + _toStem+".txt");
        threads[0]=new ThreadedWrite(f,postingLines[0],mutexesPosting[0],mutexesList[0]);
        pool.execute(threads[0]);
        for(char c='a';c<='z';c++){
            f=new File(_path + "/" + c + _toStem+".txt");
            threads[i]=new ThreadedWrite(f,postingLines[i],mutexesPosting[i],mutexesList[i]);
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

    public void push(){
        writeListToPosting();
        writeCityList();
        writeDocsToFile();


    }


    public Set<String> getLanguages(){
        return languages;
    }
    /**
     * delete the files text
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
            //the dictionary
            f=new File(_path+"/"+_toStem+"Dictionary.txt");
            f.delete();

            if(dictionary!=null) {
                dictionary.clear();
                dictionary=null;
            }
            cityDictionary.clear();
            languages.clear();

            return true;
        }
        catch (Exception e){
            System.out.println("problem in indexer->delete");
        }
        return false;
    }


    public int get_numOfFiles(){
        return _numOfFiles.get();
    }
    /**
     *
     * @param lineInPosting
     * updates the pointer of last doc of the city
     */


    /**
     *
     * @param locations
     *
     * adds all vector of location to the state posting
     */
    private void toCityPosting(String city,Vector<Integer> locations,int lineNumDoc) {

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
     * @param city
     * @return
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
            return country+"-"+currency+"-"+population;

        } catch (Exception e) {
            return "X-X-X";
        }
    }

    /**
     *
     * @param cityOfDoc
     * @param country
     * @param coin
     * @param population
     * add to dictionary of citys
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
     * @param lineOfDoc
     * @param tf
     * @param firstChar
     * @return the line number that wrote to.
     */
    private void writeToPosting(int lineOfDoc,int tf, char firstChar,ListOfByteArrays [] postingLines,String term ) {
/**
 byte [] tf_bytes=toBytes(tf);
 byte [] line_bytes=toBytes(lineOfDoc);

 byte [] toWrite=new byte[8];
 for (int i = 0; i < 4; i++) {
 toWrite[i]=line_bytes[i];
 }
 for (int i = 4; i <8 ; i++) {
 toWrite[i]=tf_bytes[i-4];
 }

 **/

        StringBuilder toWrite=new StringBuilder();
        toWrite.append(term);
        toWrite.append(" ");
        toWrite.append(lineOfDoc);
        toWrite.append("-");
        toWrite.append(tf);
        toWrite.append("\n");
        int index = Character.toLowerCase(firstChar) - 'a' + 1;
        if (index <0||index>26)//^^
            index = 0;
        mutexesList[index].lock();
        postingLines[index].add(toWrite.toString());
        mutexesList[index].unlock();

    }

    /**
     *
     * @param nameOfDoc
     * @param cityOfDoc
     * @param maxtf
     * @param size
     * @param numOfWords
     *
     * writes to the docs file the document details-
     * docName(20 bytes)|city(18)|maxtf(4)|num of terms(4)|words(4)-50 bytes

     */
    private void writeToDocuments(String nameOfDoc, String cityOfDoc, int maxtf, int size, int numOfWords,String language) {
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
     * @param term - update tf if exist- the df-++
     * @return the number of the first line of term in the posting document if exist, else:  -1.
     */
    private void ifExistUpdateTF(String term) {
        if(dictionary.containsKey(term)){//just add to df and return the line in posting
            int df= dictionary.get(term);
            df+=1;
            dictionary.remove(term);
            dictionary.put(term,df);
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
            int df= dictionary.get(Reverse(term));
            df+=1;
            dictionary.remove(Reverse(term));
            dictionary.put(newTerm,df);
        }
        else {//new term completely
            dictionary.put(term,1);

        }
    }

    private String Reverse(String term) {
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


    private int byteToInt(byte[] bytes) {
        int val = 0;
        for (int i = 0; i < 4; i++) {
            val=val<<8;
            val=val|(bytes[i] & 0xFF);
        }
        return val;
    }

    private byte[] toBytes(int i)
    {
        byte[] result = new byte[4];

        result[0] = (byte) (i >> 24);
        result[1] = (byte) (i >> 16);
        result[2] = (byte) (i >> 8);
        result[3] = (byte) (i /*>> 0*/);

        return result;
    }


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
     * @return converts string into byte array of size 30
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
     * SORT EACH FILE
     */
    public void sort() {
        long startTime = System.nanoTime();

        Thread[] threads = new ThreadedSort[28];
        ExecutorService pool = Executors.newFixedThreadPool(1);

        int i = 1;

        threads[0] = new ThreadedSort(_path + "/" + '0' + _toStem + ".txt");
        pool.execute(threads[0]);
        for (char c = 'a'; c <= 'z'; c++) {
            threads[i] = new ThreadedSort(_path + "/" + c + _toStem + ".txt");
            i++;
        }
        //the city
        threads[27]=new ThreadedSort(_path+"/CityPosting.txt");
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
        long endTime = System.nanoTime();
        System.out.println("Took "+(endTime - startTime)/1000000000 + " s");
    }




}

class ThreadedWrite extends Thread{

    File file;
    ListOfByteArrays list;
    Mutex postingMutex;
    Mutex listMutex;
    public ThreadedWrite(File file,ListOfByteArrays list,Mutex postingMutex,Mutex listMutex) {
        this.file=file;
        this.list=list;
        this.postingMutex =postingMutex;
        this.listMutex=listMutex;
    }

    public void run(){
        try {
            listMutex.lock();
            postingMutex.lock();

            StringBuilder stringBuilder=new StringBuilder();
            for(int i=0;i<list.size();i++){

                stringBuilder.append(list.get(i));
            }
            BufferedWriter bufferedWriter=new BufferedWriter(new FileWriter(file.getAbsolutePath(),true));
            bufferedWriter.write(stringBuilder.toString());
            bufferedWriter.flush();
            bufferedWriter.close();
            postingMutex.unlock();
            list.delete();
            listMutex.unlock();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
class ThreadedSort extends Thread{

    String file;


    public ThreadedSort(String file) {
        this.file = file;
    }

    public void run() {
        long minRunningMemory = (1024*1024);

        Runtime runtime = Runtime.getRuntime();

        if(runtime.freeMemory()<minRunningMemory)
            System.gc();
        try {

            System.out.println("here");
            BufferedReader reader = new BufferedReader(new FileReader(new File(file)));
            Map<String, String > mapCapital = new TreeMap<>();
            Map<String, String > mapNot = new TreeMap<>();
            String line;
            while ((line=reader.readLine())!=null) {
                if (Character.isUpperCase(line.charAt(0))) {
                    mapCapital.put(getField(line), line);
                } else {
                    mapNot.put(getField(line), line);

                }
            }
            //delete the file completely
            reader.close();
            PrintWriter pwriter = new PrintWriter(new File(file));
            pwriter.print("");
            pwriter.close();

            BufferedWriter writer = new BufferedWriter(new FileWriter(file,true));
            Iterator<String> capital=null;
            Iterator<String> not=null;
            String capitalS="";
            String notS="";
            if(mapCapital.size()>0) {
                capital = mapCapital.keySet().iterator();
                capitalS=capital.next();
            }
            if(mapNot.size()>0) {
                not = mapNot.keySet().iterator();
                notS=not.next();
            }
            while((capital!=null&&capital.hasNext()) && (not!=null&&not.hasNext())){
                String toLower=capitalS.toLowerCase();
                if(toLower.compareTo(notS)<0){
                    //capital first
                    writer.write(mapCapital.get(capitalS)+'\n');
                    capitalS=capital.next();

                }
                else{
                    writer.write(mapNot.get(notS)+'\n');
                    notS=not.next();
                }
            }
            while(not!=null &&not.hasNext()){//need to write the small letters
                writer.write(mapNot.get(notS)+'\n');
                notS=not.next();
            }
            while(capital!=null &&capital.hasNext()){//need to write the capitals
                writer.write(mapCapital.get(capitalS)+'\n');
                capitalS=capital.next();
            }
            mapCapital.clear();
            mapNot.clear();
            writer.flush();
            writer.close();


        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private static String getField(String line) {
        return line.split("-")[0];//extract value you want to sort on
    }



}
