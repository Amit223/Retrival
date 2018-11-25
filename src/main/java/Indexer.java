import java.io.*;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Dictionary;
import java.util.Enumeration;

import static com.oracle.jrockit.jfr.ContentType.Bytes;

public class Indexer {

    File dictionary;
    int lineNumDic;
    File posting;
    int lineNumPosting;
    File statesDictioary;
    File statesPosting;

    public Indexer() {
        try {
            dictionary = new File("Dictionary.txt");//term(32 bytes),idf(4 bytes),line in posting(4 bytes)-40 bytes per line
            dictionary.createNewFile();
            lineNumDic =0;
            posting = new File("Posting.txt");//docName|maxtf|tf|num of terms|city
            posting.createNewFile();
            lineNumPosting=0;
            statesDictioary = new File("StatesDictionary.txt");
            statesDictioary.createNewFile();
            statesPosting = new File("StatesPosting.txt");
            statesPosting.createNewFile();
        }
        catch (IOException e){

        }
    }

    public void Index(Dictionary<String,Integer> terms,String nameOfDoc,String countryOfDoc){
        Enumeration<String> keys=terms.keys();
        while (keys.hasMoreElements()) {
            String term = keys.nextElement();
            int lineInPosting = toDictionaryFile(term);
        }
    }

    private int toDictionaryFile(String term) {

            int []lineInDic= getLineInDic(term);
            try {
                if (lineInDic[0] == 1) {//is in dictionary
                    RandomAccessFile randomAccessFile = new RandomAccessFile(dictionary, "rw");
                    randomAccessFile.seek(lineInDic[1] * 40);
                    byte[] termInBytes = new byte[32];
                    randomAccessFile.seek(lineInDic[1] * 40 + 32);
                    byte[] idfInBytes = new byte[4];
                    randomAccessFile.read(idfInBytes);
                    randomAccessFile.seek(lineInDic[1] * 40 + 32 + 4);
                    byte[] pointerInBytes = new byte[4];
                    randomAccessFile.read(pointerInBytes);
                    //byte[]->string
                    String termFromDic = new String(termInBytes, StandardCharsets.UTF_8);
                    //get the int value of ptr,idf
                    int idf_int = byteToInt(idfInBytes);
                    int ptr_int = byteToInt(pointerInBytes);
                    idf_int += 1;//another doc

                    String newTerm = getRealTerm(term, termFromDic);
                    randomAccessFile.close();
                    AddToDic(newTerm, idf_int, ptr_int, lineInDic[1], 1);
                    this.lineNumDic = this.lineNumDic += 1;
                    return ptr_int;

                } else//new term!
                {
                    int idf_int = 1;
                    int ptr_int = this.lineNumPosting;
                    AddToDic(term, idf_int, ptr_int, lineInDic[1], 0);
                    this.lineNumDic += 1;
                    return ptr_int;
                }
            }
            catch (Exception e){
                System.out.println("problem in indexer->toDictionaryFile");
            }

        return -1;

    }

    /**
     *
     * @param newTerm
     * @param idf_int
     * @param ptr_int
     * add to dictionary in existing line
     */
    private void AddToDic(String newTerm, int idf_int, int ptr_int,int line,int exist) {
       try {
           if(exist==1) {
               RandomAccessFile raf = new RandomAccessFile(dictionary, "rw");
               byte[] lineOfData = addToArray(stringToByteArray(newTerm), idf_int, ptr_int);
               raf.seek(line * 40);
               raf.write(lineOfData);
               raf.close();
           }
           else//exist =0
           {
               RandomAccessFile raf = new RandomAccessFile(dictionary, "rw");
               byte[] lineOfData = addToArray(stringToByteArray(newTerm), idf_int, ptr_int);
               raf.seek((line+1)*40);
               byte[] linesAfter=new byte[40*(lineNumDic-line)];
               raf.read(linesAfter);
               raf.seek((line+1)*40);
               raf.write(lineOfData);
               raf.seek((line+2)*40);
               raf.write(linesAfter);
               raf.close();

           }
       }
       catch (Exception e){
       }

    }


