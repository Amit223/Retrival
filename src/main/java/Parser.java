import ParseObjects.*;
import ParseObjects.Number;
import org.apache.commons.codec.binary.StringUtils;
import org.jsoup.helper.StringUtil;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Vector;

/*this class is for parsing sentence in the text.*/
public class Parser {

    //todo - remove from stop words may and between, and add a, mrs. mr. dr.
    //private variables for the parser work:
    private Vector<String> _ListOfTokens = new Vector<>();
    private int _index = 0; //the token that we work on from the list of token.
    private PorterStemmer _stemmer; //use for stemming
    private boolean _toStem=false; // true if want to stem the terms before insert to dictionary, false if not.
    private String _cityUp=""; //the city(in upper case) to save its locations.
    private Integer _tokenCounter=0; //count every token that i pop. //when tokens is splitted by space.
    private String _token = "";
    private boolean _isMinus = false;//is the _token start with minus
    private String _token2 = "";
    private String _token3 = "";
    private String _token4 = "";
    private String _token5 = "";
    private String _token6 = "";

    //The final product for the indexer:
    private HashMap<String, Integer> _termList = new HashMap<String, Integer>(); //Map of terms-Pairs. The pair include the name of the doc the _token include in and the number of times.
    private Vector<Integer> _cityLocations = new Vector<>(); //vector of the city's locations in the doc.
    private int _wordCounter =0; // count the word in the document.
    //todo todoss.....
    //todo laws. km
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
     * stem only if needed.
     * @param theToken - to stem.
     * @return
     */
    private String stem(String theToken){
        if (_toStem) {
            _stemmer.add(theToken.toCharArray(), theToken.length());
            _stemmer.stem();
            theToken = _stemmer.toString();
        }
        return theToken;
    }
    /**
     * Get token
     * Stem the token
     *
     * @return theToken
     **/
    private String getToken_RemovePuncuation_Stem() {
        try {
            String theToken = "";
            if (_index < _ListOfTokens.size()) {
                theToken = removeFromTheTermUndefindSigns(_ListOfTokens.get(_index));
            }
            _index++;
            if(theToken.length()!=0){_tokenCounter++;}
            return (theToken != null) ? theToken : "";
        } catch (Exception e) {
            return "";
        }
    }

    private void downIndex(int tokenLength) {
        _index--;
        if(tokenLength!=0)_tokenCounter--;
    }

    /** //todo change.
     * remove from the _token:
=======
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
            int startIndex = -1;// the index that the _token begin.
            int endIndex = termS.length(); //the index that the _token ends
            int startIndex = -1;// the index that the term begin.
            int endIndex = termS.length(); //the index that the term ends
            if (termS != null && !termS.equals("")) {
                for (int i = 0; i < termS.length() && startIndex == -1; i++) {
                    if (Character.isDigit(termS.charAt(i))
                            || Character.isLetter(termS.charAt(i))
                            || termS.charAt(i) == '$') {
                        startIndex = i;
                        break;
                    }
                }
                for (int i = termS.length() - 1; 0 < i && endIndex == termS.length(); i--) {
                    if (Character.isDigit(termS.charAt(i))
                            || Character.isLetter(termS.charAt(i))
                            || termS.charAt(i) == '%') {
                        endIndex = i;
                        break;
                    }
                }
                if (startIndex >= endIndex) return "";
                if (startIndex - 1 >= 0 && termS.charAt(startIndex - 1) == '-') _isMinus = true;
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
     * @param termS - _token in string.
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
     * @param finalTerms - the final Terms to add the final _token list.
     */
    private void addToTermList(Vector<String> finalTerms) {
        for (int i = 0; i < finalTerms.size(); i++) {
            addToTermList(finalTerms.get(i));
        }
    }

