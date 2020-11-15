package QR_tree;

import common.RectItem;
import geometry.*;
import R_tree.*;

import java.util.*;

public class QRTree<T extends RectItem> {
    private Rectangle globalRegion;
    private int level;
    private QRNode<T> root;
    private int maxCapacity; // 结点容量
    private int minCapacity; // 结点填充因子 ，用于计算每个结点最小条目个数

    public QRTree(Rectangle globalRegion, int level, int maxCapacity, int minCapacity) {
        this.globalRegion = globalRegion;
        this.level = level;
        this.maxCapacity = maxCapacity;
        this.minCapacity = minCapacity;
        root = generateTree(globalRegion, level);
    }

    @SuppressWarnings("unchecked")
    private QRNode<T> generateTree(Rectangle region, int level) {
        RTree<T> rTree = new RTree<>(maxCapacity, minCapacity, 2);
        QRNode<T> node;
        if (level != 0){
            QRNode<T>[] children = new QRNode[4];
            node = new QRNode<>(region, rTree, children);
            Point center = region.getCenter();
            Rectangle[] rectangles = new Rectangle[4];
            rectangles[0] = new Rectangle(region.low.clone(), center.clone());
            rectangles[1] = new Rectangle(new Point(center.data[0], region.low.data[1]),
                    new Point(region.high.data[0], center.data[1]));
            rectangles[2] = new Rectangle(new Point(region.low.data[0], center.data[1]),
                    new Point(center.data[0], region.high.data[1]));
            rectangles[3] = new Rectangle(center.clone(), region.high.clone());
            for (int i = 0; i < 4; i++)
                children[i] = generateTree(rectangles[i], level-1);
        }else {
            node = new QRNode<>(region, rTree, null);
        }
        return node;
    }

    public void insert(T t) {
        if (t == null)
            throw new IllegalArgumentException("Rectangle cannot be null.");
        Elem<T> elem = new Elem<>(t);
        if (!globalRegion.isInternal(elem.MBR))
            throw new IllegalArgumentException("error.");
        root.insert(elem);
    }

    public void delete(T t) {
        if (t == null)
            throw new IllegalArgumentException("Rectangle cannot be null.");
        Elem<T> elem = new Elem<>(t);
        if (!globalRegion.isInternal(elem.MBR))
            throw new IllegalArgumentException("error.");
        root.delete(elem);
    }

    public List<T> search(Rectangle queryRect){
        List<T> list = new ArrayList<>();
        root.search(queryRect, list);
        return list;
    }


}
































