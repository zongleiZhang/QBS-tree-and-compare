import common.ClassMct;
import geometry.Point;
import geometry.Rectangle;

import java.io.*;
import java.text.DecimalFormat;
import java.util.*;

public class SortData {


    public static void main(String[] args) throws Exception {
        divideHours();
        System.out.println("divideHoursed");
        sort();
    }

    private static String year = "2016";
    private static String month = "11";
    private static String day;
    private static int idCount = 1;
    private static final Rectangle globalRegion = new Rectangle(new Point(0.0,0.0), new Point(8626.0,8872.0));
    private final static DecimalFormat df = new DecimalFormat("#.0000");
//    private static Map<String, Integer> IDMap = new HashMap<>();

    private static void divideHours() {
        for (int i = 1; i <= 5; i++) {
            day = "0" + i;
            File wDir = new File("/home/chenliang/data/didi/sorting" + year + month + day);
            if (!wDir.exists())
                wDir.mkdir();
            File dir = new File("/home/chenliang/data/didi/gps_" + year + month + day);
            Map<String, BufferedWriter> writerMap = new HashMap<>();
            try {
                BufferedReader br = new BufferedReader(new FileReader(dir));
                String line;
                while ((line = br.readLine()) != null) {
                    PP p = new PP(line);
                    if (p.f) {
                        String hour = String.valueOf(p.timestamp.get(Calendar.HOUR_OF_DAY));
                        BufferedWriter bw = writerMap.get(hour);
                        if (bw == null) {
                            File wf = new File(wDir, hour);
                            if (!wf.exists())
                                wf.createNewFile();
                            bw = new BufferedWriter(new FileWriter(wf, false));
                            writerMap.put(hour, bw);
                        }
                        bw.write(p.toString());
                        bw.newLine();
                    }
                }
                writerMap.values().forEach(bw -> {
                    try {
                        bw.flush();
                        bw.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void sort() throws Exception {
        for (int i = 1; i <= 5; i++) {
            day = "0" + i;
            File path = new File("/home/chenliang/data/didi/sorting" + year + month + day);
            File out = new File("/home/chenliang/data/didi/XY_" + year + month + day);
            if (!out.exists())
                out.createNewFile();
            BufferedWriter bw = new BufferedWriter(new FileWriter(out));
            File[] fs = path.listFiles();
            assert fs != null;
            Arrays.sort(fs, Comparator.comparingInt(o -> Integer.parseInt(o.getName())));
            for (File f : fs) {
                BufferedReader br = new BufferedReader(new FileReader(f));
                List<PP> list = new ArrayList<>();
                String line;
                while ((line = br.readLine()) != null) {
                    PP p = new PP(line, Long.parseLong(line.split("\t")[1]));
                    list.add(p);
                }
                br.close();
                Collections.sort(list);
                for (PP pp : list) {
                    bw.write(pp.str);
                    bw.newLine();
                }
            }
            bw.flush();
            bw.close();
        }
    }

    public static class PP implements Comparable<PP> {
        public String str;
        public boolean f;
        public Calendar timestamp;
        public String ID;
        public double[] data;

        public PP(String str, long time) {
            this.str = str;
            timestamp = Calendar.getInstance();
            timestamp.setTimeInMillis(time);
        }

        public PP(String str) {
            this.str = str;
            String[] dev = str.split(",");
            f = true;
            if( dev.length != 5) {
                f = false;
                return;
            }
            try{
                data = ClassMct.LBToXY(Double.parseDouble(dev[4]), Double.parseDouble(dev[3]));
                if (!globalRegion.isInternal(new Point(data))){
                    f = false;
                    return;
                }
//                ID = IDMap.computeIfAbsent(dev[0], s -> idCount++);
                ID = dev[0] + "," + dev[1];
                this.timestamp = Calendar.getInstance();
                this.timestamp.setTimeInMillis(Long.parseLong(dev[2])*1000);
                if ( timestamp.get(Calendar.YEAR) != Integer.parseInt(year) ||
                        timestamp.get(Calendar.MONTH)+1 != Integer.parseInt(month) ||
                        timestamp.get(Calendar.DATE) != Integer.parseInt(day) )
                    f = false;
            }catch (Exception e){
                f = false;
            }
        }

        @Override
        public int compareTo(PP arg0) {
            return Long.compare(timestamp.getTimeInMillis(),arg0.timestamp.getTimeInMillis());
        }

        @Override
        public String toString() {
            return ID + "\t" + timestamp.getTimeInMillis() + "\t" + df.format(data[0])  + "\t" + df.format(data[1]);
        }

    }

}
