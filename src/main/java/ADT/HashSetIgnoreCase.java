package ADT;

import java.util.HashSet;
import java.util.List;

public class HashSetIgnoreCase extends HashSet<String> {

    public HashSetIgnoreCase(List<String> strings) {
        super(strings);
    }
    @Override
    public boolean contains(Object o) {
        String s;
        try{
            s=(String)o;
            return super.contains(s.toLowerCase());
        }
        catch(Exception e){
            return false;
        }
    }

}
