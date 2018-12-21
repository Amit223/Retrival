import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class Controller_2 {

    Model_2 model;
    Map<String, Vector<String>> cityDictionary;

    /**
     *sets model
     */
    public void setModel() {
        model = new Model_2();
    }


    /**
     *
     * @param path_from_user
     * @return the languages from corpus
     */
    private Vector<String> getCitys(String path_from_user){
       loadCityDictionaryToMemort(path_from_user);
       Iterator<String> iterator=cityDictionary.keySet().iterator();
       Vector<String> citys=new Vector<>();
       while(iterator.hasNext()){
           citys.add(iterator.next());
       }
       cityDictionary.clear();//to not have memory issues
       return citys;
    }
    /**
     *
     * @param path
     * load the dictionary from disk to memory
     */
    private void loadCityDictionaryToMemort(String path){
        try {
            cityDictionary = new HashMap<>();
            BufferedReader bufferedReader = new BufferedReader(new FileReader(path + "/" + "CityDictionary.txt"));
            String line = bufferedReader.readLine();
            String[] lines = line.split("=");
            for (int i = 0; i < lines.length; i++) {
                String[] pair = lines[i].split("--->");
                if(pair.length==2){
                    String[] values=pair[1].split(",");
                    Vector<String> vector=new Vector();
                    vector.add(values[0].substring(1,values[0].length()));//the country
                    vector.add(values[1]);//the coin
                    vector.add(values[2].substring(0,values[2].length()-1));//population
                    cityDictionary.put(pair[0],vector);
                }

            }
            bufferedReader.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
