import java.util.ArrayList;
import java.util.List;

/**
 *
 * @param <T>
 *
 *     this class used to to lists of temp posting lines
 */
public class ListOfByteArrays <T>{

    private List<T > thelist;

    /**
     * constructor
     */
    public ListOfByteArrays() {
        thelist=new ArrayList<>();
    }

    /**
     * @param toAdd
     * adds to list
     */
    public void add(T toAdd){
        thelist.add(toAdd);
    }


    /**
     *
     * @param i
     *
     * @return T in location i from list
     */
    public T get(int i){
        return thelist.get(i);
    }

    /**
     *
     * @return size of list
     */
    public int size(){
        return thelist.size();
    }

    /**
     * reser the list
     */
    public void delete(){
        thelist.clear();
    }

}
