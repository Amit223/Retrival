import ParseObjects.Date;
import ParseObjects.*;
import ParseObjects.Number;

import java.io.File;
import java.io.RandomAccessFile;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class Main {
    public static void main(String[] args) {
    Parser p = new Parser();
    StopWords.setStopWords();
    p.Parse("Speaking at weekly press conference, the spokesman said that \n" +
            "Balladur's China tour is \"an important political visit.\" \n" +
            "  \"It will give major impetus to the further development of \n" +
            "the \n" +
            "bilateral ties between China and France, including economic and \n" +
            "trade relations,\" he said. \n" +
            "  Maintaining good political relations will provide a solid \n" +
            "foundation and favorable conditions for the development of \n" +
            "mutually beneficial economic and trade relations between the two \n" +
            "countries, he said. ",true, "Paris");
    p.printTermList();

    }

}

