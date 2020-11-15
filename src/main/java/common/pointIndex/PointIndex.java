package common.pointIndex;

import geometry.TrackPoint;

import java.util.List;

public interface PointIndex {
    void insert(TrackPoint point);
    void delete(TrackPoint point);
    List<TrackPoint> search(TrackPoint point);
}
