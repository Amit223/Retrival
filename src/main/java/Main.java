
import javafx.util.Pair;

public class Main {
    public static void main(String[] args) {
       StopWords.setStopwords("C:\\Users\\עופר\\Desktop\\C semester 1\\אחזור\\עזרים\\מנוע חיפוש עמית וליעד\\corpus");
       Parser p = new Parser();
       p.Parse("kol----liad-reut-1km-----1,000",true,"pariz");
       p.printTermList();
    }
}