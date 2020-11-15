package common.pointIndex;

import QBS_tree.QBSTree;
import geometry.Rectangle;
import geometry.TrackPoint;

import java.util.List;

public class QBSTreePointIndex implements PointIndex {
    QBSTree<TrackPoint> tree;
    double radius;

    public QBSTreePointIndex(Rectangle globalRegion, double radius){
        tree = new QBSTree<>(4, 1,17, globalRegion, 0);
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
        return tree.pointQuery(queryRect);
    }
}