    /**
     * @param finalTerm - a final _token to add to _token list
     */
    private void addToTermList(String finalTerm) {
        if(_isMinus)
        if(isMinus)
            finalTerm="-"+finalTerm;
        if (finalTerm != null || !finalTerm.equalsIgnoreCase("")) {
            if (!finalTerm.equalsIgnoreCase("between")
                    || !finalTerm.equalsIgnoreCase("and")) {
                if (_termList.containsKey(finalTerm)) {
                    _termList.put(finalTerm, _termList.get(finalTerm) + 1);
                } else {
                    _termList.put(finalTerm, 1);
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
     * _index-- if nextnextterm isn't relavante.
     *
     * @param nextTerm
     * @param nextnextTerm
     * @return true if next _token is dollars ||
     * the nextterm= u.s. and nextnextterm= dollars.
     */
    private boolean isPrice(String nextTerm, String nextnextTerm) {
        if (nextTerm.equalsIgnoreCase("dollars")) {
            downIndex(nextnextTerm.length());
            return true;
        } else if (nextTerm.equalsIgnoreCase("u.s")  //use the gusses we cut the puncuation in the end.
                && nextnextTerm.equalsIgnoreCase("dollars")) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * @param termS- _token to add
     */
    private void yesUndefinedTerm_parseCLAndAddToTermList(String termS) {
        if (termS != null && !termS.equals("")) {
            String termsLow = termS.toLowerCase();
            Integer counterLow = _termList.get(termsLow);
            String termsUp = termS.toUpperCase();
            Integer counterUp = _termList.get(termsUp);
            char firstC = termS.charAt(0);
            if (counterLow != null) { //exist lower case
                _termList.put(termsLow, counterLow + 1);
            } else if (counterUp != null) { //exist upper case
                if (Character.isLowerCase(firstC)) {
                    _termList.remove(termsUp);
                    _termList.put(termsLow, counterUp + 1);
                } else if (Character.isUpperCase(firstC)) {
                    if(termsUp.equals(_cityUp))
                        _cityLocations.add(_tokenCounter);
                    _termList.put(termsUp, counterUp + 1);
                } else { //not going to happend because @see x
                    _termList.remove(termsUp);
                    _termList.put(termsLow, counterUp + 1);
                }
            } else {// ***()
                if (Character.isUpperCase(firstC)) {
                    if(termsUp.equals(_cityUp)) _cityLocations.add(_tokenCounter);
                    _termList.put(termsUp, 1);
                } else {
                    _termList.put(termsLow, 1); // x
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
     * we find _token's first char is digit, so it can be:
     * 1. Number
     * 2. Price
     * 3. Percent
     * 4. Term or no one.
     */
    private void yesDigit_isNumberPricePrecentTermOrNoOne() {
        if (isNumeric(_token)) {
            String theNumber = _token;
            _token2 = getToken_RemovePuncuation_Stem();
        if (isNumeric(term)) {
            String theNumber = term;
            term2 = getToken_RemovePuncuation_Stem();
            boolean isPrecent = false;  //for the case of 55 billion%
            if (!_token2.equals("") && _token2.charAt(_token2.length() - 1) == '%') {  //for the case of 55 billion%
                String term2tmp = _token2.substring(0, _token2.length() - 1); //todo save _token2.length as variable
                if (yesNumeric_isModifierOrFraction(_token2)) {
                    isPrecent = true;
                    _token2 = term2tmp;
                }
            }
            boolean isModorFrac = yesNumeric_isModifierOrFraction(_token2);
            String nextTerm, nextnextTerm;

            //check the next terms:
            if (isModorFrac) {
                theNumber = theNumber + " " + _token2;
                //for the checking later:
                _token3 = getToken_RemovePuncuation_Stem();
                nextTerm = _token3;
                _token4 = getToken_RemovePuncuation_Stem();
                nextnextTerm = _token4;
            } else { // _token2 isn't mod or frac
                nextTerm = _token2;
                _token3 = getToken_RemovePuncuation_Stem();
                nextnextTerm = _token3;
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
                    addToTermList(Between.Parse(_token + " " + nextTerm + " " + nextnextTerm));
                } else {
                    downIndex(nextnextTerm.length());
                    addToTermList(Between.Parse(_token + " " + nextTerm));
                }
            } else if (isPrecent || Percentage.isPercent(nextTerm)) {
                downIndex(nextnextTerm.length()); //didn't recognize the next next _token.
                addToTermList(Percentage.Parse(theNumber));
            } else if (Date.isMonth(nextTerm)) {
                downIndex(nextnextTerm.length()); //didn't Recognize nextTerm
                addToTermList(Date.Parse(_token + " " + nextTerm));
            } else if (Distance.isDistance(nextTerm)) {
                downIndex(nextnextTerm.length()); //didn't Recognize next next Token
                addToTermList(Distance.Parse(theNumber+" "+nextTerm));
            } else { //its pure number
                downIndex(nextTerm.length()); //didn't recognize the nextTerm.
                downIndex(nextnextTerm.length()); //didnt recognize the nextnextTerm.
                addToTermList(Number.Parse(theNumber));
            }
        }//if\
        else { //isn't numeric. kind of _token.
            String termTmp = endWithBnM(_token);
            if (!termTmp.equals("")) {
                addToTermList(Price.Parse(termTmp)); //todo - problem: go over twice on u.s. dollars after.
            } else if (isBetween(_token)) {
                addToTermList(Between.Parse(_token));
            } else { //undefined _token
                yesUndefinedTerm_parseCLAndAddToTermList(_token);
            }
        }//else\
    }//yesDigit_isNumberPricePrecentTermOrNoOne\

    /**
     * Dollar F C - we know for sure the first char was dollar,
     * so we try figure out:
     * if its price (the _token without the $ is numeric)
     * if there is modifier or Frac.
     * <p>
     * if the _token without the $ is numeric and it has modifier or fraction-
     * we will add: _token + " " + _token2
     * if no modifier or fraction
     * we will add: _token
     * if no numeric without $ we wouldn't do anything.
     */
    private void yesDollarFC_isPrice_isWithModOrFrac() {
        if (isNumeric(_token)) {
            String theNumber = _token;
            _token2 = getToken_RemovePuncuation_Stem();
            boolean isModOrFrac = yesNumeric_isModifierOrFraction(_token2);
            if (isModOrFrac) theNumber = theNumber + " " + _token2;
            else downIndex(_token2.length()); //we didn't recognise _token2
       
        //else do nothing - we dont need  things like: $2324rjjdffadf3
    }

    /**
     * we know found _token is between, now we work like that:
     * if exists:
     * 1. 2 numbers
     * 2. the word and
     * we add it to the final terms as one _token, and his components as more terms. (except the stopwords "between" and "and".
     * example: +"between 5 million and 7 million" => 5M-7M
     * +"between 5 and 7 million" => 5-7M
     */
    private void yesBetween_isNumberAndNumber_hasModOrFrac() {
        _token2 = getToken_RemovePuncuation_Stem();
        _token3 = getToken_RemovePuncuation_Stem();
        _token4 = getToken_RemovePuncuation_Stem();
        _token5 = getToken_RemovePuncuation_Stem();
        _token6 = getToken_RemovePuncuation_Stem();
        boolean isBetween = true;
        String number1 = "", number2 = "", between = "";
        if (isNumeric(_token2)) {
            if (_token3.equalsIgnoreCase("And")) {
                number1 = _token2;
                //between _token2 and _token4...
                if (isNumeric(_token4)) {
                    if (yesNumeric_isModifierOrFraction(_token5)) {
                        number2 = _token4 + " " + _token5;
                    } else {
                        number2 = _token4;
                        downIndex(_token5.length()); //we don't need _token5
                    }
                    addToTermList(Between.Parse(number1 + "-" + number2));
                    downIndex(_token6.length()); //we don't need _token6.
                }//else - not between because it's not two numbers.
                else isBetween = false;
            } // _token3 isn't "And"
            else if (_token4.equalsIgnoreCase("And")
                    && yesNumeric_isModifierOrFraction(_token3)) {
                number1 = _token2 + " " + _token3;
                //between _token2 _token3 and _token5...
                if (isNumeric(_token5)) {
                    if (yesNumeric_isModifierOrFraction(_token6)) {
                        number2 = _token5 + " " + _token6;
                    } else {
                        number2 = _token5;
                        downIndex(_token5.length()); //we don't need _token5
                    }
                    addToTermList(Between.Parse(number1 + "-" + number2));
                } //_token5  isn't numeric.
                else isBetween = false;
            } // _token4 isn't "And"
            else isBetween = false;
        }//_token2 isn't numeric.
        else isBetween = false;
        if (!isBetween) {// it's between but not with 2 numbers.
            //we don't need to save it as one _token of between.
            //so we just need to reverse the _index back.
            downIndex(_token6.length()); //->_token6
            downIndex(_token5.length()); //->_token5
            downIndex(_token4.length()); //->_token4
            downIndex(_token3.length()); //->_token3
            downIndex(_token2.length()); //->_token2
            //now next time it will continue parse from _token2.
        }//if not between\
    }//yesBetween_isNumber....()\

    /**
     * we found _token is name of month,
     * now we check if its part of Date _token.
     * if yes, parse it and add it to the _termList.
     */
    private void yesMonth_isDate() {
        _token2 = getToken_RemovePuncuation_Stem();
        if (isNumeric(_token2)) {
            addToTermList(Date.Parse(_token + " " + _token2));
        term2 = getToken_RemovePuncuation_Stem();
        if (isNumeric(term2)) {
            addToTermList(Date.Parse(term + " " + term2));
        } else {
            downIndex(_token2.length());
            yesUndefinedTerm_parseCLAndAddToTermList(_token);
        }
    }

    /**the parse functions:**/

    /**
     * parsing a sentece.
     *
     * @param sentenceInDoc - separated by '/n'.
     */
    private void ParseSentence(String sentenceInDoc) {
        _index =0;
        _ListOfTokens = new Vector<String>(Arrays.asList(sentenceInDoc.split(" "))); //use vector beacause: https://stackoverflow.com/questions/11001330/java-split-string-performances
        if(_toStem)_stemmer=new PorterStemmer();
        char FirstC;
        char lastC;
        while (_index < _ListOfTokens.size()) { //parse _token:
            _token = "";
            _isMinus = false;//is the _token start with minus
            _token2 = "";
            _token3 = "";
            _token4 = "";
            _token5 = "";
            _token6 = "";
            _token = getToken_RemovePuncuation_Stem();
            if (!StopWords.isStopWord(_token) && _token !=null && !_token.equals("")) {
                FirstC = _token.charAt(0);
                lastC = _token.charAt(_token.length() - 1);//todo save _token length as variable.

                if (lastC == '%') {
                    _token = _token.substring(0, _token.length() - 1);
                    if (isNumeric(_token)) {
                        addToTermList(Percentage.Parse(_token));
                    }
                    //else do anything because we didn't want words like  ssfdk2222%
                } else if (Character.isDigit(FirstC)) {
                    yesDigit_isNumberPricePrecentTermOrNoOne();
                } else if (FirstC == '$') {
                    _token = _token.substring(1);
                    yesDollarFC_isPrice_isWithModOrFrac();
                } else if (Character.isLetter(FirstC)) {
                    _isMinus =false;
                    if (_token.equalsIgnoreCase("Between")) {
                        yesBetween_isNumberAndNumber_hasModOrFrac();
                    } else if (Date.isMonth(_token)) {
                        yesMonth_isDate();
                    } else if (_token.contains("-")) { // todo 12-13 3/4
                        addToTermList(Between.Parse(_token));
                    } else {
                        yesUndefinedTerm_parseCLAndAddToTermList(stem(_token));
                    }
                } else {
                    yesUndefinedTerm_parseCLAndAddToTermList(_token);
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
     *
     * @param doc - the document to pars
     * @param toStem - to stem the words?
     * @param city - the city that we want to return her locations.
     */
    public void Parse(String doc, boolean toStem, String city){
        _toStem=toStem;
        _cityUp=city.toUpperCase();
        if(!doc.equals("")) {
            Vector<String> ListOfSentences = new Vector<String>(Arrays.asList(doc.split("\n"))); //use vector beacause: https://stackoverflow.com/questions/11001330/java-split-string-performances
            int size = ListOfSentences.size();
            for (int i = 0; i < size; i++) {
                ParseSentence(ListOfSentences.get(i));
            }
        }
    }

    /**getters for the indexer**/

    /**
     * @return the term list.
     */
    public HashMap<String, Integer> getTerms() {
        return _termList;
    }

    /**
     * @return vector of the city locations in the text.
     */
    public Vector<Integer> getLocations(){
        return _cityLocations;
    }

    /**
     *
     * @return the number of the words in the text.
     */
    public int getWordCount(){
        return _wordCounter;
    }

    /**
     * for tests todo delete
     */
    public void printTermList() {
        System.out.println(_termList.toString()+"\n"+_cityLocations.toString());

    }
}//Parser class\



