import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;

public class Main {
    public static void main(String[] args) {
        Dictionary<String,Integer>dic= new Hashtable<>();
        dic.put("A",2);
        dic.put("b",5);
        dic.put("ee",9);
        dic.put("whyyyyyyyyyyy",1);
        dic.put("z",10);
        Indexer indexer=new Indexer();
        indexer.Index(dic,"a","b");


        // System.out.println(term);

    }

}
