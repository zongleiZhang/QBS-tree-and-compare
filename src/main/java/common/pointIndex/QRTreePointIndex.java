package common.pointIndex;

import QR_tree.QRTree;
import geometry.Rectangle;
import geometry.TrackPoint;

import java.util.List;

public class QRTreePointIndex implements PointIndex {
    QRTree<TrackPoint> tree;
    double radius;

    public QRTreePointIndex(Rectangle globalRegion, double radius){
        tree = new QRTree<>(globalRegion, 3, 8, 4);
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