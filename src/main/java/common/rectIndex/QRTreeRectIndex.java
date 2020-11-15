package common.rectIndex;

import QR_tree.QRTree;
import geometry.Point;
import geometry.Rectangle;
import geometry.Segment;

import java.util.List;

public class QRTreeRectIndex implements RectIndex{
    QRTree<Segment> tree;
    double radius;

    public QRTreeRectIndex(Rectangle globalRegion, double radius){
        tree = new QRTree<>(globalRegion, 3, 8, 4);
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