    /**
     *
     * @param term
     * @param termFromDic
     * @return the term in upper letter if there is
     */
    private String getRealTerm(String term, String termFromDic) {
        if(Character.isUpperCase(term.charAt(0)))
            return term;
        return termFromDic;
    }

    public void test(){
        try (RandomAccessFile raf=new RandomAccessFile(this.dictionary,"rw")) {
            byte[] bytes=stringToByteArray("aa");
            byte [] stuff={1,2,3,4,5,6,7,8};
            byte[] both = Arrays.copyOf(bytes, 40);
            System.arraycopy(stuff, 0, both, bytes.length, stuff.length);
            raf.write(both);
            bytes=stringToByteArray("cc");
            both = Arrays.copyOf(bytes, 40);
            System.arraycopy(stuff, 0, both, bytes.length, stuff.length);
            raf.write(both);
            bytes=stringToByteArray("dd");
            both = Arrays.copyOf(bytes, 40);
            System.arraycopy(stuff, 0, both, bytes.length, stuff.length);
            raf.write(both);
            bytes=stringToByteArray("ff");
            both = Arrays.copyOf(bytes, 40);
            System.arraycopy(stuff, 0, both, bytes.length, stuff.length);
            raf.write(both);
            this.lineNumDic=4;
            raf.close();


        }
        catch (Exception e){

        }
    }

    /**
     *
     * @param term
     * @return converts string into byte array of size 30
     */
    private byte[] stringToByteArray(String term){
        byte [] stringInByte=term.getBytes(StandardCharsets.UTF_8);
        byte [] fullByteArray=new byte[32];
        for (int i = 0; i<fullByteArray.length ; i++) {
            if(i<stringInByte.length)
                fullByteArray[i]=stringInByte[i];
            else
                fullByteArray[i]=35;//# in ascii
        }
        return fullByteArray;

    }

    /**
     *
     * @param bytes
     * @param idf
     * @param ptr
     * @return make line of dictionary
     */
    private byte[] addToArray(byte[] bytes,int idf, int ptr){
        byte[] idf_ =toBytes(idf);
        byte [] ptr_=toBytes(ptr);
        byte [] line=new byte[40];
        for (int i = 0; i <32 ; i++) {
            line[i]=bytes[i];
        }
        for (int i = 32; i <36 ; i++) {
            line[i]=idf_[i-32];
        }
        for (int i = 36; i <40 ; i++) {
            line[i]=idf_[i-36];
        }
        return line;
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

    /**
     * Binary search
     * @param term
     * @return array which the first int is wheter or not the line was found, the 2nd int is the line it is in or should be after
     */
    private int[] getLineInDic(String term) {
        if (this.lineNumDic > 0) {
            try {
                RandomAccessFile dictionary = new RandomAccessFile(this.dictionary, "r");
                int line = this.lineNumDic / 2;
                int start = 0;
                int end = this.lineNumDic;
                int[] toReturn = new int[2];
                while (start <= end) {
                    line = (start + end) / 2;
                    dictionary.seek(line * 40);
                    byte[] stringInLine = new byte[32];
                    dictionary.read(stringInLine);
                    String theTerm = new String(stringInLine, StandardCharsets.UTF_8);
                    theTerm = theTerm.substring(0, theTerm.indexOf("#"));
                    if (term.equalsIgnoreCase(theTerm)) {
                        dictionary.close();
                        toReturn[0] = 1;
                        toReturn[1] = line;
                        return toReturn;
                    } else if (term.compareTo(theTerm) < 0) {
                        if (line == 0)//need to be first//
                        {
                            dictionary.close();
                            toReturn[0] = 0;
                            toReturn[1] = -1;
                            return toReturn;
                        } else
                            end = line - 1;
                    } else//term>termfromline
                    {
                        if (line == this.lineNumDic - 1) {
                            dictionary.close();
                            toReturn[0] = 0;
                            toReturn[1] = line;
                            return toReturn;
                        }
                        start = line + 1;
                    }
                }
                dictionary.close();
                toReturn[0] = 0;
                toReturn[1] = line - 1;
                return toReturn;

            } catch (Exception e) {
                e.printStackTrace();
            }
        }else {
            int [] toret=new int[]{0,0};
            return toret;
        }
            return new int[2];
        }



}
