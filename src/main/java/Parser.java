import ParseObjects.*;
import ParseObjects.Number;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Vector;

/*this class is for parsing sentence in the text.*/
public class Parser {

    //private variables for the parser work:
    private HashMap<String, Integer> termList = new HashMap<String, Integer>(); //Map of terms-Pairs. The pair include the name of the doc the term include in and the number of times.
    private Vector<String> ListOfTokens = new Vector<>();
    private int index = 0; //the token that we work on from the list of token.
    private PorterStemmer stemmer = new PorterStemmer(); //use for stemming
    private boolean toStem; // true if want to stem the terms before insert to dictionary, false if not.
    private String term = "";
    private boolean isMinus = false;//is the term start with minus //todo
    private String term2 = "";
    private String term3 = "";
    private String term4 = "";
    private String term5 = "";
    private String term6 = "";
    //todo debug on sentcenes
    //todo debug on text.
    //todo todoss.....
    //todo laws. km mr mrs bercovich
    //mr mrs - add to stop word.
    //todo : find all the token index of all the "city" and return in vector.

    /**helpful functions for the whole program**/

    /**
     * Get token
     * Stem the token
     *
     * @return theToken
     **/
    private String getToken_RemovePuncuation_Stem() {
        try {
            String theToken = "";
            if (index < ListOfTokens.size()) {
                theToken = removeFromTheTermUndefindSigns(ListOfTokens.get(index));
            }
            index++;
            if (toStem) {
                stemmer.add(theToken.toCharArray(), theToken.length());
                stemmer.stem();
                theToken = stemmer.toString();
            }
            return (theToken != null) ? theToken : "";
        } catch (Exception e) {
            return "";
        }
    }

    private void downIndex() {
        index--;
    }

    /** //todo change.
     * remove from the term:
     * 1. '-' (at the start)
     * 2. '.' (at the end)/
     * 3. '?'
     * 4. '...'
     * 5. '!'
     * 6. ','
     */
    private String removeFromTheTermUndefindSigns(String termS) {
        try {
            int startIndex = -1;// the index that the term begin.
            int endIndex = termS.length(); //the index that the term ends
            if (termS != null && !termS.equals("")) {
                for (int i = 0; i < termS.length() && startIndex == -1; i++) {
                    if (Character.isDigit(termS.charAt(i))
                            || Character.isLetter(termS.charAt(i))
                            || termS.charAt(i) == '$') {
                        startIndex = i;
                    }
                }
                for (int i = termS.length() - 1; 0 < i && endIndex == termS.length(); i--) {
                    if (Character.isDigit(termS.charAt(i))
                            || Character.isLetter(termS.charAt(i))
                            || termS.charAt(i) == '%') {
                        endIndex = i;
                    }
                }
                if (startIndex >= endIndex) return "";
                if (startIndex - 1 >= 0 && termS.charAt(startIndex - 1) == '-') isMinus = true;
                if ((startIndex != 0) || (endIndex != termS.length())) {
                    termS = termS.substring(startIndex, endIndex + 1);
                }
                return termS;
            }
            return "";
        }
        catch (Exception e) {
            return "";
        }
    }

