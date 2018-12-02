
import ParseObjects.Number;
import javafx.util.Pair;
import sun.awt.Mutex;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class Indexer {

    private Map<String,Pair<Integer,Integer>> dictionary;


    //lineNum(4 bytes)|tf(4 Bytes)|pte next(4 Bytes) ==12 bytes

    private File documents;//docName(20 bytes)|city(18)|maxtf(4)|num of terms(4)|words(4)-50 bytes
    private int lineNumDocs;
    private int _numOfFiles;
    private String _path;
    private boolean _toStem;
    private Mutex [] mutexesPosting; //mutexs of the posting files.

    //citys
    private Map <String,Pair<Vector<String>,Integer>> cityDictionary;
    private File citysPosting;//docLine(4 B)|loc1|loc2|loc3|loc4|loc5|ptr nxt==28 bytes (4 each)
    private int lineNumCitys;


    //mutex
    private Mutex docMutex;
    private Mutex cityMutex;

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
            dictionary = new HashMap<>();
            lineNumDocs = 0;
            lineNumCitys = 0;
            _numOfFiles =0;

            //citys
            cityDictionary = new HashMap<>();
            citysPosting.createNewFile();

            //mutexesPostingFiles
            cityMutex=new Mutex();
            docMutex=new Mutex();
            mutexesPosting =new Mutex[27];
            for(int i=0; i<mutexesPosting.length;i++){
                mutexesPosting[i]=new Mutex();
            }
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
                      int numOfWords){
        Set<String> keys=terms.keySet();
        int maxtf=getMaxTf(terms.values());
        docMutex.lock();
        writeToDocuments(nameOfDoc,cityOfDoc,maxtf,terms.size(),numOfWords);
        lineNumDocs+=1;
        docMutex.unlock();
        Iterator<String>termsKeys=keys.iterator();




        while (termsKeys.hasNext()) {
            String term = termsKeys.next();
            int lineOfFirstDoc = addToDic(term);
            if(lineOfFirstDoc!=-1)
                updateTheLastNodePtr(lineOfFirstDoc,'x');//updates the term's ladt doc that new line will be added in lineNumPosting

            //todo
            writeToPosting(lineNumDocs-1,terms.get(term));
        }



        String details=getCityDetails(cityOfDoc);
        String[] strings=details.split("-");
        cityMutex.lock();
            toStatesDictionary(cityOfDoc, strings[0], strings[1], strings[2],locations.size());
        if(locations.size()>0) {
            toCityPosting(locations);
        }
        lineNumCitys+=1;
        _numOfFiles +=1;
        cityMutex.unlock();
        System.out.println(_numOfFiles);

    }


    public Map<String, Pair<Integer, Integer>> getDictionary() {
        return dictionary;
    }
    public Map<String, Pair<Vector<String>, Integer>> getCityDictionary() {
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
            lineNumDocs=0;
        }
        catch (Exception e){
            System.out.println("problem in indexer->delete");
        }
    }


    public int get_numOfFiles(){
        return _numOfFiles;
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
    private void toCityPosting(Vector<Integer> locations) {
        int size=locations.size();
        int index=0;
        byte[] toWrite=new byte[28];
        byte [] docline=toBytes(this.lineNumDocs-1);
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
            try {
                RandomAccessFile raf=new RandomAccessFile(citysPosting,"rw");
                raf.seek(raf.length());
                raf.write(toWrite);
                raf.close();
                this.lineNumCitys+=1;
            }
            catch (Exception e){
                System.out.println("problem in write to city posting");
            }
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
        try {
            RandomAccessFile raf=new RandomAccessFile(citysPosting,"rw");
            raf.seek(raf.length());
            raf.write(toWrite);
            raf.close();
        }
        catch (Exception e){
            System.out.println("problem in write to city posting");
        }


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
    private void toStatesDictionary(String cityOfDoc,String country,String coin,String population,int size) {
        if(!cityDictionary.containsKey(cityOfDoc)){
            Vector<String> v=new Vector<>();
            v.add(country);
            v.add(coin);
            v.add(population);
            if(size>0) {
                Pair<Vector<String>, Integer> pair = new Pair<>(v, new Integer(this.lineNumCitys));
                cityDictionary.put(cityOfDoc, pair);
            }
            else {
                Pair<Vector<String>, Integer> pair = new Pair<>(v, -1);
                cityDictionary.put(cityOfDoc, pair);
            }
        }
        else{//exist need to update the posting file's pointer
            if(size>0) {
                int lineInPosting = (cityDictionary.get(cityOfDoc)).getValue();
                updateStatesPosting(lineInPosting);
            }
        }


    }


    private void updateStatesPosting(int lineInPosting) {
        try {
            RandomAccessFile raf = new RandomAccessFile(citysPosting, "rw");
            raf.seek(28*lineInPosting+24);
            byte[] ptr = new byte[4];
            raf.read(ptr);
            int ptr_int=byteToInt(ptr);
            int prevptr=lineInPosting;//to know what line is the last of the term's docs
            while(ptr_int!=-1){
                prevptr=ptr_int;
                raf.seek(ptr_int*27+24);
                ptr = new byte[4];
                raf.read(ptr);
                ptr_int=byteToInt(ptr);
            }
            //the last line.need to change the pointer!
            raf.seek(prevptr*28);
            byte[] line = new byte[28];
            raf.read(line);
            raf.close();
            ptr=toBytes(this.lineNumCitys);
            line[46]=ptr[0];
            line[47]=ptr[1];
            line[48]=ptr[2];
            line[49]=ptr[3];
            raf=new RandomAccessFile(citysPosting,"rw");
            raf.seek(prevptr*28);
            raf.write(line);
            raf.close();


        }
        catch(Exception e){

        }
    }


    private void writeToPosting(int lineOfDoc,int tf) {
        byte [] tf_bytes=toBytes(tf);
        byte [] line_bytes=toBytes(lineOfDoc);
        byte [] ptr_bytes=toBytes(-1);

        byte [] toWrite=new byte[12];
        for (int i = 0; i < 4; i++) {
            toWrite[i]=line_bytes[i];
        }
        for (int i = 4; i <8 ; i++) {
            toWrite[i]=tf_bytes[i-4];
        }

        for (int i = 8; i < 12; i++) {
            toWrite[i]=ptr_bytes[i-8];
        }
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
    private void writeToDocuments(String nameOfDoc, String cityOfDoc, int maxtf, int size, int numOfWords) {
        //docName(20 bytes)|city(18)|maxtf(4)|num of terms(4)|words(4)-50 bytes
        byte[] name=stringToByteArray(nameOfDoc,20);
        byte[] city=stringToByteArray(cityOfDoc,18);
        byte [] maxtf_bytes=toBytes(maxtf);
        byte [] size_bytes=toBytes(size);
        byte [] words_bytes=toBytes(numOfWords);
        byte [] toWrite=new byte[50];
        for (int i = 0; i < 20; i++) {
            toWrite[i]=name[i];
        }
        for (int i = 20; i <38 ; i++) {
            toWrite[i]=city[i-20];
        }

        for (int i = 38; i < 42; i++) {
            toWrite[i]=maxtf_bytes[i-38];
        }
        for (int i = 42; i <46 ; i++) {
            toWrite[i]=size_bytes[i-42];
        }

        for (int i = 46; i < 50; i++) {
            toWrite[i]=words_bytes[i-46];
        }
        try {
            RandomAccessFile raf=new RandomAccessFile(documents,"rw");
            raf.seek(raf.length());
            raf.write(toWrite);
            raf.close();
        }
        catch (Exception e){
            System.out.println("problem in write to doc");
        }

    }

    /**
     *
     * @param lineOfFirstDoc - the index of line in posting file of the first in the linked list.
     * searches for last row of doc of the term, updates its pointer to the new row.
     * @return the row to add on.
     */
    private int updateTheLastNodePtr(int lineOfFirstDoc,char firstChar) {
        try {
            File f=new File(_path+"/"+Character.toLowerCase(firstChar)+_toStem);
            int index=Character.toLowerCase(firstChar) - 'a' + 1;
            if(index==-48)//^^
                index=0;
            mutexesPosting[index].lock();
            RandomAccessFile raf = new RandomAccessFile(f, "r");
            raf.seek(lineOfFirstDoc*12+8);
            byte[] ptr = new byte[4];
            raf.read(ptr);
            int ptr_int=byteToInt(ptr);
            int prevptr=lineOfFirstDoc;//to know what line is the last of the term's docs
            while(ptr_int!=-1){
                prevptr=ptr_int;
                raf.seek(ptr_int*12+8);
                ptr = new byte[4];
                raf.read(ptr);
                ptr_int=byteToInt(ptr);
            }
            //the last line.need to change the pointer!
            raf.seek(prevptr*12);
            byte[] line = new byte[12];
            long lineToAdd=raf.length()/12;
            raf.read(line);
            raf.close();
            mutexesPosting[index].unlock();
            ptr=toBytes((int)lineToAdd);
            line[8]=ptr[0];
            line[9]=ptr[1];
            line[10]=ptr[2];
            line[11]=ptr[3];
            //updates the line
            mutexesPosting[index].lock();
            raf=new RandomAccessFile(f,"rw");
            raf.seek(prevptr*12);
            raf.write(line);
            raf.close();
            mutexesPosting[index].unlock();
            return (int)lineToAdd;
        }
        catch (Exception e){
            System.out.println("problem in updateTheLastNodePtr !");
        }
            return -1;
    }

    /**
     *
     * @param term - add to dictionary or update
     * @return the number of the first line in the posting document if exist, else:  -1.
     */
    private int addToDic(String term) {
        if(dictionary.containsKey(term)){//just add to df and return the line in posting
           int df= dictionary.get(term).getKey();
           int pointerToPost= dictionary.get(term).getValue(); //pointer to posting file.
           df+=1;
           dictionary.remove(term);
           dictionary.put(term,new Pair<Integer, Integer>(df,pointerToPost));
           return pointerToPost;
        }
        //check if exist in the dictionary in diffrent way.
        else if(dictionary.containsKey(Reverse(term))){//the reversed term in dictionary need to put the uppercase one //todo - fix.
            String newTerm;
            if(Character.isLowerCase(Reverse(term).charAt(0))){//the term in dictionary is the uppercase one
                newTerm=Reverse(term);
            }
            else{//the new term is the upper case one!
                newTerm=term;
            }
            //take the details of its if exist.
            int df= dictionary.get(Reverse(term)).getKey();
            int pointer= dictionary.get(Reverse(term)).getValue();
            df+=1;
            dictionary.remove(Reverse(term));
            //todo newTerm = changeCase(newTerm); //todo
            dictionary.put(newTerm,new Pair<Integer, Integer>(df,pointer));
            return pointer;
        }
        else {//new term completely
            dictionary.put(term,new Pair<Integer, Integer>(1,-1));
            return -1;
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



}
