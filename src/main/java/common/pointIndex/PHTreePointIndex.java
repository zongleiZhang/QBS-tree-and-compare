package common.pointIndex;

import geometry.Rectangle;
import geometry.TrackPoint;
import org.tinspin.index.phtree.PHTreeP;

import java.util.ArrayList;
import java.util.List;

public class PHTreePointIndex implements PointIndex {

    PHTreeP<List<TrackPoint>> tree;
    double radius;
/*

    //3维
    public PHTreePointIndex(double radius){
        tree =  PHTreeP.createPHTree(3);
        this.radius = radius;
    }

    @Override
    public void insert(TrackPoint point) {
        double[] key = get3DKey(point);
        List<TrackPoint> value = tree.queryExact(key);
        if (value == null){
            value = new ArrayList<>(4);
            value.add(point);
            tree.insert(key, value);
        }else {
            value.add(point);
        }

    }

    @Override
    public void delete(TrackPoint point) {
        double[] key = get3DKey(point);
        List<TrackPoint> value = tree.queryExact(key);
        if (value != null){
            value.remove(point);
            if (value.size() == 0)
                tree.remove(key);
        }
    }

    @Override
    public List<TrackPoint> search(TrackPoint point) {
        Rectangle rect = new Rectangle(point.clone(), point.clone()).extendLength(radius);
        double[] lowKey  = new double[]{rect.low.data[0],  rect.low.data[1],  0};
        double[] highKey = new double[]{rect.high.data[0], rect.high.data[1], System.currentTimeMillis()};
        List<TrackPoint> list = new ArrayList<>();
        tree.query(lowKey, highKey).forEachRemaining(entry -> list.addAll(entry.value()));
        return list;
    }

    private double[] get3DKey(TrackPoint point){
        double[] doubles = new double[3];
        doubles[0] = point.data[0];
        doubles[1] = point.data[1];
        doubles[2] = point.timestamp;
        return doubles;
    }
*/

    //2维
    public PHTreePointIndex(double radius){
        tree = PHTreeP.createPHTree(2);
        this.radius = radius;
    }

    @Override
    public void insert(TrackPoint point) {
        List<TrackPoint> value = tree.queryExact(point.data);
        if (value == null){
            value = new ArrayList<>(4);
            value.add(point);
            tree.insert(point.data, value);
        }else {
            value.add(point);
        }
    }

    @Override
    public void delete(TrackPoint point) {
        List<TrackPoint> value = tree.queryExact(point.data);
        if (value != null){
            value.remove(point);
            if (value.size() == 0)
                tree.remove(point.data);
        }
    }

    @Override
    public List<TrackPoint> search(TrackPoint point) {
        Rectangle rect = new Rectangle(point.clone(), point.clone()).extendLength(radius);
        List<TrackPoint> list = new ArrayList<>();
        tree.query(rect.low.data, rect.high.data).forEachRemaining(entry -> list.addAll(entry.value()));
        return list;
    }
}
