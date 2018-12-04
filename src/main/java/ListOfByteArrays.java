import java.util.ArrayList;
import java.util.List;

public class ListOfByteArrays {

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
    private List<String > thelist;
    public ListOfByteArrays() {
        thelist=new ArrayList<>();
    }

    public void add(String toAdd){
        thelist.add(toAdd);
    }


    public String get(int i){
        return thelist.get(i);
    }

    public int size(){
        return thelist.size();
    }
    public void delete(){
        thelist.clear();
    }

}
