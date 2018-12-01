import ADT.HashSetIgnoreCase;

import java.awt.*;
import java.util.Arrays;
public class StopWords {
    private static HashSetIgnoreCase stopwords; //not include between and may.

    /**
     * build the list
     */
    public static void setStopwords(String theWords){
        String [] words=theWords.split(",");
        stopwords = new HashSetIgnoreCase(Arrays.asList(words)); //use vector beacause: https://stackoverflow.com/questions/11001330/java-split-string-performances
    }

    public static boolean isStopWord(String s){
        boolean ans = stopwords.contains(s);
        return ans;
    }
}
