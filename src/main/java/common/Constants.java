package common;

import geometry.Rectangle;

import java.io.*;
import java.text.DecimalFormat;
import java.util.*;

public class Constants implements Serializable {

    /**
     * 定义Double类型的零
     */
    public final static double zero = 0.00001;

    public final static DecimalFormat df = new DecimalFormat("#.00000");

    public static boolean isEqual(double a, double b){
        return Math.abs(a - b) < zero;
    }

    public static int compare(double a, double b){
        double minus = a-b;
        if (Math.abs(minus) > zero)
            return minus > 0?1:-1;
        else
            return 0;
    }

    public static boolean rectangleEqual(Rectangle curRectangle, Rectangle orgRectangle) {
        if (curRectangle == null && orgRectangle == null)
            return true;
        else if (curRectangle == null || orgRectangle == null)
            return false;
        else
            return curRectangle.low.equals(orgRectangle.low) &&
                    curRectangle.high.equals(orgRectangle.high);
    }

}
