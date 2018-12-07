/**
 * this class is record of table in the dictionaryController.
 */
public class Record {

    private String term;
    private int total_tf;

    /**
     * constructor
     *
     * @param term
     * @param total_tf
     */
    public Record(String term, int total_tf) {
        this.term = term;
        this.total_tf = total_tf;
    }

    /**
     *
     * @return the term
     */
    public String getTerm() {
        return term;
    }

    /**
     *
     * @return the total tf
     */
    public int getTotal_tf() {
        return total_tf;
    }

}
