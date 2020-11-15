package common.pointIndex;

import R_tree.RTree;
import geometry.Rectangle;
import geometry.TrackPoint;

import java.util.List;

public class RteePointIndex implements PointIndex{
    RTree<TrackPoint> tree;
    double radius;

    public RteePointIndex(double radius){
        tree = new RTree<>(8, 4, 2);
        this.radius = radius;
    }

    @Override
    public void insert(TrackPoint point) {
        tree.insert(point);
    }

    @Override
    public void delete(TrackPoint point) {
        tree.delete(point);
    }

    @Override
    public List<TrackPoint> search(TrackPoint point) {
        Rectangle queryRect = new Rectangle(point.clone(), point.clone()).extendLength(radius);
        return tree.search(queryRect);
    }
}
