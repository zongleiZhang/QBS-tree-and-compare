import common.pointIndex.*;
import common.rectIndex.*;
import geometry.*;
import jdk.nashorn.internal.ir.debug.ObjectSizeCalculator;

import java.io.*;
import java.util.*;

public class App {

    private static volatile boolean flag = true;
    private static final LinkedList<TrackPoint> pointBuffer = new LinkedList<>();
    private static final LinkedList<Segment> segmentBuffer = new LinkedList<>();
//    北京出租车
    private static final Rectangle globalRegion = new Rectangle(new Point(0.0,0.0), new Point(1929725.6050, 1828070.4620));
    //成都滴滴
//    private static final Rectangle globalRegion = new Rectangle(new Point(0.0,0.0), new Point(8626.0,8872.0));
    private static String inputDir;
    private static String outputFile;
    private static String configFile;
    private static String tree_type;  //树类型
    private static PointIndex pointIndex;
    private static RectIndex rectIndex;
    private static int tree_size;  //树的规模
    private static double radius; //查询矩形的大小
    private static int ratio; //查询和更新的比例
    private static int times = 5000;
    private static int indexType = 1; //0:点索引， 1：矩形索引
    private static Random random = new Random(0);
    private static final ReadPointThread READ_POINT_THREAD = new ReadPointThread();
    private static final TestPointIndexThread TEST_POINT_INDEX_THREAD = new TestPointIndexThread();
    private static final ReadSegmentThread READ_SEGMENT_THREAD = new ReadSegmentThread();
    private static final TestRectIndexThread TEST_RECT_INDEX_THREAD = new TestRectIndexThread();

    public static void main(String[] args){
        System.out.println(args[0]);
        configInit(Integer.parseInt(args[0]));
        if (tree_type.length() < 2)
            return;
        if (indexType == 0) {
            READ_POINT_THREAD.start();
            TEST_POINT_INDEX_THREAD.start();
        }else {
            READ_SEGMENT_THREAD.start();
            TEST_RECT_INDEX_THREAD.start();
        }
    }

    private static class ReadSegmentThread extends Thread{

