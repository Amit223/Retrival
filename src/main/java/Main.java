import ParseObjects.Distances;

import java.io.File;
import java.io.RandomAccessFile;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Vector;

public class Main {
    public static void main(String[] args) {
        Test2();
    }

    public void Test1()
    {
        Indexer indexer=new Indexer();
        indexer.delete();
        Dictionary<String,Integer> dic=new Hashtable<>();
        dic.put("A",5);
        dic.put("B",4);
        dic.put("aa",3);
        dic.put("c",1);
        dic.put("F",10);
        dic.put("g",100);
        Vector<Integer> locations=new Vector<>();
        locations.add(1);
        locations.add(10);
        locations.add(20);
        locations.add(100);
        indexer.Index(dic,locations,"AAA","Tallin",20000);
        indexer.Index(dic,locations,"AAA","LOL",20000);

        byte[] d1=new byte[4];
        byte[] d2=new byte[4];
        byte[] d3=new byte[4];
        try {
            RandomAccessFile raf=new RandomAccessFile(new File("Documents.txt"),"rw");
            raf.seek(38);
            raf.read(d1);
            raf.seek(42);
            raf.read(d2);
            raf.seek(46);
            raf.read(d3);
            raf.close();
        }
        catch (Exception e){
            System.out.println("ppppppp");
        }
        int dd1=indexer.byteToInt(d1);
        int dd2=indexer.byteToInt(d2);
        int dd3=indexer.byteToInt(d3);
        System.out.println(dd1);
        System.out.println(dd2);
        System.out.println(dd3);
        // System.out.println(term);

        System.out.println(indexer.getCityDictionary().toString());
        System.out.println(indexer.getDictionary().toString());

    }

    public static void Test2(){
        String dis="10,000.3 km";
        System.out.println(Distances.Parse(dis));
    }
}
