/**
 * this class is record of table in the dictionaryController.
 */
public class Record {

    private String term;
    private int total_tf;

    public Record(String term, int total_tf) {
        this.term = term;
        this.total_tf = total_tf;
    }

    public String getTerm() {
        return term;
    }

    public int getTotal_tf() {
        return total_tf;
    }

    public void setTerm(String term) {
        this.term = term;
    }

    public void setTotal_tf(int total_tf) {
        this.total_tf = total_tf;
    }
}
