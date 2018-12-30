import ParseObjects.*;
import ParseObjects.Number;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Vector;

/**
 * this class goal is parsing text in file.
 * the goal: its focus on sort the token to the next auxiliary classes' parse functions:
 * {@link Number}
 * {@link Price}
 * {@link Percentage}
 * {@link Date}
 * {@link Distance}
 * {@link Between}
 **/
public class Parser {

    //private variables for the parser work:
    private Vector<String> _ListOfTokens = new Vector<>(); //list of tokens in sentence
    private int _index = 0; //the token that we work on from the list of token.
    private PorterStemmer _stemmer; //use for stemming
    private boolean _toStem = false; // true if want to stem the terms before insert to dictionary, false if not.
    private String _cityUp = ""; //the city(in upper case) to save its locations.
    private Integer _tokenCounter = 0; //count every token that i pop. //when tokens is splitted by space.
    private String _token = "";
    private boolean _isMinus = false;//is the _token start with minus
    private String _token2 = "";
    private String _token3 = "";
    private String _token4 = "";
    private String _token5 = "";
    private String _token6 = "";


    public Parser() {
        _ListOfTokens = new Vector<>();
        _index = 0; //the token that we work on from the list of token.
        _stemmer = new PorterStemmer(); //use for stemming
        _toStem = false; // true if want to stem the terms before insert to dictionary, false if not.
        _cityUp = ""; //the city(in upper case) to save its locations.
        _tokenCounter = 0; //count every token that i pop. //when tokens is splitted by space.
        _token = "";
        _isMinus = false;//is the _token start with minus
        _token2 = "";
        _token3 = "";
        _token4 = "";
        _token5 = "";
        _token6 = "";
    }

    //The final product for the indexer:
    private HashMap<String, Integer> _termList = new HashMap<String, Integer>(); //Map of terms-TF.
    private Vector<Integer> _cityLocations = new Vector<>(); //vector of the city's locations in the document.

    /*Auxiliary functions for the whole program*/

    /**
     * stem only if _toStem=true.
     *
     * @param theToken - to stem.
     * @return theToken
     * @see #stemPartOfBetween(String, int)
     */
    private String stem(String theToken) {
        if (_toStem) {
            if (theToken.contains("-")) {
                Vector<String> vector = new Vector<String>(Arrays.asList(theToken.split("-")));
                theToken = "";
                for (int i = 0; i < vector.size(); i++) {
                    String s = vector.get(i);
                    _stemmer.add(s.toCharArray(), s.length());
                    _stemmer.stem();
                    s = _stemmer.toString();
                    theToken += s;
                    if (i != vector.size() - 1) theToken += "-";
                }
            } else {
                _stemmer.add(theToken.toCharArray(), theToken.length());
                _stemmer.stem();
                theToken = _stemmer.toString();
            }
        }
        return theToken;
    }

    /**
     * ***Our Extra: stem for each part of word-word, word-number, number-word.
     * stem only the part0 of between or the part1 if _toStem=true.
     *
     * @param theBetweenToken - to stem ex. 13-dogs
     * @param idxPartToStem   - 0 or 1, else not stem
     * @return theBetweenToken - after the one part stem.
     * @see #stem(String)
     */
    private String stemPartOfBetween(String theBetweenToken, int idxPartToStem) {
        if (idxPartToStem == 1 || idxPartToStem == 0) {
            if (_toStem) {
                if (theBetweenToken.contains("-")) {
                    Vector<String> vector = new Vector<String>(Arrays.asList(theBetweenToken.split("-")));
                    String partToStem = vector.get(idxPartToStem);
                    String part0 = vector.get(0);
                    String part1 = vector.get(1);
                    _stemmer.add(partToStem.toCharArray(), partToStem.length());
                    _stemmer.stem();
                    partToStem = _stemmer.toString();
                    if (idxPartToStem == 0) theBetweenToken = partToStem + "-" + part1;
                    if (idxPartToStem == 1) theBetweenToken = part0 + "-" + partToStem;
                }
            }
        }
        return theBetweenToken;
    }