        @Override
        public void run(){
            try {
                Iterator<File> fs = Arrays.asList(Objects.requireNonNull(new File(inputDir).listFiles())).iterator();
                BufferedReader br = new BufferedReader(new FileReader(fs.next()));
                Map<Integer, TrackPoint> map = new HashMap<>();
                String line;
                int count = 0;
                while (flag) {
                    if (pointBuffer.size() < 200000) {
                        for (int i = 0; i < 10000; i++) {
                            if ((line = br.readLine()) != null) {
                                String[] split = line.split("\t");
                                TrackPoint curPoint = new TrackPoint(new double[]{Double.parseDouble(split[2]), Double.parseDouble(split[3])},
                                        Long.parseLong(split[1]),
                                        Integer.parseInt(split[0]));
                                TrackPoint prePoint = map.get(curPoint.TID);
                                if (prePoint == null){
                                    map.put(curPoint.TID, curPoint);
                                }else {
                                    if (java.awt.Point.distance(curPoint.data[0], curPoint.data[1], prePoint.data[0], prePoint.data[1]) > 600.0){
                                        map.replace(curPoint.TID, prePoint, curPoint);
                                    }else {
                                        segmentBuffer.add(new Segment(prePoint, curPoint));
                                    }
                                }
                                count++;
                                if (count == 100000){
                                    count = 0;
                                    map.entrySet().removeIf(entry -> entry.getValue().timestamp - curPoint.timestamp > 1000*60*3);
                                }
                            } else {
                                br.close();
                                if (fs.hasNext()){
                                    br = new BufferedReader(new FileReader(fs.next()));
                                }else {
                                    throw new DataFinal();
                                }
                            }
                        }
                    }
                }
            } catch (DataFinal e){
                System.out.println("Data Final.");
            }catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static class TestRectIndexThread extends Thread{
        @Override
        public void run(){
            try {
                Thread.sleep(3000);
                Queue<Segment> queue = new LinkedList<>();
                int count = 0, size_num = 0;
                long size = 0;
                while (true) {
                    if (count <= tree_size) {
                        Segment segment = segmentBuffer.remove();
                        rectIndex.insert(segment);
                        queue.add(segment);
                        count++;
                    }else {
                        Segment segment = segmentBuffer.remove();
                        rectIndex.insert(segment);
                        queue.add(segment);
                        count++;
                        if (count % 500 == 0){
                            size_num++;
                            size += ObjectSizeCalculator.getObjectSize(rectIndex) - ObjectSizeCalculator.getObjectSize(segment)*tree_size;
                        }
                        if (count == tree_size+times)
                            break;
                        rectIndex.delete(queue.remove());
                    }
                }
                flag = false;
                BufferedWriter bw = new BufferedWriter(new FileWriter(outputFile, true));
                String sb = tree_type + "\t" +
                        tree_size + "\t";
                bw.write(sb + (size/size_num));
                System.out.println(sb + (size/size_num));
                bw.newLine();
                bw.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static class ReadPointThread extends Thread{

        @Override
        public void run(){
            try {
                Iterator<File> fs = Arrays.asList(Objects.requireNonNull(new File(inputDir).listFiles())).iterator();
                BufferedReader br = new BufferedReader(new FileReader(fs.next()));
                String line;
                while (flag) {
                    if (pointBuffer.size() < 400000) {
                        for (int i = 0; i < 40000; i++) {
                            if ((line = br.readLine()) != null) {
                                String[] split = line.split("\t");
                                TrackPoint point = new TrackPoint(new double[]{Double.parseDouble(split[2]), Double.parseDouble(split[3])},
                                        Long.parseLong(split[1]),
                                        Integer.parseInt(split[0]));
                                pointBuffer.add(point);
                            } else {
                                br.close();
                                if (fs.hasNext()){
                                    br = new BufferedReader(new FileReader(fs.next()));
                                }else {
                                    throw new DataFinal();
                                }
                            }
                        }
                    }
                }
            } catch (DataFinal e){
                System.out.println("Data Final.");
            }catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static class TestPointIndexThread extends Thread{
        @Override
        public void run(){
            try {
                Thread.sleep(3000);
                Queue<TrackPoint> queue = new LinkedList<>();
                int count = 0, size_num = 0;
                long size = 0;
                while (true) {
                    if (count <= tree_size) {
                        TrackPoint point = pointBuffer.remove();
                        pointIndex.insert(point);
                        queue.add(point);
                        count++;
                    }else {
                        TrackPoint point = pointBuffer.remove();
                        pointIndex.insert(point);
                        queue.add(point);
                        count++;
                        if (count % 500 == 0){
                            size_num++;
                            size += ObjectSizeCalculator.getObjectSize(pointIndex) - ObjectSizeCalculator.getObjectSize(point)*tree_size;
                        }
                        if (count == tree_size+times)
                            break;
                        pointIndex.delete(queue.remove());
                    }
                }
                flag = false;
                BufferedWriter bw = new BufferedWriter(new FileWriter(outputFile, true));
                String sb = tree_type + "\t" +
                        tree_size + "\t";
                bw.write(sb + (size/size_num));
                System.out.println(sb + (size/size_num));
                bw.newLine();
                bw.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static class DataFinal extends Exception{}

    private static void configInit(int num){
        try {
            String os = System.getProperty("os.name");
            if (os.startsWith("Windows")){
//                inputDir = "D:\\研究生资料\\论文\\track_data\\北京出租车\\merge\\convert";
                inputDir = "D:\\研究生资料\\论文\\track_data\\成都滴滴\\Sorted_2D\\";
                outputFile = "D:\\研究生资料\\论文\\my paper\\MyPaper\\分布式空间索引\\投递期刊\\Data\\local\\SingleNodeTree.txt";
                configFile = "D:\\研究生资料\\论文\\my paper\\MyPaper\\分布式空间索引\\投递期刊\\Data\\local\\config.txt";
            }
            if (os.startsWith("Linux")){
                //北京出租车
                inputDir = "/home/chenliang/data/didi/Bei_Jing";

                //成都滴滴
//                inputDir = "/home/chenliang/data/didi/Cheng_Du/Sorted_2D";
                outputFile = "/home/chenliang/data/zzl/SingleNodeTree.txt";
                configFile = "/home/chenliang/data/zzl/config.txt";
            }
            BufferedReader br = new BufferedReader(new FileReader(configFile));
            int curNum = 0;
            String line;
            while ((line = br.readLine()) != null){
                String[] configs = line.split("\t");
                if (configs.length == 4 && !configs[0].equals("tree_type")){
                    if (curNum == num){
                        System.out.println(line);
                        tree_type = configs[0];
                        tree_size = Integer.parseInt(configs[1]);
                        radius = Double.parseDouble(configs[2]);
                        ratio = Integer.parseInt(configs[3]);
                        if (indexType == 0) {
                            switch (tree_type) {
                                case "PHTree":
                                    pointIndex = new PHTreePointIndex(radius);
                                    break;
                                case "QBSTree":
                                    pointIndex = new QBSTreePointIndex(globalRegion, radius);
                                    break;
                                case "RTree":
                                    pointIndex = new RteePointIndex(radius);
                                    break;
                                case "QRTree":
                                    pointIndex = new QRTreePointIndex(globalRegion, radius);
                                    break;
                                default:
                                    throw new IllegalArgumentException();
                            }
                        }else {
                            switch (tree_type) {
                                case "PHTree":
                                    rectIndex = new PHTreeRectIndex(radius);
                                    break;
                                case "QBSTree":
                                    rectIndex = new QBSTreeRectIndex(globalRegion, radius);
                                    break;
                                case "RTree":
                                    rectIndex = new RteeRectIndex(radius);
                                    break;
                                case "QRTree":
                                    rectIndex = new QRTreeRectIndex(globalRegion, radius);
                                    break;
                                default:
                                    throw new IllegalArgumentException();
                            }
                        }
                        br.close();
                        return;
                    }
                    curNum++;
                }
            }
            br.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
