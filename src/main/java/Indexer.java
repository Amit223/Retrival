
import ParseObjects.Number;
import javafx.util.Pair;
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

public class Indexer {

    private Map<String,Integer> dictionary;




    //docs
    private File documents;//docName(16 bytes)|city(16)|language(10)|maxtf(4)|num of terms(4)|words(4)|-54 bytes
    private List<Byte> docsToWrite;
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
    private File citysPosting;//docLine(4 B)|loc1|loc2|loc3|loc4|loc5|ptr nxt==28 bytes (4 each)
    private int lineNumCitys;
    private List<String> cityLines = new ArrayList<String>();



    //postingMutex
    private Mutex docMutex;
    private Mutex cityMutex;
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
     * @param toStem to stem - to decide the path of the file.
     * @param path -the path of the directory of the posting files.
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
            dictionary = new ConcurrentHashMap<>();
            _AtomicNumlineDocs = new AtomicInteger(0);
            lineNumCitys = 0;
            _numOfFiles=new AtomicInteger(0);
            docsToWrite=new ArrayList<>();
            languages=new HashSet<>();

            //citys
            cityDictionary = new HashMap<>();
            citysPosting.createNewFile();
            cityLines=new ArrayList<>();

            //mutexes&&posting lines
            cityMutex=new Mutex();
            docMutex=new Mutex();
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
     * @param terms
     * @param locations - vector of locations of the city
     * @param nameOfDoc
     * @param cityOfDoc
     * @param numOfWords - the number of word in the document
     */
    public void Index(Map<String,Integer> terms,Vector<Integer> locations,String nameOfDoc,String cityOfDoc,
                      int numOfWords,String language){


        Set<String> keys=terms.keySet();
         int maxtf=getMaxTf(terms.values());
         docMutex.lock();
         writeToDocuments(nameOfDoc,cityOfDoc,maxtf,terms.size(),numOfWords,language); //
         docMutex.unlock();
        _AtomicNumlineDocs.getAndAdd(1);
        int lineNumDocs = _AtomicNumlineDocs.get()-1;


        Iterator<String>termsKeys=keys.iterator();
        while (termsKeys.hasNext()) {
            String term = termsKeys.next();
            dictionaryMutex.lock();
            ifExistUpdateTF(term); //updates dictionary df/ add new term
            dictionaryMutex.unlock();
            char FirstC = (Character.isDigit(term.charAt(0))||term.charAt(0)=='-') ? '0' : Character.toLowerCase(term.charAt(0));
            writeToPosting(lineNumDocs,terms.get(term),FirstC,postingLines,term);
        }
        if(_numOfFiles.get()%1000==0)
            writeListToPosting();


        if(cityOfDoc.length()!=0) {
             String details = getCityDetails(cityOfDoc);
             String[] strings = details.split("-");
             cityMutex.lock();
             toStatesDictionary(cityOfDoc, strings[0], strings[1], strings[2]);
             if (locations.size() > 0 ) {//need only if in file to add
                 toCityPosting(cityOfDoc,locations, lineNumDocs);
                 lineNumCitys += 1;
                }
            cityMutex.unlock();
        }

        _numOfFiles.getAndAdd(1);
        // System.out.println(_numOfFiles);

    }

    /**
     * todo add!
     */
    public boolean loadDictionaryToMemory() {
        String dicString = "";
        FileReader f = null;
        try {
            f = new FileReader(_path+"\\"+_toStem+"Dictionary.txt");
        } catch (FileNotFoundException e) {
            return false;
        }
        Scanner sc = new Scanner(f);
        int i = 0, startIndex = 1, //cut the '{'
                endindex = 1;
        sc.useDelimiter(", ");
        while (sc.hasNext()) {
            String line = sc.next();
            endindex = line.length(); //cut '/n' and ',' and ' '
            if (i != 0) startIndex = 0;
            if (!sc.hasNext()) endindex = line.length() - 1; //cut the '}'
            line =line.substring(startIndex, endindex);
            String[] pair = line.split("=");
            dictionary.put(pair[0], Integer.parseInt(pair[1]));
            i++;
        }
        return true;
    }


    /**
     * todo add!
     */
    public void loadDictionaryToFile(){
        String dictionaryToString="";
        dictionaryToString = dictionary.toString();
        BufferedWriter writer = null;
        try
        {
            writer = new BufferedWriter( new FileWriter( _path+"\\"+_toStem+"Dictionary.txt"));
            writer.write(dictionaryToString);

        }
        catch ( IOException e)
        {
        }
        finally
        {
            try
            {
                if ( writer != null)
                    writer.close( );
                numOfTerms=dictionary.size();
                dictionary.clear();
            }
            catch ( IOException e)
            {
            }
        }
    }