    /**
     * Get token from _ListOfTokens {@link #_ListOfTokens}
     * using {@link #removeFromTheTermUndefindSigns(String)}
     * remove punctuation
     * treat the index, and the _tokenCounter
     *
     * @return theToken or "" if the sentence is ended.
     * @see #_tokenCounter
     **/
    private String getToken_RemovePunctuation() {
        try {
            String theToken = "";
            if (_index < _ListOfTokens.size()) {
                theToken = removeFromTheTermUndefindSigns(_ListOfTokens.get(_index));
            }
            _index++;
            if (theToken.length() != 0) {
                _tokenCounter++;
            }
            return (theToken != null) ? theToken : "";
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * index--;
     * _tokenCounter++ if it isn't "". @see _wordCounter
     *
     * @param tokenLength to identify if its empty string and recognize that isn't consider a token.
     */
    private void downIndex(int tokenLength) {
        _index--;
        if (tokenLength != 0) _tokenCounter--;
    }

    /**
     * ***Our Extra: we do it also for the terms in the between terms. @see {@link Between}
     * remove from the beginning and the ending of the _token undefined signs.
     * the defined signs are only digit, letter or $ in the beginning,
     * or only digit, letter  or % in the end.
     * if find minus: _isMinus=true, so we
     * using for {@link #getToken_RemovePunctuation()}
     * {@link #addToTermList(Vector)}
     * and {@link #yesUndefinedTerm_parseCLAndAddToTermList(String)}
     * @param termS - a term
     * @param termS - the term after removing.
     */
    private String removeFromTheTermUndefindSigns(String termS) { //like: "dfsdfdsf
        try {
            int startIndex = -1;// the index that the _token begin.
            int endIndex = termS.length(); //the index that the _token ends
            if (termS != null && !termS.equals("")) {
                for (int i = 0; i < termS.length() && startIndex == -1; i++) {
                    if (Character.isDigit(termS.charAt(i))
                            || Character.isLetter(termS.charAt(i))
                            || termS.charAt(i) == '$') {
                        startIndex = i;
                        break;
                    }
                }
                for (int i = termS.length() - 1; 0 <= i && endIndex == termS.length(); i--) {
                    if (Character.isDigit(termS.charAt(i))
                            || Character.isLetter(termS.charAt(i))
                            || termS.charAt(i) == '%') {
                        endIndex = i;
                        break;
                    }
                }
                if (termS.length() > 1 && startIndex >= endIndex) return "";
                if (startIndex - 1 >= 0 && termS.charAt(startIndex - 1) == '-') _isMinus = true;
                if ((startIndex != 0) || (endIndex != termS.length())) {
                    termS = termS.substring(startIndex, endIndex + 1);
                }
                return termS;
            }
            return "";
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * ***Our Extra: treat the terms inside Between term:
     * 1. removing undefined signs: @see removeFromTheTermUndefindSigns
     * 2. Capital letter check: @see yesUndefinedTerm_parseCLAndAddToTermList
     * for between see {@link #Parse(String, boolean, String) that return vector}
     * use {@link #addToTermList(String)} for  the between term
     * or {@link #yesUndefinedTerm_parseCLAndAddToTermList(String)} for the terms inside the between
     *
     * @param finalTerms - the final Terms to add the _termList {@link #_termList}.
     */
    private void addToTermList(Vector<String> finalTerms) {
        for (int i = 0; i < finalTerms.size(); i++) {
            if (i == 0) //the first one is the one and only between term
                addToTermList(finalTerms.get(i));
            else
                _ListOfTokens.add(removeFromTheTermUndefindSigns(finalTerms.get(i)));
        } // sdfjk-"Y&^%%^&%15,  sdfjk, %15
    }

    /**
     * treat the TF.
     *
     * @param finalTerm - a final _token to add to {@link #_termList}
     */
    private void addToTermList(String finalTerm) {
        if (!StopWords.isStopWord(finalTerm)) {
            if (_isMinus)
                finalTerm = "-" + finalTerm;
            if (finalTerm != null && !finalTerm.equalsIgnoreCase("")) {
                if (!finalTerm.equalsIgnoreCase("between")
                        && !finalTerm.equalsIgnoreCase("and") && !StopWords.isStopWord(finalTerm)) {
                    if (_termList.containsKey(finalTerm)) {
                        _termList.put(finalTerm, _termList.get(finalTerm) + 1);
                    } else {
                        _termList.put(finalTerm, 1);
                    }
                }
            }
        }
    }


    /** Auxiliary functions for #yesDigit_isNumberPricePrecentDateDistanceBetweenOrNoOne */

    /**
     * ***Our Extra: we can also recognize m and bn as modifier for #Price, #Percentage and #Distance
     * ***Our Extra: we can recognize fraction for #Price, #Percentage and #Distance
     * modifier is Million, Billion, Trillion, Thousand, m, bn
     *
     * @param theToken
     * @return true if its modifier or fraction. false if either.
     */
    private boolean yesNumeric_isModifierOrFraction(String theToken) {
        if (!theToken.equals("")) {
            String[] parts = theToken.split("/");
            if (parts.length == 2) {
                return Number.isNumeric(parts[0]) &&
                        Number.isNumeric(parts[1]);
            } else if (theToken.equalsIgnoreCase("Million")
                    || theToken.equalsIgnoreCase("Billion")
                    || theToken.equalsIgnoreCase("Trillion")
                    || theToken.equalsIgnoreCase("Thousand")
                    || theToken.equalsIgnoreCase("m")
                    || theToken.equalsIgnoreCase("bn")) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    /**
     * ***Our Extra: city that accidentally written in  not Capital letter will consider too.
     * this function is for the case that we have undefined term.
     * we change the first char to lower case if there is a word in this document that has an instance in first char lower case.
     * we complete this check ( only in {@link Indexer#ifExistUpdateTF(String) for the whole corpus}
     *
     * @param undefinedTerm- undefined term to add
     */
    private void yesUndefinedTerm_parseCLAndAddToTermList(String undefinedTerm) {
        undefinedTerm = removeFromTheTermUndefindSigns(undefinedTerm); // for " Balldur's " case after stem its  " Balldur' "
        if (!undefinedTerm.equals("") && Character.isDigit(undefinedTerm.charAt(0))) {
            addToTermList(undefinedTerm);
            return;
        } //if "13asZz" ,"13 M" , "13%" we add to dictionary the same.
        if (!StopWords.isStopWord(undefinedTerm)) {
            if (undefinedTerm != null && !undefinedTerm.equals("")) {
                String termsLow = undefinedTerm.toLowerCase();
                Integer counterLow = _termList.get(termsLow);
                String termsUp = undefinedTerm.toUpperCase();
                Integer counterUp = _termList.get(termsUp);
                char firstC = undefinedTerm.charAt(0);
                if (counterLow != null) { //exist lower case
                    _termList.put(termsLow, counterLow + 1);
                } else if (counterUp != null) { //exist upper case
                    if (Character.isLowerCase(firstC)) {
                        _termList.remove(termsUp);
                        _termList.put(termsLow, counterUp + 1);
                    } else if (Character.isUpperCase(firstC)) {

                        _termList.put(termsUp, counterUp + 1);
                    } else { //not going to happend because @see x
                        _termList.remove(termsUp);
                        _termList.put(termsLow, counterUp + 1);
                    }
                } else {// *()
                    if (Character.isUpperCase(firstC)) {
                        _termList.put(termsUp, 1);
                    } else {
                        _termList.put(termsLow, 1); // x
                    }
                }
                if (termsUp.equals(_cityUp)) _cityLocations.add(_tokenCounter);
            }
        }
    }


    /*Auxiliary functions for ParseSentence**/

    /**
     * ***Our Extra: for the number-word number-number cases.
     * we consider number as number fraction/modifier even in between terms. @see {@link Between}
     * it's placed in this class because it's include modifier check and between check, two different class.
     * @param theToken
     * @return true if its modifier-token or fraction-token
     * for ex. true: million-13, million-dogs, 3/4-games
     */
    private boolean isBetweenModOrFracAndToken(String theToken) {
        String[] termComponnents = (theToken).split("-"); //array 13
        return (termComponnents.length == 2
                && yesNumeric_isModifierOrFraction(termComponnents[0]));
    }

    /**
     * use in {@link #yesDigit_isNumberPricePrecentDateDistanceBetweenOrNoOne()}
     * ***Our Extra: we recognize numbermodifier without space and fix it.
     * ex. 5m parse as Number Modifier normally even it's not needed in the instructions.
     * if end with bn or m so return it with space. either return "" (that's because it's for private use.)
     *
     * @param theToken
     * @return theToken - Number Modifier with space or "" other case.
     */
    private String endWithBnMToTheSameWithSpace(String theToken) {
        if (theToken.length() > 2) {
            if (theToken.charAt(theToken.length() - 1) == 'm') {
                theToken = theToken.substring(0, theToken.length() - 1) + " m";
                return theToken;
            } else if ((theToken.charAt(theToken.length() - 2) == 'b')
                    && (theToken.charAt(theToken.length() - 1) == 'n')) {
                theToken = theToken.substring(0, theToken.length() - 2) + " bn";
                return theToken;
            } else return "";
        } else return "";
    }

    /** used for {@link #Parse(String, boolean, String)}
     *  using {@link #isBetweenModOrFracAndToken(String)}
     * we find _token's first char is digit, now decide which of those:
     * 1. Number
     * 2. Price
     * 3. Percent
     * 4. Distance
     * 5. Date
     * 6. Between
     * 4. no one.
     */
    private void yesDigit_isNumberPricePrecentDateDistanceBetweenOrNoOne() {
        if (Number.isNumeric(_token)) {
            String theNumber = _token;
            _token2 = getToken_RemovePunctuation();
            boolean isPrecent = false;  //for the case of 55 billion%
            if (!_token2.equals("") && _token2.charAt(_token2.length() - 1) == '%') {  //for the case of 55 billion%
                String token2tmp = _token2.substring(0, _token2.length() - 1); //todo save _token2.length as variable
                if (yesNumeric_isModifierOrFraction(token2tmp)) {
                    isPrecent = true;
                    _token2 = token2tmp;
                }
            }
            boolean isModorFrac = yesNumeric_isModifierOrFraction(_token2);
            String nextTerm, nextnextTerm;

            //check the next terms:
            if (isModorFrac) {
                theNumber = theNumber + " " + _token2;
                //for the checking later:
                _token3 = getToken_RemovePunctuation();
                nextTerm = _token3;
                _token4 = getToken_RemovePunctuation();
                nextnextTerm = _token4;
            } else { // _token2 isn't mod or frac
                nextTerm = _token2;
                _token3 = getToken_RemovePunctuation();
                nextnextTerm = _token3;
            }

            //check what kind of parse needed, down the index if needed, parse and add to termlist.

            if (Price.isPrice(nextTerm, nextnextTerm)) {
                if (!(nextTerm.equalsIgnoreCase("u.s")
                        && nextnextTerm.equalsIgnoreCase("dollars")))
                    downIndex(nextnextTerm.length());
                addToTermList(Price.Parse(theNumber));
            } else if (isBetweenModOrFracAndToken(nextTerm)) {
                if (Between.isThePartOfBetweenIsNumeric(nextTerm, 1) && yesNumeric_isModifierOrFraction(nextnextTerm)) { // 12 miillion-13 million
                    addToTermList(Between.Parse(_token + " " + nextTerm + " " + nextnextTerm));
                } else {
                    downIndex(nextnextTerm.length());
                    addToTermList(Between.Parse(stemPartOfBetween(_token + " " + nextTerm, 1))); //13 million-dogs, 12 million-13 todo something like 13 million-dog's??
                }
            } else if (isPrecent || Percentage.isPercent(nextTerm, nextnextTerm)) {
                if (!(nextTerm.equalsIgnoreCase("per")
                        && nextnextTerm.equalsIgnoreCase("cent")))
                    downIndex(nextnextTerm.length()); //didn't recognize the next next _token.
                addToTermList(Percentage.Parse(theNumber));
            } else if (Date.isMonth(nextTerm)) {
                downIndex(nextnextTerm.length()); //didn't Recognize nextTerm
                addToTermList(Date.Parse(_token + " " + nextTerm));
            } else if (Distance.isDistance(nextTerm)) {
                downIndex(nextnextTerm.length()); //didn't Recognize next next Token
                addToTermList(Distance.Parse(theNumber + " " + nextTerm));
            } else { //its pure number
                downIndex(nextTerm.length()); //didn't recognize the nextTerm.
                downIndex(nextnextTerm.length()); //didnt recognize the nextnextTerm.
                addToTermList(Number.Parse(theNumber));
            }
        }//if\
        else { //isn't numeric. kind of _token.
            String termTmp = endWithBnMToTheSameWithSpace(_token);
            if (!termTmp.equals("")) {
                addToTermList(Price.Parse(termTmp));
            } else if (Between.isBetween(_token)) {
                if (Between.isThePartOfBetweenIsNumeric(_token, 1)) {  // IF 11-*13*
                    _token2 = getToken_RemovePunctuation();
                    if (yesNumeric_isModifierOrFraction(_token2)) { // IF 11-13 million // include:12-13 3/4
                        addToTermList(Between.Parse(_token + " " + _token2));
                    } else { //12-13
                        downIndex(_token2.length());
                        addToTermList(Between.Parse(_token));
                    }
                } else { //11-dogs
                    addToTermList(Between.Parse(stemPartOfBetween(_token, 1))); //need to stem dogs.
                }
            } else { //totally undefined _token
                yesUndefinedTerm_parseCLAndAddToTermList(_token);
            }
        }//else\
    }//yesDigit_isNumberPricePrecentDateDistanceBetweenOrNoOne\

    /**
     * ***Our Extra: if we get a term start with dollar and the continuance isn't numeric -
     * we don't add it to {@link #_termList} because we consider it as garbage.
     * Dollar F C - we know for sure the first char was dollar,
     * so we try figure out:
     * if its price (the _token without the $ is numeric)
     * if there is modifier or Frac.
     *
     * if the _token without the $ is numeric and it has modifier or fraction-
     * we will add: _token + " " + _token2
     * if no modifier or fraction
     * we will add: _token
     * if no numeric without $ we wouldn't do anything.
     */
    private void yesDollarFC_isPrice_isWithModOrFrac() {
        if (Number.isNumeric(_token)) {
            String theNumber = _token;
            _token2 = getToken_RemovePunctuation();
            boolean isModOrFrac = yesNumeric_isModifierOrFraction(_token2);
            if (isModOrFrac) theNumber = theNumber + " " + _token2;
            else downIndex(_token2.length()); //we didn't recognise _token2
            addToTermList(Price.Parse(theNumber));
        }
        //else do nothing - we dont need  things like: $2324rjjdffadf3
    }

    /**
     * ***Our Extra: we add the {@link Between} term to the {@link #_termList} as one term, and his components as more terms.
     * that's in order to retrieve in the next part documents that include also those terms.
     * we know found _token is between, now we work like that:
     * if exists:
     * 1. 2 numbers
     * 2. the word and
     * we add it to the final terms as one token, and his components as more terms. (except the stopwords "between" and "and").
     * example: +"between 5 million and 7 million" => 5M-7M
     * +"between 5 and 7 million" => 5-7M
     */
    private void yesBetween_isNumberAndNumber_hasModOrFrac() {
        _token2 = getToken_RemovePunctuation();
        _token3 = getToken_RemovePunctuation();
        _token4 = getToken_RemovePunctuation();
        _token5 = getToken_RemovePunctuation();
        _token6 = getToken_RemovePunctuation();
        boolean isBetween = true;
        String number1 = "", number2 = "", between = "";
        if (Number.isNumeric(_token2)) {
            if (_token3.equalsIgnoreCase("And")) {
                number1 = _token2;
                //between _token2 and _token4...
                if (Number.isNumeric(_token4)) {
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
                if (Number.isNumeric(_token5)) {
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
     * ***Our Extra: we add the {@link Date} term to the {@link #_termList} as one term, and the year as {@link Number} term.
     * that's in order to retrieve in the next part documents that include also this Number or if someone want to retrieve.
     * we found that the _token is name of month,
     * now we check if its part of Date term.
     * if yes, parse it and add it to the _termList.
     */
    private void yesMonth_isDate() {
        _token2 = getToken_RemovePunctuation();
        if (Number.isNumeric(_token2)) {
            addToTermList(Date.Parse(_token + " " + _token2));
        } else {
            downIndex(_token2.length());
            yesUndefinedTerm_parseCLAndAddToTermList(stem(_token));
        }
    }


    /*the parse functions:*/

    /**
     * using by {@link #Parse(String, boolean, String)}
     * the goal: its focus on sort the token to the next auxiliary classes' parse functions:
     * parsing a sentence:
     * 1. parse it to tokens and put it in {@link #_ListOfTokens}
     * 2. pop a {@link #_token} and reset all the fields. {@link #_token2} {@link #_token3} {@link #_token4} {@link #_token5} {@link #_token6} {@link #_isMinus}
     * 3. #_removeFromTheTermUndefindSigns from the {@link #_token}
     * 4. search hint and sort the tokens to different ways to parse:
     * Percentage{@link #Parse(String, boolean, String)}
     * {@link #yesDigit_isNumberPricePrecentDateDistanceBetweenOrNoOne()}
     * {@link #yesDollarFC_isPrice_isWithModOrFrac()}
     * {@link #yesBetween_isNumberAndNumber_hasModOrFrac()}
     * {@link #yesMonth_isDate()}
     * Between{@link #Parse(String, boolean, String)}
     * {@link #yesUndefinedTerm_parseCLAndAddToTermList(String)} using -
     * {@link #stem(String)} if {@link #_toStem}
     * we decide that the terms that not going to any of the above, will fo to {@link #yesUndefinedTerm_parseCLAndAddToTermList(String)}
     * without stemming, because we don't want to change something that we dont know.
     * @param sentenceInDoc - separated by '/n'.
     */
    private void ParseSentence(String sentenceInDoc) {
        _index = 0;
        _ListOfTokens = new Vector<String>(Arrays.asList(sentenceInDoc.split(" "))); //use vector beacause: https://stackoverflow.com/questions/11001330/java-split-string-performances
        if (_toStem) _stemmer = new PorterStemmer();
        char FirstC;
        char lastC;
        while (_index < _ListOfTokens.size()) { //parse _token:
            _isMinus = false;//is the _token start with minus
            _token = "";
            _token2 = "";
            _token3 = "";
            _token4 = "";
            _token5 = "";
            _token6 = "";
            _token = getToken_RemovePunctuation();
            if (!StopWords.isStopWord(_token) && _token != null && !_token.equals("")) {
                FirstC = _token.charAt(0);
                lastC = _token.charAt(_token.length() - 1);
                if (lastC == '%') {
                    _token = _token.substring(0, _token.length() - 1);
                    if (Number.isNumeric(_token)) {
                        addToTermList(Percentage.Parse(_token));
                    }
                    //else do anything because we didn't want words like  ssfdk2222%
                } else if (Character.isDigit(FirstC)) {
                    yesDigit_isNumberPricePrecentDateDistanceBetweenOrNoOne();
                } else if (FirstC == '$') {
                    _token = _token.substring(1);
                    yesDollarFC_isPrice_isWithModOrFrac();
                } else if (Character.isLetter(FirstC)) {
                    _isMinus = false;
                    if (_token.equalsIgnoreCase("Between")) {
                        yesBetween_isNumberAndNumber_hasModOrFrac();
                    } else if (Date.isMonth(_token)) {
                        yesMonth_isDate();
                    } else if (_token.contains("-")) {
                        addToTermList(Between.Parse(stem(_token))); //word-word //word-number //word-word-word
                    } else {
                        yesUndefinedTerm_parseCLAndAddToTermList(stem(_token));
                    }
                } else {
                    yesUndefinedTerm_parseCLAndAddToTermList(_token); //we don't have to stem because its not a word and we want to save it as is.
                }
            }//if is stopword\
        }
    }//ParseSentence function\


    /**
     * using by {@link ThreadedIndex#run()} for every document
     * go over the sentences in the document and {@link #ParseSentence(String)}
     * Prepare to the {@link Indexer}  this fields:
     * {@link #_termList}
     * {@link #_cityLocations}
     * {@link #_tokenCounter}
     * @param doc    - the document to pars
     * @param toStem - to stem the words? if true - stemming.
     * @param city   - the city that we want to return her locations, appear in the <F P =104></F>
     */
    public void Parse(String doc, boolean toStem, String city) {
        _toStem = toStem;
        _cityUp = city.toUpperCase();
        if (!doc.equals("")) {
            Vector<String> ListOfSentences = new Vector<String>(Arrays.asList(doc.split("\n"))); //use vector beacause: https://stackoverflow.com/questions/11001330/java-split-string-performances
            int size = ListOfSentences.size();
            for (int i = 0; i < size; i++) {
                ParseSentence(ListOfSentences.get(i));
            }
        }
        //printTermList();
    }


    /*getters for the indexer*/

    /**
     * @return the {@link #_termList}
     */
    public HashMap<String, Integer> getTerms() {
        return _termList;
    }

    /**
     * @return vector of {@link #_cityLocations} in the text.
     */
    public Vector<Integer> getLocations() {
        return _cityLocations;
    }

    /**
     * @return the {@link #_tokenCounter} in the text.
     */
    public int getWordCount() {
        return _tokenCounter;
    }

    /**
     * for tests todo delete
     */
    public void printTermList() {
        System.out.println(_termList.toString() + "\n" + _cityLocations.toString() + "\n" + _termList.size());
    }
}//Parser class\