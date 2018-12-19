
import javafx.util.Pair;
import org.omg.IOP.Encoding;

import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class Main {
    public static void main(String[] args) {
       byte[] num=toBytes(999999);
        System.out.println(convertByteToInt(num));
        FilterDocsByCitys();
    }
    public static int convertByteToInt(byte[] b)
    {

        int pomAsInt = ByteBuffer.wrap(b).getInt();
        return pomAsInt;

    }

    public static byte[] toBytes(int i)
    {
        /**
        byte[] result = new byte[4];

        result[0] = (byte) (i >> 24);
        result[1] = (byte) (i >> 16);
        result[2] = (byte) (i >> 8);
        result[3] = (byte) (i /*>> 0*/
        byte[] pom = ByteBuffer.allocate(4).putInt(i).array();

        return pom;
    }

    private static void FilterDocsByCitys() {
        Vector<String> _chosenCities=new Vector<>();
        HashMap<Integer,String> _doc_termPlusTfs=new HashMap<>();
        _doc_termPlusTfs.put(0,"AAA");
        _doc_termPlusTfs.put(1,"AAA");
        _doc_termPlusTfs.put(2,"AAA");
        String _path="C:\\Users\\AMIT MOSHE\\Desktop\\אוניברסיטה\\סמסטר ה\\אחזור\\tosave";
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
            int size;
            while (iterator.hasNext()){
                int docLine=iterator.next();
                raf.seek(docLine * 54 + 50);//to get to the size
                byte[] length_bytes=new byte[4];
                raf.read(length_bytes);
                size=convertByteToInt(length_bytes);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}