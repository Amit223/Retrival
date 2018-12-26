import ADT.HashSetIgnoreCase;

import java.awt.*;
import java.util.Arrays;
import java.util.HashSet;

public class StopWords {
    private static HashSetIgnoreCase stopwords; //not include between and may.


    /**
     * build the list
     */
    public static void setStopwords(String theWords){
        String [] words=theWords.split(",");
        stopwords = new HashSetIgnoreCase(Arrays.asList(words)); //use vector beacause: https://stackoverflow.com/questions/11001330/java-split-string-performances
        if(stopwords.size()>0) {
            if (stopwords.contains("May")) {
                stopwords.remove("May");
            }
            if (stopwords.contains("Between")) {
                stopwords.remove("Between");
            }
            stopwords.add("amp");
            stopwords.add("dr");
            stopwords.add("m");
            stopwords.add("a");
            stopwords.add("mr");
            stopwords.add("mrs");
        }
    }

    public static boolean isStopWord(String s){
        boolean ans = stopwords.contains(s);
        return ans;
    }

    public static void reset(){
        stopwords.clear();
    }
}
