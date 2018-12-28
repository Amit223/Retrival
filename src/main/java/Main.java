
import javafx.util.Pair;
import org.omg.IOP.Encoding;
import sun.awt.Mutex;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class Main {

private static boolean  isSemantic = true;

    private static String treatSemantic(String query) {
        if(isSemantic){
            String queryWithPluses=  queryWithPluses(query);
            try{
                String urlString = "https://api.datamuse.com/words?ml=" + queryWithPluses;
                URL url = new URL(urlString);
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod("GET");
                InputStreamReader iSR =new InputStreamReader((con.getInputStream()));
                BufferedReader bufferedReader =new BufferedReader(iSR);
                String inputLine;
                StringBuffer similiarSemanticWords= new StringBuffer();
                while((inputLine=bufferedReader.readLine())!=null){
                    int index =0;
                    String [] words = inputLine.substring(2).split("\\{");
                    for(String word: words){
                        if(index==15) break;
                        String [] wordStruct=  word.split(",")[0].split(":");
                        similiarSemanticWords.append(wordStruct[1].substring(1).replace('"',' '));
                        index++;
                    }
                }
                bufferedReader.close();
                iSR.close();
                return similiarSemanticWords.toString();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        else return query;
        return query;
    }


    private static String queryWithPluses(String query) {
        String [] words=query.split(" ");
        String queryWithPluses="";
        for (int i = 0; i < words.length ; i++) {
            if(i==0)
                queryWithPluses=words[i];
            else{
                queryWithPluses+= ("+" + words[i]);
            }
        }
        return query;
    }
    private static PriorityQueue<Pair<Integer, Double>> _RankedDocs = new PriorityQueue(new Comparator<Pair<Integer, Double>>() {
        @Override
        public int compare(Pair<Integer, Double> o1, Pair<Integer, Double> o2) {
            if (o1.getValue() <= o2.getValue()) {
                return -1;
            } else //if(o1.getValue()<o2.getValue())
                return 1;
            //else return 0;

        }
    }); // Rank-Doc

     public static void addItem(double rank, Integer doc) {
        if (rank == 0) return;
        if (_RankedDocs.size() > 49) {
            Double lowest = _RankedDocs.peek().getValue();
            if (rank > (lowest)) {
                _RankedDocs.poll();
                _RankedDocs.add(new Pair<Integer, Double>(doc, rank));
            }
        } else _RankedDocs.add(new Pair<Integer, Double>(doc, rank));
    }

    public static void main(String[] args) {

       Searcher searcher=new Searcher(20,472525
               ,"C:\\Users\\AMIT MOSHE\\Desktop\\אוניברסיטה\\סמסטר ה\\אחזור\\tosave",new HashSet<>(),true);
       searcher.Search("it by an economic crisis compounded by the 50 per \n" +
               "cent devaluation of the CFA",false);
    }
    public static int convertByteToInt(byte[] b)
    {

        int pomAsInt = ByteBuffer.wrap(b).getInt();
        return pomAsInt;

    }

    public static byte[] toBytes(int i)
    {
        /**
        byte[] result = new byte[4];

        result[0] = (byte) (i >> 24);
        result[1] = (byte) (i >> 16);
        result[2] = (byte) (i >> 8);
        result[3] = (byte) (i /*>> 0*/
        byte[] pom = ByteBuffer.allocate(4).putInt(i).array();

        return pom;
    }

    private static void FilterDocsByCitys() {
        Vector<String> _chosenCities=new Vector<>();
        HashMap<Integer,String> _doc_termPlusTfs=new HashMap<>();
        _doc_termPlusTfs.put(0,"AAA");
        _doc_termPlusTfs.put(1,"AAA");
        _doc_termPlusTfs.put(2,"AAA");
        String _path="C:\\Users\\AMIT MOSHE\\Desktop\\אוניברסיטה\\סמסטר ה\\אחזור\\tosave";
        try {
            RandomAccessFile raf=new RandomAccessFile(_path+"/Documents.txt","r");
            Vector<Integer> toDelete=new Vector<>();
            Iterator<Integer> iterator=_doc_termPlusTfs.keySet().iterator();
            while(iterator.hasNext()){
                Integer docLine=iterator.next();
                //filter stage
                if(_chosenCities.size()>0) {
                    raf.seek(docLine * 54 + 16);//to get to the city
                    byte[] city_bytes = new byte[16];
                    raf.read(city_bytes);
                    String city  = new String(city_bytes, StandardCharsets.UTF_8);

                    if(city.contains("#"))
                        city = city.substring(0, city.indexOf("#"));
                    if(city.equals("")||!_chosenCities.contains(city)){
                        toDelete.add(docLine);
                    }
                }
            }
            //delete all the toDelete
            for(int i=0;i<toDelete.size();i++){
                int lineNum=toDelete.get(i);
                _doc_termPlusTfs.remove(lineNum);
            }
            iterator=_doc_termPlusTfs.keySet().iterator();//all the remain ones
            //add to each doc it's size
            int size;
            while (iterator.hasNext()){
                int docLine=iterator.next();
                raf.seek(docLine * 54 + 50);//to get to the size
                byte[] length_bytes=new byte[4];
                raf.read(length_bytes);
                size=convertByteToInt(length_bytes);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}