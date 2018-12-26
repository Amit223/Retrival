import java.util.Collection;
import javax.xml.stream.events.StartDocument;
import java.io.*;

import java.util.HashSet;
import java.util.Vector;

public class Model_2 {

    private Searcher searcher;
    private double avgldl;
    private int numOfIndexedDocs;
    /**
     * This function is the main function of the program.
     */

    public Collection<Integer> Start(String path, Vector<String> cities, String query, boolean toStem, boolean toTreatSemantic){
        HashSet<String> citieshash = new HashSet<>(cities);
        StopWords.setStopwords(ReadFile.readStopWords("d:\\documents\\users\\liadber\\Downloads\\corpus")); //todo fix
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

