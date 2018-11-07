import java.util.*;
import ParseObjects.Number;
import ParseObjects.Percentage;
import ParseObjects.Price;

public class Parser {

    Dictionary<String, Integer> termList;

    //forParseSentence
    Vector<String> ListOfTokens;
    HashSet<String> stopwords = StopWords.getStopwords();
    HashSet<String> months; {// TODO: 07/11/2018  add all month opthins. 
        PorterStemmer stemmer = new PorterStemmer();
        Vector<String> tokensDesktop;//the four that i work with in the for loop.
        String term, term2, term3, term4, term5, term6;
        int i=0;
        boolean twoArg = false, threeArg = false, fourArg = false ; //true if use _ arg


        //this class is for parsing sentence in the text.
        public void ParseSentence(String sentenceInDoc) {
            ListOfTokens = new Vector<String>(Arrays.asList(sentenceInDoc.split(" "))); //use vector beacause: https://stackoverflow.com/questions/11001330/java-split-string-performances
            //stopwords//
            StopWords.setStopWords();

            char FirstC;
            char lastC;
            i = 0;
            while (i < ListOfTokens.size()) { //todo i=0 or 3 ????????
                term =  getToken_removePunc_Stem_AndAddToDesktop();
                if (!stopwords.contains(term)) {//not stop word!!
                    FirstC = term.charAt(0);
                    lastC = term.charAt(term.length()-1);
                    if (Character.isDigit(FirstC))
                        if(lastC=='%') Percentage.Parse(term); //like 36%
                        else
                            isNumberPriceDateOrPrecent();
                    else if (FirstC == '$') isDollarStartWith$();
                }
            }
        }

        /**
         * Get token
         * Remove puncuation from it
         * Stem the token
         * Add to desktop
         * @return theToken

**/
        private String getToken_removePunc_Stem_AndAddToDesktop()
        {
            String theToken =ListOfTokens.get(i);
            i++;
            theToken = removePunctuationsFromString(theToken);
            stemmer.add(theToken.toCharArray(), theToken.length());
            stemmer.stem();
            theToken = stemmer.toString();
            tokensDesktop.add(theToken);
            return theToken;
        }



        private void isNumberPriceDateOrPrecent() {
            term2=getToken_removePunc_Stem_AndAddToDesktop();

            //Fraction:
            char firstC= term2.charAt(0);
            if( Character.isDigit(firstC))
                isWithFraction();
            //Price:
            if(term2.equalsIgnoreCase("Dollars"))
                Price.Parse(term);

            //Percentage:
            if(term2.equalsIgnoreCase("Percent")
                    || term2.equalsIgnoreCase("Percentage")
                    || term2.equalsIgnoreCase("%"))
                Percentage.Parse(term); //like 36 % || 36 percent

            if(term2.equalsIgnoreCase("Thousand")) {
                Number.Parse(term,term2); //like: 3 tousand //todo did it possible 1 tousand precent?
            }

            //like 3 million:
            if(term2.equalsIgnoreCase("Million")
                    || term2.equalsIgnoreCase("Billion")
                    || term2.equalsIgnoreCase("Trillion")){
                isNumber();
                isPrice();
                isPercent();//todo- did it possible 1 milion precent?
            }
        }

        if(term3.)
    }
    private void isWithFraction(){
        isPureNumber();
        isPrice();
        isPercent();//todo - can be 10 1/2 percent? if yes- add
    }


    private void isDollarStartWith$() {
    }

    /**
     *
     * @param s
     * @return s without puncuation-
     *      remove from begining only 1 Punctuation.
     *      remove from the end only 1 Punctuation.
**/
    private String removePunctuationsFromString(String s) {
        s = s.replaceAll("\\s*\\p{Punct}+\\s*$", "");//remove from the end only 1 Punctuation.
        s.replaceAll("^\\p{Punct}|\\p{Punct}$", "");//remove from begining only 1 Punctuation.
        return s;
    }

    /**
     *
     * @return the list of terms.
**/
    public Dictionary<String, Integer> getTermList() {
        return termList;
    }
}