    /**
     * @param termS - term in string.
     * @return true - if the string is numeric. else false
     */
    private boolean isNumeric(String termS) {
        try {
            termS = Number.RemoveComas(termS);
            Double.parseDouble(termS);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * @param finalTerms - the final Terms to add the final term list.
     */
    private void addToTermList(Vector<String> finalTerms) {
        for (int i = 0; i < finalTerms.size(); i++) {
            addToTermList(finalTerms.get(i));
        }
    }

    /**
     * @param finalTerm - a final term to add to term list
     */
    private void addToTermList(String finalTerm) {
        if(isMinus)
            finalTerm="-"+finalTerm;
        if (finalTerm != null || !finalTerm.equalsIgnoreCase("")) {
            if (!finalTerm.equalsIgnoreCase("between")
                    || !finalTerm.equalsIgnoreCase("and")) {
                if (termList.containsKey(finalTerm)) {
                    termList.put(finalTerm, termList.get(finalTerm) + 1);
                } else {
                    termList.put(finalTerm, 1);
                }
            }
        }
    }

    /** helpful functions for yesDigit_isNumberPricePrecentTermOrNoOne */

    /**
     * @param termS
     * @return true if its modifier or fraction. false if either.
     */
    private boolean yesNumeric_isModifierOrFraction(String termS) {
        if (!termS.equals("")) {
            String[] parts = termS.split("/");
            if (parts.length == 2) {
                try {
                    isNumeric(parts[0]);
                    isNumeric(parts[1]);
                    return true;
                } catch (Exception e) {
                    return false;
                }
            } else if (termS.equalsIgnoreCase("Million")
                    || termS.equalsIgnoreCase("Billion")
                    || termS.equalsIgnoreCase("Trillion")
                    || termS.equalsIgnoreCase("Thousand")
                    || termS.equalsIgnoreCase("m")
                    || termS.equalsIgnoreCase("bn")) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    /**
     * index-- if nextnextterm isn't relavante.
     *
     * @param nextTerm
     * @param nextnextTerm
     * @return true if next term is dollars ||
     * the nextterm= u.s. and nextnextterm= dollars.
     */
    private boolean isPrice(String nextTerm, String nextnextTerm) {
        if (nextTerm.equalsIgnoreCase("dollars")) {
            downIndex();
            return true;
        } else if (nextTerm.equalsIgnoreCase("u.s")  //use the gusses we cut the puncuation in the end.
                && nextnextTerm.equalsIgnoreCase("dollars")) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * @param nextTerm - check if its percent or precentage.
     * @return
     */
    private boolean isPercent(String nextTerm) {
        if (nextTerm.equalsIgnoreCase("Percent")
                || nextTerm.equalsIgnoreCase("Percentage"))
            return true;
        return false;
    }

    /**
     * @param termS- term to add
     */
    private void yesUndefinedTerm_parseCLAndAddToTermList(String termS) {
        if (termS != null && !termS.equals("")) {
            String termsLow = termS.toLowerCase();
            Integer counterLow = termList.get(termsLow);
            String termsUp = termS.toUpperCase();
            Integer counterUp = termList.get(termsUp);
            char firstC = termS.charAt(0);
            if (counterLow != null) { //exist lower case
                termList.put(termsLow, counterLow + 1);
            } else if (counterUp != null) { //exist upper case
                if (Character.isLowerCase(firstC)) {
                    termList.remove(termsUp);
                    termList.put(termsLow, counterUp + 1);
                } else if (Character.isUpperCase(firstC)) {
                    termList.put(termsUp, counterUp + 1);
                } else { //not going to happend because @see x
                    termList.remove(termsUp);
                    termList.put(termsLow, counterUp + 1);
                }
            } else {// ***()
                if (Character.isUpperCase(firstC)) {
                    termList.put(termsUp, 1);
                } else {
                    termList.put(termsLow, 1); // x
                }
            }
        }
    }

    /**helpful functions for ParseSentence*/

    /**
     * todo documention and change order
     */
    private boolean isBetween(String termS) {
        String[] termComponnents = (termS).split("-"); //array
        return termComponnents.length == 2;
    }

    /**
     * todo documention and change order
     */
    private boolean isBetweenModOrFracAndNumber(String termS) {
        String[] termComponnents = (termS).split("-"); //array
        return (termComponnents.length == 2
                && yesNumeric_isModifierOrFraction(termComponnents[0])
                && isNumeric(termComponnents[1]));
    }

    /**
     * we find term's first char is digit, so it can be:
     * 1. Number
     * 2. Price
     * 3. Percent
     * 4. Term or no one.
     */
    private void yesDigit_isNumberPricePrecentTermOrNoOne() {
        if (isNumeric(term)) {
            String theNumber = term;
            term2 = getToken_RemovePuncuation_Stem();
            boolean isPrecent = false;  //for the case of 55 billion%
            if (!term2.equals("") && term2.charAt(term2.length() - 1) == '%') {  //for the case of 55 billion%
                String term2tmp = term2.substring(0, term2.length() - 1); //todo save term2.length as term.
                if (yesNumeric_isModifierOrFraction(term2)) {
                    isPrecent = true;
                    term2 = term2tmp;
                }
            }
            boolean isModorFrac = yesNumeric_isModifierOrFraction(term2);
            String nextTerm, nextnextTerm;

            //check the next terms:
            if (isModorFrac) {
                theNumber = theNumber + " " + term2;
                //for the checking later:
                term3 = getToken_RemovePuncuation_Stem();
                nextTerm = term3;
                term4 = getToken_RemovePuncuation_Stem();
                nextnextTerm = term4;
            } else { // term2 isn't mod or frac
                nextTerm = term2;
                term3 = getToken_RemovePuncuation_Stem();
                nextnextTerm = term3;
            }

            //check what kind of parse needed

            if (isPrice(nextTerm, nextnextTerm)) {
                addToTermList(Price.Parse(theNumber));
            } else if (isBetweenModOrFracAndNumber(nextTerm)) {
                if (yesNumeric_isModifierOrFraction(nextnextTerm)) {
                    addToTermList(Between.Parse(term + " " + nextTerm + " " + nextnextTerm));
                } else {
                    downIndex();
                    addToTermList(Between.Parse(term + " " + nextTerm));
                }
            } else if (isPrecent || isPercent(nextTerm)) {
                downIndex(); //didn't recognize the next next term.
                addToTermList(Percentage.Parse(theNumber));
            } else if (Date.isMonth(nextTerm)) {
                downIndex(); //didn't Recognize nextTerm
                addToTermList(Date.Parse(term + " " + nextTerm));
            } else { //its pure number
                downIndex(); //didn't recognize the nextTerm.
                downIndex(); //didnt recognize the nextnextTerm.
                addToTermList(Number.Parse(theNumber));
            }
        }//if\
        else { //isn't numeric. kind of term.
            String termTmp = endWithBnM(term);
            if (!termTmp.equals("")) {
                addToTermList(Price.Parse(termTmp)); //todo - problem: go over twice on u.s. dollars after.
            } else if (isBetween(term)) {
                addToTermList(Between.Parse(term));
            } else { //undefined term
                yesUndefinedTerm_parseCLAndAddToTermList(term);
            }
        }//else\
    }//yesDigit_isNumberPricePrecentTermOrNoOne\

    /**
     * todo docum
     * todo change order. help for yesdigit......
     *
     * @param termS
     * @return
     */
    private String endWithBnM(String termS) {
        if (termS.length() > 2) {
            if (termS.charAt(termS.length() - 1) == 'm') { //todo variavle to .length
                termS = termS.substring(0, termS.length() - 1) + " m";
                return termS;
            } else if ((termS.charAt(termS.length() - 2) == 'b')
                    && (termS.charAt(termS.length() - 1) == 'n')) {
                termS = termS.substring(0, termS.length() - 2) + " bn";
                return termS;
            } else return "";
        } else return "";
    }

    /**
     * Dollar F C - we know for sure the first char was dollar,
     * so we try figure out:
     * if its price (the term without the $ is numeric)
     * if there is modifier or Frac.
     * <p>
     * if the term without the $ is numeric and it has modifier or fraction-
     * we will add: term + " " + term2
     * if no modifier or fraction
     * we will add: term
     * if no numeric without $ we wouldn't do anything.
     */
    private void yesDollarFC_isPrice_isWithModOrFrac() {
        if (isNumeric(term)) {
            String theNumber = term;
            term2 = getToken_RemovePuncuation_Stem();
            boolean isModOrFrac = yesNumeric_isModifierOrFraction(term2);
            if (isModOrFrac) theNumber = theNumber + " " + term2;
            else downIndex(); //we didn't recognise term2
            addToTermList(Price.Parse(theNumber));
        }
        //else do nothing - we dont need  things like: $2324rjjdffadf3
    }

    /**
     * we know found term is between, now we work like that:
     * if exists:
     * 1. 2 numbers
     * 2. the word and
     * we add it to the final terms as one term, and his components as more terms. (except the stopwords "between" and "and".
     * example: +"between 5 million and 7 million" => 5M-7M
     * +"between 5 and 7 million" => 5-7M
     */
    private void yesBetween_isNumberAndNumber_hasModOrFrac() {
        term2 = getToken_RemovePuncuation_Stem();
        term3 = getToken_RemovePuncuation_Stem();
        term4 = getToken_RemovePuncuation_Stem();
        term5 = getToken_RemovePuncuation_Stem();
        term6 = getToken_RemovePuncuation_Stem();
        boolean isBetween = true;
        String number1 = "", number2 = "", between = "";
        if (isNumeric(term2)) {
            if (term3.equalsIgnoreCase("And")) {
                number1 = term2;
                //between term2 and term4...
                if (isNumeric(term4)) {
                    if (yesNumeric_isModifierOrFraction(term5)) {
                        number2 = term4 + " " + term5;
                    } else {
                        number2 = term4;
                        downIndex(); //we don't need term5
                    }
                    addToTermList(Between.Parse(number1 + "-" + number2));
                    downIndex(); //we don't need term6.
                }//else - not between because it's not two numbers.
                else isBetween = false;
            } // term3 isn't "And"
            else if (term4.equalsIgnoreCase("And")
                    && yesNumeric_isModifierOrFraction(term3)) {
                number1 = term2 + " " + term3;
                //between term2 term3 and term5...
                if (isNumeric(term5)) {
                    if (yesNumeric_isModifierOrFraction(term6)) {
                        number2 = term5 + " " + term6;
                    } else {
                        number2 = term5;
                        downIndex(); //we don't need term5
                    }
                    addToTermList(Between.Parse(number1 + "-" + number2));
                } //term5  isn't numeric.
                else isBetween = false;
            } // term4 isn't "And"
            else isBetween = false;
        }//term2 isn't numeric.
        else isBetween = false;
        if (!isBetween) {// it's between but not with 2 numbers.
            //we don't need to save it as one term of between.
            //so we just need to reverse the index back.
            downIndex(); //->term6
            downIndex(); //->term5
            downIndex(); //->term4
            downIndex(); //->term3
            downIndex(); //->term2
            //now next time it will continue parse from term2.
        }//if not between\
    }//yesBetween_isNumber....()\

    /**
     * we found term is name of month,
     * now we check if its part of Date term.
     * if yes, parse it and add it to the termList.
     */
    private void yesMonth_isDate() {
        term2 = getToken_RemovePuncuation_Stem();
        if (isNumeric(term2)) {
            addToTermList(Date.Parse(term + " " + term2));
        } else {
            downIndex();
            yesUndefinedTerm_parseCLAndAddToTermList(term);
        }
    }


    /**
     * parsing a sentece.
     *
     * @param sentenceInDoc - separated by '/n'.
     */
    private void ParseSentence(String sentenceInDoc) {
        index=0;
        ListOfTokens = new Vector<String>(Arrays.asList(sentenceInDoc.split(" "))); //use vector beacause: https://stackoverflow.com/questions/11001330/java-split-string-performances
        char FirstC;
        char lastC;
        index = 0;
        while (index < ListOfTokens.size()) { //parse term:
            term = "";
            isMinus = false;//is the term start with minus
            term2 = "";
            term3 = "";
            term4 = "";
            term5 = "";
            term6 = "";
            term = getToken_RemovePuncuation_Stem();
            if (!StopWords.isStopWord(term) && term!=null && !term.equals("")) {
                FirstC = term.charAt(0);
                lastC = term.charAt(term.length() - 1);//todo save term length as variable.
                if (lastC == '%') {
                    term = term.substring(0, term.length() - 1);
                    if (isNumeric(term)) {
                        addToTermList(Percentage.Parse(term));
                    }
                    //else do anything because we didn't want words like  ssfdk2222%
                } else if (Character.isDigit(FirstC)) {
                    yesDigit_isNumberPricePrecentTermOrNoOne();
                } else if (FirstC == '$') {
                    term = term.substring(1);
                    yesDollarFC_isPrice_isWithModOrFrac();
                } else if (Character.isLetter(FirstC)) {
                    if (term.equalsIgnoreCase("Between")) {
                        yesBetween_isNumberAndNumber_hasModOrFrac();
                    } else if (term.contains("-")) { //todo 12-13 million
                        addToTermList(Between.Parse(term));
                    } else if (Date.isMonth(term)) {
                        yesMonth_isDate();
                    } else {
                        yesUndefinedTerm_parseCLAndAddToTermList(term);
                    }
                } else {
                    yesUndefinedTerm_parseCLAndAddToTermList(term);
                }
            }//if is stopword\
        }
    }//ParseSentence function\

    public void Parse(String Doc){
        Vector <String> ListOfSentences = new Vector<String>(Arrays.asList(Doc.split("\n"))); //use vector beacause: https://stackoverflow.com/questions/11001330/java-split-string-performances
        int size = ListOfSentences.size();
        for (int i = 0; i < size ; i++) {
            ParseSentence(ListOfSentences.get(i));
        }
    }

    /**
     * for tests todo delete
     */
    public void printTermList() {
        System.out.println(termList.toString());

    }
}//Parser class\



