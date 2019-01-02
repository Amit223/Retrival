import javafx.util.Pair;

import java.nio.file.Path;
import java.util.Collection;
import java.io.*;

import java.util.HashSet;
import java.util.Vector;

public class Model_2 {

    private Searcher searcher;
    private double avgldl;
    private int numOfIndexedDocs;


    /**
     *
     * @param path of posting files
     * @param cities to choose
     * @param queriesPath the path of query file
     * @param toStem
     * @param toTreatSemantic
     * @param savePath- path to save the results file.
     * @return answers for query
     * @throws IOException
     */
    public Vector<Pair<String,Collection<Document>>> Start(String path, Vector<String> cities, Path queriesPath, boolean toStem, boolean toTreatSemantic, String savePath) throws IOException {
        Vector<Pair<String,Collection<Document>>> id_docsCollection= new Vector<>();
        HashSet<String> citieshash = new HashSet<>(cities);
        readIndexerInfo(path,toStem);
        Vector<Pair<String, String>> queries = ReadFile.readQueriesFile(queriesPath);
        searcher=new Searcher(avgldl,numOfIndexedDocs,path,citieshash,toStem,savePath);//todo !!!!!

        for (int i = 0; i <queries.size() ; i++) {
            Pair <String,String>id_query= queries.get(i);
            String id = id_query.getKey();
            String query = id_query.getValue();
            Collection<Document> queryDocs=  searcher.Search(id, query, toTreatSemantic);
            id_docsCollection.add(new Pair<String,Collection<Document>>(id,queryDocs));
        }
        return id_docsCollection;
    }
    /**
     * This function is the main function of the program.
     */
    /**
     *
     * @param path of posting files
     * @param cities to choose
     * @param query
     * @param toStem
     * @param toTreatSemantic
     * @param savePath - path to save the results file.
     * @return answers for query
     */
    public Collection<Document> Start(String path, Vector<String> cities, String query, boolean toStem, boolean toTreatSemantic, String savePath){
        HashSet<String> citieshash = new HashSet<>(cities);
        readIndexerInfo(path,toStem);

        searcher=new Searcher(avgldl,numOfIndexedDocs,path,citieshash,toStem,savePath);//todo !!!!!!!!!!!!
        Collection<Document> docs = searcher.Search("1", query, toTreatSemantic);

        return docs;
    }

    private void readIndexerInfo(String path,boolean toStem) {
        try {
            File file=new File(path+"/Details"+toStem+".txt");
            BufferedReader bufferedReader=new BufferedReader(new FileReader(file));
            String line=bufferedReader.readLine();
            String[] details=line.split("%");
            bufferedReader.close();
            avgldl=Double.parseDouble(details[0]);
            numOfIndexedDocs=Integer.parseInt(details[1]);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}

