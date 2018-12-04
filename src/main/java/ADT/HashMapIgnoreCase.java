package ADT;

import java.util.HashMap;

public class HashMapIgnoreCase extends HashMap<String, Integer> {
    @Override
    public Integer put(String key, Integer value) {
        return super.put(key.toLowerCase(), value);
    }

    @Override
    public Integer get(Object key) {
        return super.get(key.toString().toLowerCase());
    }

    @Override
    public boolean containsKey(Object key) {
        String s;
        try{
            s=(String)key;
            return super.containsKey(s.toLowerCase());
        }
        catch(Exception e){
            return false;
        }
    }


}