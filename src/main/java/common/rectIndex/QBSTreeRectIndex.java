package common.rectIndex;

import QBS_tree.QBSTree;
import geometry.Point;
import geometry.Rectangle;
import geometry.Segment;

import java.util.List;

public class QBSTreeRectIndex implements RectIndex{

    QBSTree<Segment> tree;
    double radius;

    public QBSTreeRectIndex(Rectangle globalRegion, double radius){
        tree = new QBSTree<>(4, 1,11, globalRegion, 0);
        this.radius = radius;
    }

    @Override
    public void insert(Segment segment) {
        tree.insert(segment);
    }

    @Override
    public void delete(Segment segment) {
        tree.delete(segment);
    }

    @Override
    public List<Segment> search(Point point) {
        Rectangle queryRect = new Rectangle(point.clone(), point.clone()).extendLength(radius);
        return tree.rectQuery(queryRect, false);
    }
}
