

import javafx.util.Pair;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class Main {


    public static void main(String[] args) {
        Path path= Paths.get("D:\\documents\\users\\ammo\\queries.txt");
        try {
            Vector<Pair<String,String>> answer=ReadFile.readQueriesFile(path);
            System.out.println("d");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}