import ParseObjects.Number;

public class Main {
    public static void main(String[] args) {
        String s="1,000";
        System.out.println("should be 1K:"+Number.Parse(s));

        s="1,000,000";
        System.out.println("should be 1M:"+Number.Parse(s));

        s="7 Trillion";
        System.out.println("should be 700B:"+Number.Parse(s));

        s="10,123";
        System.out.println("should be 10.123K:"+Number.Parse(s));

        s="123 Thousand";
        System.out.println("should be 123K:"+Number.Parse(s));

        s="1010.56";
        System.out.println("should be 1.01056K:"+Number.Parse(s));



    }

}
