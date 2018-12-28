import javafx.util.Pair;

import java.nio.file.Path;
import java.util.Collection;
import javax.xml.stream.events.StartDocument;
import java.io.*;

import java.util.HashSet;
import java.util.Map;
import java.util.Vector;

public class Model_2 {

    private Searcher searcher;
    private double avgldl;
    private int numOfIndexedDocs;

    public Vector<Pair<String,Collection<Integer>>> Start(String path, Vector<String> cities, Path queriesPath, boolean toStem, boolean toTreatSemantic) throws IOException {
        Vector<Pair<String,Collection<Integer>>> id_docsCollection= new Vector<>();
        HashSet<String> citieshash = new HashSet<>(cities);
        readIndexerInfo(path,toStem);
        Vector<Pair<String, String>> queries = ReadFile.readQueriesFile(queriesPath);
        searcher=new Searcher(avgldl,numOfIndexedDocs,path,citieshash,toStem);

        for (int i = 0; i <queries.size() ; i++) {
            Pair <String,String>id_query= queries.get(i);
            String id = id_query.getKey();
            String query = id_query.getValue();
            Collection<Integer> queryDocs=  searcher.Search(query, toTreatSemantic);
            id_docsCollection.add(new Pair<String,Collection<Integer>>(id,queryDocs));
        }
        return id_docsCollection;
    }
    /**
     * This function is the main function of the program.
     */
    public Collection<Integer> Start(String path, Vector<String> cities, String query, boolean toStem, boolean toTreatSemantic){
        HashSet<String> citieshash = new HashSet<>(cities);
        readIndexerInfo(path,toStem);

        searcher=new Searcher(avgldl,numOfIndexedDocs,path,citieshash,toStem);
        return searcher.Search(query, toTreatSemantic);
    }

    private void readIndexerInfo(String path,boolean toStem) {
        try {
            BufferedReader bufferedReader=new BufferedReader(new FileReader(new File(path+"Details"+toStem+".txt")));
            String avg=bufferedReader.readLine();
            String numOfFiles=bufferedReader.readLine();
            bufferedReader.close();
            avgldl=Double.parseDouble(avg);
            numOfIndexedDocs=Integer.parseInt(numOfFiles);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}

