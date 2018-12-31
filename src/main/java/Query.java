import java.util.Collection;

public class Query {
    private String _queryId;
    private Collection<Document> _documents;

    public Query(String _queryId, Collection<Document> _documents) {
        this._queryId = _queryId;
        this._documents = _documents;
    }

    public String get_queryId() {
        return _queryId;
    }

    public void set_queryId(String _queryId) {
        this._queryId = _queryId;
    }

    public Collection<Document> get_documents() {
        return _documents;
    }

    public void set_documents(Collection<Document> _documents) {
        this._documents = _documents;
    }
}
