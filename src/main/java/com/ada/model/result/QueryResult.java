package com.ada.model.result;

import geometry.Segment;

import java.io.Serializable;
import java.util.List;

public class QueryResult implements Serializable {
    public long queryID;
    public long timeStamp;
    public List<Segment> list;

    public QueryResult() {
    }

    public QueryResult(long queryID, long timeStamp, List<Segment> list) {
        this.queryID = queryID;
        this.timeStamp = timeStamp;
        this.list = list;
    }
}
