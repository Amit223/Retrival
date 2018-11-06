import java.util.Arrays;
import java.util.Dictionary;
import java.util.HashSet;
import java.util.Vector;

public class Parser {

    Dictionary<String,Integer> termList;


    //this class is for parsing all the text.
   public void Parse(String doc){
       Vector<String> ListOfTokens= new Vector <String> (Arrays.asList(doc.split(" "))); //use vector beacause: https://stackoverflow.com/questions/11001330/java-split-string-performances
       PorterStemmer stemmer=new PorterStemmer();
       String term;
       //stopwords//
       StopWords.setStopWords();
       HashSet<String> stopwords=StopWords.getStopwords();
       for (int i = 0; i < ListOfTokens.size() ; i++) {
           term=ListOfTokens.get(i);
           term= removePunctuationsFromString(term);
            if(!stopwords.contains(term)) {//not stop word!!
                //stemming
                stemmer.add(term.toCharArray(),term.length());
                stemmer.stem();
                term=stemmer.toString();
                //if-else

            }

       }

   }

    /**
     *
     * @param s
     * @return s without puncuation-
     *      remove from begining only 1 Punctuation.
     *      remove from the end only 1 Punctuation.
     */
    private String removePunctuationsFromString(String s) {
        s = s.replaceAll("\\s*\\p{Punct}+\\s*$", "");//remove from the end only 1 Punctuation.
        s.replaceAll("^\\p{Punct}|\\p{Punct}$", "");//remove from begining only 1 Punctuation.
       return s;
    }

    /**
     *
     * @return the list of terms.
     */
    public Dictionary<String, Integer> getTermList() {
        return termList;
    }
}
