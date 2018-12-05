import java.util.ArrayList;
import java.util.List;

public class ListOfByteArrays <T>{

   /**
    private List<byte[]> thelist;
    public ListOfByteArrays() {
        thelist=new ArrayList<>();
    }

    public void add(byte[] toAdd){
        thelist.add(toAdd);
    }


    public byte[] get(int i){
        return thelist.get(i);
    }

    public int size(){
        return thelist.size();
    }
    public void delete(){
        thelist.clear();
    }
    **/
    private List<T > thelist;
    public ListOfByteArrays() {
        thelist=new ArrayList<>();
    }

    public void add(T toAdd){
        thelist.add(toAdd);
    }


    public T get(int i){
        return thelist.get(i);
    }

    public int size(){
        return thelist.size();
    }
    public void delete(){
        thelist.clear();
    }

}
