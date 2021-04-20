import com.ada.model.result.QueryResult;
import common.ClassMct;
import geometry.Point;
import geometry.Rectangle;
import geometry.Segment;
import jdk.nashorn.internal.objects.annotations.Getter;
import jdk.nashorn.internal.objects.annotations.Setter;

import java.io.*;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class TextSize {

    private final static DecimalFormat df = new DecimalFormat("#.0000");

    public static void main(String[] args) throws Exception{
        Map<Long, List<Segment>> singleNodeMap = new HashMap<>();
        readFile( "D:\\研究生资料\\论文\\my paper\\MyPaper\\分布式空间索引\\投递期刊\\Data\\debug\\SSI_PH\\output_0", singleNodeMap);

    }

    private static void readFile(String path, Map<Long, List<Segment>> map) throws Exception {
        File f = new File(path);
        FileInputStream fis = new FileInputStream(f);
        ObjectInputStream ois = new ObjectInputStream(fis);
        Object o;
        while ((o = ois.readObject()) != null){
            QueryResult result = (QueryResult) o;
            map.put(result.queryID, result.list);
        }
        ois.close();
        fis.close();
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
