import common.ClassMct;
import geometry.Point;
import geometry.Rectangle;

import java.io.*;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class TextSize {

    private final static DecimalFormat df = new DecimalFormat("#.0000");

    public static void main(String[] args) throws IOException{
//        new Rectangle(new Point(105.0,30.0), new Point(125.0, 45.0))
//        File rf = new File("D:\\研究生资料\\论文\\track_data\\北京出租车\\merge\\merge.txt");
        File rf = new File("D:\\研究生资料\\论文\\track_data\\北京出租车\\merge\\merge.txt");
        File wf = new File("D:\\研究生资料\\论文\\track_data\\北京出租车\\merge\\convert");
        if (!wf.exists())
            wf.createNewFile();
        BufferedReader br = new BufferedReader(new FileReader(rf));
        BufferedWriter bw = new BufferedWriter(new FileWriter(wf));
        String str;
        while ((str = br.readLine()) != null){
            PP p = new PP(str);
            if (p.f) {
                bw.write(p.toString());
                bw.newLine();
            }
        }
        br.close();
        bw.close();
    }

    public static class PP {
        private static final Rectangle globalRegion = new Rectangle(new Point(0.0, 0.0), new Point(1929725.6050, 1828070.4620));
        public String str;
        public boolean f;
        public Date timestamp;
        public String ID;
        public double[] data;

        public PP(String str){
            this.str = str;
            f = true;
            String[] dev = str.split(",");
            if (dev.length != 4) {
                f = false;
                return;
            }
            ID = dev[0];
            data = ClassMct.LBToXY(Double.parseDouble(dev[3]), Double.parseDouble(dev[2]));
            try {
                if (!globalRegion.isInternal(new Point(data))) {
                    throw new IllegalArgumentException();
                }
                this.timestamp = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").parse(dev[1]);
            }catch (Exception e){
                f = false;
            }
        }

        @Override
        public String toString() {
            return ID + "\t" + timestamp.getTime() + "\t" + df.format(data[0]) + "\t" + df.format(data[1]);
        }
    }

//    private static void testSize() {
//        System.out.println(ObjectSizeCalculator.getObjectSize(a));
//        System.out.println(ObjectSizeCalculator.getObjectSize(b));
//    }
}
