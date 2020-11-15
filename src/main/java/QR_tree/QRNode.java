package QR_tree;

import common.RectItem;
import geometry.Rectangle;
import R_tree.*;

import java.util.List;

class QRNode<T extends RectItem> {
    private Rectangle region;
    private RTree<T> rTree;
    private QRNode<T>[] children;

    QRNode(Rectangle region, RTree<T> rTree, QRNode<T>[] children) {
        this.region = region;
        this.rTree = rTree;
        this.children = children;
    }

    void  insert(Elem<T> elem) {
        if (children != null){
            for (QRNode<T> child : children) {
                if (child.region.isInternal(elem.MBR)){
                    child.insert(elem);
                    return;
                }
            }
        }
        rTree.insert(elem);
    }

    void delete(Elem<T> elem) {
        if (children != null){
            for (QRNode<T> child : children) {
                if (child.region.isInternal(elem.MBR)){
                    child.delete(elem);
                    return;
                }
            }
        }
        rTree.delete(elem);
    }

    void search(Rectangle queryRect, List<T> list) {
        list.addAll(rTree.search(queryRect));
        if (children != null) {
            for (QRNode<T> child : children) {
                if (child.region.isIntersection(queryRect))
                    child.search(queryRect, list);
            }
        }
    }
}