    private void writeListToPosting() {
        Thread [] threads=new ThreadedWrite[27];
        ExecutorService pool= Executors.newFixedThreadPool(27);

        int i=1;
        File f=new File(_path + "/" + '0' + _toStem+".txt");
        threads[0]=new ThreadedWrite(f,postingLines[0],mutexesPosting[0],mutexesList[0]);
        pool.submit(threads[0]);
        for(char c='a';c<'z';c++){
            f=new File(_path + "/" + c + _toStem+".txt");
            threads[i]=new ThreadedWrite(f,postingLines[i],mutexesPosting[i],mutexesList[i]);
            pool.execute(threads[i]);
            i++;
        }
        pool.shutdown();

        try {
            boolean flag = false;
            while (!flag)
                flag = pool.awaitTermination(1009, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void push(){
        Byte[] Bytes = cityLines.toArray(new Byte[cityLines.size()]);
        byte[] bytes=new byte[cityLines.size()];
        for (int i=0;i<Bytes.length;i++){
            bytes[i]=Bytes[i];
        }
        try {
            FileOutputStream out = new FileOutputStream(citysPosting);
            RandomAccessFile raf=new RandomAccessFile(documents,"rw");
            byte[] bytesDocs=new byte[docsToWrite.size()];
            for(int i=0;i<docsToWrite.size();i++){
                bytesDocs[i]=docsToWrite.get(i);
            }
            raf.write(bytesDocs);
            raf.close();
            docsToWrite.clear();
            out.write(bytes);
            out.close();
        }
        catch (Exception e){

        }
        writeListToPosting();


    }

    public Map<String,Integer> getDictionary() {
        return dictionary;
    }
    public Map<String, Vector<String>> getCityDictionary() {
        return cityDictionary;
    }

    /**
     * delete the files text
     */
    public void delete(){
        try {

            PrintWriter writer = new PrintWriter(documents);
            writer.print("");
            writer.close();
            writer=new PrintWriter(citysPosting);
            writer.print("");
            writer.close();
            lineNumCitys=0;
            _AtomicNumlineDocs.set(0); ;
        }
        catch (Exception e){
            System.out.println("problem in indexer->delete");
        }
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
    //todo
    private void toCityPosting(String city,Vector<Integer> locations,int lineNumDoc) {

        StringBuilder toWrite=new StringBuilder();
        int index=locations.size();
        while(index>3){
            toWrite.append(city);
            toWrite.append(" ");
            toWrite.append(lineNumDoc);
            toWrite.append(" ");

        }
        for(int i=0;i<3;i++){
            if(i<locations.size()){
                toWrite.append(locations.get(i));
            }
            else
                toWrite.append("#");
        }

        toWrite.append("\n");
        cityMutex.lock();
        cityLines.add(toWrite.toString());
        mutexesList[index].unlock();



        /**
         *
        int size=locations.size();
        int index=0;
        Byte[] toWrite=new Byte[28];
        byte [] docline=toBytes(lineNumDoc);
        byte []loc1;
        byte []loc2;
        byte []loc3;
        byte []loc4;
        byte []loc5;
        byte[]ptr;
        while(size>5){
            loc1=toBytes(locations.get(index*5));
            loc2=toBytes(locations.get(index*5+1));
            loc3=toBytes(locations.get(index*5+2));
            loc4=toBytes(locations.get(index*5+3));
            loc5=toBytes(locations.get(index*5+4));
            ptr=toBytes(this.lineNumCitys);
            for (int i = 0; i < 4; i++) {
                toWrite[i]=docline[i];
            }
            for (int i = 4; i <8 ; i++) {
                toWrite[i]=loc1[i-4];
            }

            for (int i = 8; i < 12; i++) {
                toWrite[i]=loc2[i-8];
            }
            for (int i = 12; i <16 ; i++) {
                toWrite[i]=loc3[i-12];
            }

            for (int i = 16; i < 20; i++) {
                toWrite[i]=loc4[i-16];
            }
            for (int i = 20; i <24 ; i++) {
                toWrite[i]=loc5[i-20];
            }

            for (int i = 24; i < 28; i++) {
                toWrite[i]=ptr[i-24];
            }
            cityLines.addAll(Arrays.asList(toWrite));
            index+=1;
            size=size-5;
        }
        loc1=toBytes(-1);
        loc2=toBytes(-1);
        loc3=toBytes(-1);
        loc4=toBytes(-1);
        loc5=toBytes(-1);
        ptr=toBytes(-1);
        if(size>0)
            loc1=toBytes(locations.get(index*5));
        if(size>1)
            loc2=toBytes(locations.get(index*5+1));
        if(size>2)
            loc3=toBytes(locations.get(index*5+2));
        if(size>3)
            loc4=toBytes(locations.get(index*5+3));
        if(size>4)
            loc5=toBytes(locations.get(index*5+4));
        for (int i = 0; i < 4; i++) {
            toWrite[i]=docline[i];
        }
        for (int i = 4; i <8 ; i++) {
            toWrite[i]=loc1[i-4];
        }

        for (int i = 8; i < 12; i++) {
            toWrite[i]=loc2[i-8];
        }
        for (int i = 12; i <16 ; i++) {
            toWrite[i]=loc3[i-12];
        }

        for (int i = 16; i < 20; i++) {
            toWrite[i]=loc4[i-16];
        }
        for (int i = 20; i <24 ; i++) {
            toWrite[i]=loc5[i-20];
        }

        for (int i = 24; i < 28; i++) {
            toWrite[i]=ptr[i-24];
        }
        cityLines.addAll(Arrays.asList(toWrite));
**/


    }


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
            String country=string.substring(string.indexOf("\"geobytescountry\":")+19,string.indexOf("geobytesregionlocation")-3);
            String population=string.substring(string.indexOf("\"geobytespopulation\":")+22,string.indexOf("geobytesnationalityplural")-3);
            population=Number.Parse(population);
            if(population.contains(".")) {
                char mod=population.charAt(population.length() - 1);
                double num=Double.parseDouble(population.substring(0,population.length()-1));
                num= Math.round(num * 100.0) / 100.0;
                population=Double.toString(num)+mod;
            }
            return country+"-"+currency+"-"+population;

        } catch (Exception e) {
            return "";
        }
    }
    private void toStatesDictionary(String cityOfDoc,String country,String coin,String population) {
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
        //docName(20 bytes)|city(18)|maxtf(4)|num of terms(4)|words(4)-50 bytes
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
     * @param lineOfFirstDoc - the index of line in posting file of the first in the linked list.
     * searches for last row of doc of the term, updates its pointer to the new row.
     * @return the row to add on.
     */
    private int updateTheLastNodePtr(int lineOfFirstDoc,char firstChar,ListOfByteArrays[] postingLines) {
        try {
            File f=new File(_path+"/"+Character.toLowerCase(firstChar)+_toStem+".txt");
            int index=Character.toLowerCase(firstChar) - 'a' + 1;
            if(index<0||index>26)//^^
                index=0;
            mutexesPosting[index].lock();
            RandomAccessFile raf = new RandomAccessFile(f, "r");
            raf.seek(lineOfFirstDoc*12);
            byte[] ptr = new byte[12];
            raf.read(ptr);
            byte [] pointer={ptr[8],ptr[9],ptr[10],ptr[11]};
            int ptr_int=byteToInt(pointer);
            int prevptr=lineOfFirstDoc;//to know what line is the last of the term's docs
            while(ptr_int!=-1){
                prevptr=ptr_int;
                raf.seek(ptr_int*12);
                ptr = new byte[12];
                raf.read(ptr);
                pointer=new byte [4];
                pointer[0]=ptr[8];
                pointer[1]=ptr[9];
                pointer[2]=ptr[10];
                pointer[3]=ptr[11];
                ptr_int=byteToInt(pointer);
            }
            //the last line.need to change the pointer!
            raf.seek(prevptr*12);
            byte[] line = new byte[12];
            long lineToAdd=raf.length()/12;
            raf.read(line);
            raf.close();
            ptr=toBytes((int)lineToAdd+postingLines[index].size());
            line[8]=ptr[0];
            line[9]=ptr[1];
            line[10]=ptr[2];
            line[11]=ptr[3];
            //updates the line
            raf=new RandomAccessFile(f,"rw");
            raf.seek(prevptr*12);
            raf.write(line);
            raf.close();
            mutexesPosting[index].unlock();
            return (int)lineToAdd+postingLines[index].size();
        }
        catch (Exception e){
            System.out.println("problem in updateTheLastNodePtr !");
        }
        return -1;
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
        else if(dictionary.containsKey(Reverse(term))){//the reversed term in dictionary need to put the uppercase one //todo - fix.
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

        Thread[] threads = new ThreadedSort[27];
        ExecutorService pool = Executors.newFixedThreadPool(3);

        int i = 1;

        threads[0] = new ThreadedSort(_path + "/" + '0' + _toStem + ".txt");
        pool.submit(threads[0]);
        for (char c = 'a'; c < 'z'; c++) {
            threads[i] = new ThreadedSort(_path + "/" + c + _toStem + ".txt");
            pool.execute(threads[i]);
            i++;
        }
        pool.shutdown();

        try {
            boolean flag = false;
            while (!flag)
                flag = pool.awaitTermination(1009, TimeUnit.MILLISECONDS);
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
        try {

            System.out.println("here");
            BufferedReader reader = new BufferedReader(new FileReader(new File(file)));
            Map<String, String > mapCapital = new TreeMap<String,String>();
            Map<String, String > mapNot = new TreeMap<String,String>();
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
            reader.close();


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
            writer.close();

            /**
             *  for(int i=0;i<lineNum;i++){
             byte[] line=new byte[24];
             reader.seek(i*24);
             reader.read(line);
             byte[]term=new byte[16];
             byte[]docLine=new byte[4];
             for (int j=0;j<16;j++)
             term[j]=line[j];
             for (int j =16; j <20 ; j++) {
             docLine[j-16]=line[j];
             }
             // BufferedWriter bufferedWriter=new BufferedWriter(new FileWriter(file.getAbsolutePath(),true));
             //  bufferedWriter.write(stringBuilder.toString());
             String termInString=new String(term);
             termInString=termInString.substring(0,termInString.indexOf("#"));
             String lineInString=new String(docLine);
             map.put(termInString+lineInString, line);

             }
             */

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private static String getField(String line) {
        return line.split("-")[0];//extract value you want to sort on
    }



}
