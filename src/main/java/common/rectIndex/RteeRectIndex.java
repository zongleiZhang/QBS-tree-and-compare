package common.rectIndex;

import R_tree.RTree;
import geometry.Point;
import geometry.Rectangle;
import geometry.Segment;

import java.util.List;

public class RteeRectIndex implements RectIndex{
    RTree<Segment> tree;
    double radius;

    public RteeRectIndex(double radius){
        tree = new RTree<>(8, 4, 2);
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
        return tree.search(queryRect);
    }
}
