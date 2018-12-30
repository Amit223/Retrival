
import java.util.Map;
import java.util.Vector;

public class Document {
    private int _docNum;
    private String _name;
    private Map<String, Integer> _entities;

    public Document(int docNum, String name, Map<String, Integer> entities) {
        this._docNum = docNum;
        this._name = name;
        this._entities= entities;
    }

    public int get_docNum() {
        return _docNum;
    }

    public String get_name() {
        return _name;
    }

    public void set_docNum(int _docNum) {
        this._docNum = _docNum;
    }

    public void set_name(String _name) {
        this._name = _name;
    }

    public Map<String, Integer> get_entities() {
        return _entities;
    }

}
