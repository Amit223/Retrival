package ParseObjects;

public class Distance {
    //todo maybe need to add pounds ?

    /**
     *
     * @param distance= number modifier, modifier is- m , km, meter, meters, kilometer, kilometers
     * @return
     */
    public static String Parse(String distance) {
        String[] splitNum = distance.split(" ");
        String modifier= splitNum[splitNum.length-1];//m,km ...
        if(modifier.equalsIgnoreCase("km")||modifier.equalsIgnoreCase("kilometer")||
                modifier.equalsIgnoreCase("kilometers"))
        {
            if(splitNum[0].contains(".")){
                double d=Double.parseDouble(splitNum[0]);
                d=d*1000;
                splitNum[0]=String.valueOf(d);
               // int index=splitNum[0].indexOf(".");
                //String next=splitNum[0].substring(index,splitNum[0].length());
                //splitNum[0]=splitNum[0].substring(0,index)+"000"+next;
            }
            else
                splitNum[0]=splitNum[0]+"000";//multiply by 1000
        }
        String num="";
        for (int i=0;i<splitNum.length-1;i++){
            num=num+ splitNum[i]+" ";
        }
        num=num.substring(0,num.length()-1);
        String dis=Number.Parse(num);
        dis=dis+" m";
        return dis;
    }

    public static boolean isDistance(String nextTerm) {
        return (nextTerm.equalsIgnoreCase("m")
                ||nextTerm.equalsIgnoreCase("meters")
                ||nextTerm.equalsIgnoreCase("meter")
                ||nextTerm.equalsIgnoreCase("km")
                ||nextTerm.equalsIgnoreCase("kilometer")||
                nextTerm.equalsIgnoreCase("kilometers"));
    }
}
