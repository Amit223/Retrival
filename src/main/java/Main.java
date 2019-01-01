

import javafx.util.Pair;

import java.io.*;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class Main {


    public static void main(String[] args) {
        int k=3;
        byte[] d=toBytes(k);
        System.out.println(byteToInt(d));
    }

    public static void test(){
        try {
            RandomAccessFile raf=new RandomAccessFile("D:\\documents\\users\\ammo\\posting true\\dtrueDone.txt", "r");
            byte[] line=new byte[4];
            byte[] tf=new byte[4];
            raf.seek(0);
            System.out.println(raf.readInt());
            raf.seek(4);
            System.out.println(raf.readInt());
            raf.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static byte[] toBytes(int input)
    {
        byte[] b =BigInteger.valueOf(input).toByteArray();
        if(b.length<4){
            byte[] returned=new byte[4];
            for(int i=0;i<4;i++){
                if(i<b.length)
                    returned[3-i]=b[b.length-1-i];
                else
                    returned[3-i]=0;
            }
            return returned;
        }
        else{
            return b;

        }


    }

    private static String convertByteToString(byte[] name) {
        String s=new String(name, Charset.forName("UTF-8"));
        String out="";
        boolean flag=true;
        for (int i = 0; i < s.length()&&flag; i++) {
            if(s.charAt(i)!='#')
                out=out+s.charAt(i);
            else flag=false;
        }
        return out;
    }
    private static void writeSonPosting(){
        try {
            RandomAccessFile raf = new RandomAccessFile("D:\\documents\\users\\ammo\\posting true\\strueDone.txt", "r");
            File file = new File("SonPosting.txt");
            file.createNewFile();
            BufferedWriter writer = new BufferedWriter(new FileWriter(file));
            byte[] tf=new byte[4];
            byte[]line=new byte[4];
            for(int i=-1;i<19392;i++){
                raf.seek((i+276776)*8);
                raf.read(line);
                raf.seek((i+276776)*8+4);
                raf.read(tf);
                int t_f=byteToInt(tf);
                int line_num=byteToInt(line);
                writer.write("Line num:"+line_num+" , tf:"+t_f);
                writer.newLine();
            }
            writer.flush();
            writer.close();
            raf.close();

        }
        catch (Exception e){

        }
    }
    private static void writeImpactPosting(){
        try {
            RandomAccessFile raf = new RandomAccessFile("D:\\documents\\users\\ammo\\posting true\\itrueDone.txt", "r");
            File file = new File("impactPosting2.txt");
            file.createNewFile();
            BufferedWriter writer = new BufferedWriter(new FileWriter(file));
            byte[] tf=new byte[4];
            byte[]line=new byte[4];
            for(int i=-1;i<19392;i++){
                raf.seek((i+276776)*8);
                int line_num=raf.readInt();
                raf.seek((i+276776)*8+4);
                int t_f=raf.readInt();
                writer.write("Line num:"+line_num+" , tf:"+t_f);
                writer.newLine();
            }
            writer.flush();
            writer.close();
            raf.close();
        }
        catch (Exception e){

        }
    }
    private static int byteToInt(byte[] bytes) {
        String s2="";
        for (int i=0; i<4; i++){
            String x = String.format("%8s", Integer.toBinaryString(bytes[i] & 0xFF)).replaceAll("\\s","");
            for (int j=8-x.length(); j>0; j--){
                s2 =s2 + "0";
            }
            s2 = s2  +  x;
        }
        s2 = s2.replaceAll("\\s","");
        int foo = Integer.parseInt(s2, 2);
        return foo;
    }
    private static void ll(){
        try {
            RandomAccessFile raf=new RandomAccessFile("D:\\documents\\users\\ammo\\posting true\\Documents.txt","r");
            File file=new File("docs.txt");
            file.createNewFile();
            BufferedWriter writer=new BufferedWriter(new FileWriter(file));
            byte[] allFile=new byte[(int) raf.length()];
            raf.seek(0);
            raf.read(allFile);
            for (int i = 0; i <allFile.length ; i+=54) {
                byte[]name=new byte[16];
                name[0]=allFile[i];
                name[1]=allFile[i+1];
                name[2]=allFile[i+2];
                name[3]=allFile[i+3];
                name[4]=allFile[i+4];
                name[5]=allFile[i+5];
                name[6]=allFile[i+6];
                name[7]=allFile[i+7];
                name[8]=allFile[i+8];
                name[9]=allFile[i+9];
                name[10]=allFile[i+10];
                name[11]=allFile[i+11];
                name[12]=allFile[i+12];
                name[13]=allFile[i+13];
                name[14]=allFile[i+14];
                name[15]=allFile[i+15];
                String theName=convertByteToString(name);
                writer.write(theName);
                writer.newLine();

            }
            writer.flush();
            writer.close();
            raf.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}