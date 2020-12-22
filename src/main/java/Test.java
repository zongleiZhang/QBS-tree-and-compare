import geometry.Segment;
import geometry.TrackPoint;
import redis.clients.jedis.Jedis;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class Test {

    public static void main(String[] args){
        int[][] intArrys1 = new int[512][512];
        intArrys1[0][1] = 590;
        int[][] intArrys2 = new int[512][512];
        intArrys2[0][1] = 590;
        Jedis jedis = new Jedis("localhost");
        jedis.set("testSegments".getBytes(StandardCharsets.UTF_8), toByteArray(intArrys1));
        int[][] myTestR = (int[][]) toObject(jedis.get("testSegments".getBytes(StandardCharsets.UTF_8)));
        System.out.println(myTestR);
        jedis.del("testSegments".getBytes(StandardCharsets.UTF_8));
        jedis.close();
    }


    static class MyTest implements Serializable{
        int [][] intArrys;

        MyTest(){}

        public MyTest(int[][] intArrys) {
            this.intArrys = intArrys;
        }

        public int[][] getIntArrys() {
            return intArrys;
        }

        public void setIntArrys(int[][] intArrys) {
            this.intArrys = intArrys;
        }
    }

    /**
     * 对象转数组
     * @param obj
     * @return
     */
    public static byte[] toByteArray (Object obj) {
        byte[] bytes = null;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            ObjectOutputStream oos = new ObjectOutputStream(bos);
            oos.writeObject(obj);
            oos.flush();
            bytes = bos.toByteArray ();
            oos.close();
            bos.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return bytes;
    }

    /**
     * 数组转对象
     * @param bytes
     * @return
     */
    public static Object toObject (byte[] bytes) {
        Object obj = null;
        try {
            ByteArrayInputStream bis = new ByteArrayInputStream (bytes);
            ObjectInputStream ois = new ObjectInputStream (bis);
            obj = ois.readObject();
            ois.close();
            bis.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (ClassNotFoundException ex) {
            ex.printStackTrace();
        }
        return obj;
    }
}
