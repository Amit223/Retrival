import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

public class Main {
    public static void main(String[] args) {
        Vector<String> s= new Vector<String>(4);
        s.add("1");
        s.add("2");
        s.add("3");
        System.out.println(s.get(0)+"_"+ s.get(1)+ "_"+ s.get(2));
        s.remove(0); s.add("4");

        System.out.print(s.get(0)+"_"+ s.get(1)+ "_"+ s.get(2));
    }
}
