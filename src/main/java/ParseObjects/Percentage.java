package ParseObjects;

/**
 * this static class parse String to Percentage term form using {@link #Parse(String)}:
 * {@link Number} percent/percentage/per cent/%"  => {@link Number}%
 * ***Our Extra: we find that the documents big number of times talking about percent and write "per cent" instead.
 * it's the old form of this word, and we want to retrieve it also.
 */
public class Percentage {
    /**
     * {@link Number} percent/percentage/%"  => {@link Number}%
     * ***Our Extra: we find that the documents big number of times talking about percent and write "per cent" instead.
     * it's the old form of this word.
     * @param percent
     * @return percent after parse.
     */
    public static String Parse(String percent){
        String out=Number.Parse(percent);
        if(out.equals("")){//parse didnt work
            return "";
        }
        String newOut=out+"%";
        return newOut;
    }

    /**
     * @param nextTerm
     * @param nextnextTerm
     * @return true if next token is percent || percentage
     * the nextterm= per and nextnextterm= cent.
     */
    public static boolean isPercent(String nextTerm, String nextnextTerm) {
        if (nextTerm.equalsIgnoreCase("Percent")
                || nextTerm.equalsIgnoreCase("Percentage")) {
            return true;
        } else if (nextTerm.equalsIgnoreCase("per")  //use the gusses we cut the puncuation in the end.
                && nextnextTerm.equalsIgnoreCase("cent")) {
            return true;
        } else {
            return false;
        }
    }

}
