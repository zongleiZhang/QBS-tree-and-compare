package common.rectIndex;

import geometry.Point;
import geometry.Segment;

import java.util.List;

public interface RectIndex {
    void insert(Segment segment);
    void delete(Segment segment);
    List<Segment> search(Point point);
}
