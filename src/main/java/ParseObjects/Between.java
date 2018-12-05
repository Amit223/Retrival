package ParseObjects;
import java.util.Vector;

/**
 * this static class parse String to Between term form using {@link #Parse(String)}:
 * "word-word" => word-word, term, term
 * "word-word-word"=> word-word-word, term, term, term
 * "number-word" || "word-number" => {@link Number}-term ||  term-{@link Number}, term, {@link Number}
 * "number-number" => {@link Number}-{@link Number}, {@link Number}, {@link Number}
 * "between number and number" => {@link Number}-{@link Number}, {@link Number}, {@link Number}
 */
public class Between {

    /**
     * ***Our Extra: we add the {@link Between} term to the _termList as one term, and his components as more terms.
     * that's in order to retrieve in the next part documents that include also those terms.
     * "word-word" => word-word, term, term
     * "word-word-word"=> word-word-word, term, term, term
     * "number-word" || "word-number" => {@link Number}-term ||  term-{@link Number}, term, {@link Number}
     * "number-number" => {@link Number}-{@link Number}, {@link Number}, {@link Number}
     * "between number and number" => {@link Number}-{@link Number}, {@link Number}, {@link Number}
     * @param between
     * @return vector of terms so term is x-x and x is word or num + x if word + x after parse if num
     */
    public static Vector<String> Parse(String between) {
        //between=word-word; word-word-word; number-word; word-number; number-number; number number; number frac/mod-number frac/mod; number-number mod/frac;
        Vector<String> terms = new Vector<String>();
        try {
            if (!between.contains("-")) //between number and number
            {
                String[] strings = between.split(" ");
                terms.add(strings[0] + "-" + strings[1]);
                terms.add(strings[0]);
                terms.add(strings[1]);
            } else { //with-
                String[] serperte = between.split("-");
                if (serperte.length == 2) {
                    if (isNum(serperte[0]) && isNum(serperte[1])) {//num - num
                        String num1 = Number.Parse(serperte[0]);
                        String num2 = Number.Parse(serperte[1]);
                        terms.add(num1 + "-" + num2);
                        terms.add(num1);
                        terms.add(num2);
                    } else if (isNum(serperte[0]) && !isNum(serperte[1])) {//num-word
                        String num = Number.Parse(serperte[0]);
                        terms.add(num + "-" + serperte[1]);
                        terms.add(num);
                        terms.add(serperte[1]);
                    } else if (!isNum(serperte[0]) && isNum(serperte[1])) {//word-num
                        String num = Number.Parse(serperte[1]);
                        terms.add(serperte[0] + "-" + num);
                        terms.add(num);
                        terms.add(serperte[0]);
                    } else {//word word
                        terms.add(serperte[0] + "-" + serperte[1]);
                        terms.add(serperte[0]);
                        terms.add(serperte[1]);

                    }

                } else {//word-word-word
                    terms.add(serperte[0] + "-" + serperte[1] + "-" + serperte[2]);
                    terms.add(serperte[0]);
                    terms.add(serperte[1]);
                    terms.add(serperte[2]);
                }
            }
            return terms;
        }
        catch (Exception e){
            return new Vector<String>();//empty vector
        }
    }

    /**helpful functions to the {@link #Parse(String)} **/

    /**
     * helpful private function to the {@link #Parse(String)}
     * check if it may be a num.
     * @param word
     * @return true if may be a real num- 5 , 5.3 , 0.9 and false otherwise
     */
    private static boolean isNum(String word){
        if(Character.isDigit(word.charAt(0))){
            if(word.charAt(0)!='0'){
                return true;
            }
            else {
                if(word.contains("."))
                    return true;
                else
                    return false;
            }
        }
        else
            return false;
    }

    /**helpful functions to the sort part in the #Parser**/

    /**
     * use it for # yesDigit_isNumberPricePrecentTermOrNoOne()
     * @param theToken
     * @return true only if its between term
     */
    public static boolean isBetween(String theToken) {
        String[] termComponnents = (theToken).split("-"); //array
        return termComponnents.length == 2;
    }

    /**
     * between  terms has two parts. part0 and part1 looks like part0-part1.
     * this function check if the given part is numeric, for private using, so don't treat #partidx!=0||1
     * @param termS   - term of between
     * @param partidx - zero or one
     * @return
     */
    public static boolean isThePartOfBetweenIsNumeric(String termS, int partidx) {
        String[] termComponnents = (termS).split("-"); //array 13
        return (termComponnents.length == 2 &&
                Number.isNumeric(termComponnents[partidx]));
    }


}
