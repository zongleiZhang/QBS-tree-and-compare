package geometry;

import common.Constants;

import java.io.Serializable;
import java.util.Arrays;

public class Point implements Serializable, Cloneable {
    public double[] data;

    public Point(){}

    public Point(double[] data){
        this.data = data;
    }

    public Point(double x, double y){
        this.data = new double[]{x,y};
    }

    public double[] getData() {
        return data;
    }

    public void setData(double[] data) {
        this.data = data;
    }

    @Override
    public Point clone()  {
        Point point = null;
        try {
            point = (Point) super.clone();
            if (data != null)
                point.data = data.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return point;
    }

    @Override
    public String toString() {
        return Constants.df.format(data[0]) + " " + Constants.df.format(data[1]);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Point)) return false;
        Point point = (Point) o;
        return Constants.isEqual(data[0], point.data[0]) && Constants.isEqual(data[1], point.data[1]);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(getData());
    }

    public double distancePoint(Point trackPoint) {
        return  Math.sqrt(Math.pow(this.data[0] - trackPoint.data[0],2.0) + Math.pow(this.data[1]- trackPoint.data[1],2.0));
    }

    /**
     * @return 返回Point的维度
     */
    public int getDimension() {
        return 2;
    }

    /**
     * @return 返回Point坐标第i位的float值
     */
    public double getFloatCoordinate(int index) {
        return data[index];
    }

    /**
     * @return 返回Point坐标第i位的int值
     */
    public int getIntCoordinate(int index) {
        return (int) data[index];
    }



}
