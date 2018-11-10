package ParseObjects;

import java.util.Vector;

public class Between {

    /**
     *
     * @param between
     * @return vector of terms so term is x-x and x is word or num + x if word + x after parse if num
     */
    public static Vector<String> Parse(String between){
        //between=word-word; word-word-word; number-word; word-number; number-number; number number
        Vector <String> terms= new Vector<String>();
        if(!between.contains("-")) //between number and number
        {
            String [] strings=between.split(" ");
            terms.add(strings[0]);
            terms.add(strings[1]);
            terms.add(strings[0]+"-"+strings[1]);
        }
        else { //with-
            String[] serperte = between.split("-");
            if(serperte.length==2){
                if(isNum(serperte[0])&&isNum(serperte[1])){//num - num
                    Vector<String > num1=Number.Parse(serperte[0]);
                    Vector<String > num2=Number.Parse(serperte[1]);
                    terms.add(num1.elementAt(0));
                    terms.add(num2.elementAt(0));
                }
                else if(isNum(serperte[0])&&!isNum(serperte[1])){//num-word
                    Vector<String> num=Number.Parse(serperte[0]);
                    terms.add(num.elementAt(0));
                    terms.add(serperte[1]);
                }
                else if(!isNum(serperte[0])&&isNum(serperte[1])){//word-num
                    Vector<String> num=Number.Parse(serperte[1]);
                    terms.add(num.elementAt(0));
                    terms.add(serperte[0]);
                }
                else{//word word
                    terms.add(serperte[0]);
                    terms.add(serperte[1]);
                }
                terms.add(serperte[0]+"-"+serperte[1]);

            }
            else{//word-word-word
                terms.add(serperte[0]);
                terms.add(serperte[1]);
                terms.add(serperte[2]);
                terms.add(serperte[0]+"-"+serperte[1]+"-"+serperte[2]);
            }
        }


        return terms;
    }


    /**
     *
     * @param word
     * @return true if num- 5 , 5.3 , 0.9 and false otherwise
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
}
