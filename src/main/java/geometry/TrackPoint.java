package geometry;

import QBS_tree.ElemRoot;
import common.RectItem;

import java.text.DecimalFormat;
import java.util.*;

public class TrackPoint extends ElemRoot implements Cloneable, RectItem {
	public long timestamp;
	public int TID;

	public TrackPoint(double[] data, long timestamp, int TID) {
		super(data);
		this.timestamp = timestamp;
		this.TID = TID;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	public int getTID() {
		return TID;
	}

	public void setTID(int TID) {
		this.TID = TID;
	}

	public boolean isEmpty(){
		return data == null;
	}

	@Override
	public TrackPoint clone() {
		return (TrackPoint) super.clone();
	}

	@Override
	public String toString() {
		StringBuilder sBuffer = new StringBuilder();
		Calendar timestamp = Calendar.getInstance();
		timestamp.setTimeInMillis(this.timestamp);
		DecimalFormat df = new DecimalFormat("#.0000");
		sBuffer.append(TID).append(" ");
		sBuffer.append(df.format(data[0])).append(" ");
		sBuffer.append(df.format(data[1])).append(" ");
		sBuffer.append(this.timestamp).append(" ");
		sBuffer.append(timestamp.get(Calendar.YEAR)).append("-");
		sBuffer.append(timestamp.get(Calendar.MONTH)+1).append("-");
		sBuffer.append(timestamp.get(Calendar.DAY_OF_MONTH)).append(" ");
		sBuffer.append(timestamp.get(Calendar.HOUR_OF_DAY)).append(":");
		sBuffer.append(timestamp.get(Calendar.MINUTE)).append(":");
		sBuffer.append(timestamp.get(Calendar.SECOND));
		return sBuffer.toString();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof TrackPoint)) return false;
		if (!super.equals(o)) return false;
		TrackPoint point = (TrackPoint) o;
		return super.equals(point) &&
				getTimestamp() == point.getTimestamp() &&
				getTID() == point.getTID();
	}

	@Override
	public int hashCode() {
		return Objects.hash(super.hashCode(), getTimestamp(), getTID());
	}

	@Override
	public Rectangle getRectangle() {
		return new Rectangle(this, this);
	}
}

