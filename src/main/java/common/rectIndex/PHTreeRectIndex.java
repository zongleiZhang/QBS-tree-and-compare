package common.rectIndex;

import geometry.Point;
import geometry.Rectangle;
import geometry.Segment;
import org.tinspin.index.phtree.PHTreeR;

import java.util.ArrayList;
import java.util.List;

public class PHTreeRectIndex implements RectIndex{


    PHTreeR<List<Segment>> tree;
    double radius;
    /*
    //3维
    public PHTreeRectIndex(double radius){
        tree = PHTreeR.createPHTree(3);
        this.radius = radius;
    }

    @Override
    public void insert(Segment segment) {
        double[] low = new double[]{segment.rect.low.data[0], segment.rect.low.data[1], segment.p1.timestamp};
        double[] high = new double[]{segment.rect.high.data[0], segment.rect.high.data[1], segment.p2.timestamp};
        List<Segment> value = tree.queryExact(low, high);
        if (value == null){
            value = new ArrayList<>(4);
            value.add(segment);
            tree.insert(low, high, value);
        }else {
            value.add(segment);
        }
    }

    @Override
    public void delete(Segment segment) {
        double[] low = new double[]{segment.rect.low.data[0], segment.rect.low.data[1], segment.p1.timestamp};
        double[] high = new double[]{segment.rect.high.data[0], segment.rect.high.data[1], segment.p2.timestamp};
        List<Segment> value = tree.queryExact(low, high);
        if (value != null){
            value.remove(segment);
            if (value.size() == 0)
                tree.remove(low, high);
        }
    }

    @Override
    public List<Segment> search(Point point) {
        Rectangle rect = new Rectangle(point.clone(), point.clone()).extendLength(radius);
        double[] low = new double[]{rect.low.data[0], rect.low.data[1], 0};
        double[] high = new double[]{rect.high.data[0], rect.high.data[1], System.currentTimeMillis()};
        List<Segment> list = new ArrayList<>();
        tree.queryIntersect(low, high).forEachRemaining(entry -> list.addAll(entry.value()));
        return list;
    }*/

    //2维
    public PHTreeRectIndex(double radius){
        tree = PHTreeR.createPHTree(2);
        this.radius = radius;
    }

    @Override
    public void insert(Segment segment) {
        List<Segment> value = tree.queryExact(segment.rect.low.data, segment.rect.high.data);
        if (value == null){
            value = new ArrayList<>(4);
            value.add(segment);
            tree.insert(segment.rect.low.data, segment.rect.high.data, value);
        }else {
            value.add(segment);
        }
    }

    @Override
    public void delete(Segment segment) {
        List<Segment> value = tree.queryExact(segment.rect.low.data, segment.rect.high.data);
        if (value != null){
            value.remove(segment);
            if (value.size() == 0)
                tree.remove(segment.rect.low.data, segment.rect.high.data);
        }
    }

    @Override
    public List<Segment> search(Point point) {
        Rectangle rect = new Rectangle(point.clone(), point.clone()).extendLength(radius);
        List<Segment> list = new ArrayList<>();
        tree.queryIntersect(rect.low.data, rect.high.data).forEachRemaining(entry -> list.addAll(entry.value()));
        return list;
    }
}
