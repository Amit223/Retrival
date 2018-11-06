import java.util.HashSet;
import java.util.Set;

public class Main {
    public static void main(String[] args) {
        String s="computer";
        PorterStemmer stemmer=new PorterStemmer();
        stemmer.add(s.toCharArray(),s.length());
        stemmer.stem();
        System.out.println(stemmer.toString());
    }
}
