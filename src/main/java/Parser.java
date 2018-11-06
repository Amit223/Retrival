import java.util.ArrayList;
import java.util.Arrays;
import java.util.Dictionary;
import java.util.Vector;

public class Parser {

    Dictionary<String,Integer> termList;
    //this class is for parsing all the text.
   public void Parse(String doc){
       Vector<String> ListOfTokens= new Vector <String> (Arrays.asList(doc.split(" "))); //use vector beacause: https://stackoverflow.com/questions/11001330/java-split-string-performances
        PorterStemmer stemmer=new PorterStemmer();
        
   }

    public Dictionary<String, Integer> getTermList() {
        return termList;
    }
}
