package R_tree;

import java.util.*;

import common.RectItem;
import geometry.*;


public class RDirNode<T extends RectItem> extends RNode<T> {
    /**
     * 孩子结点集
     */
    RNode<T>[] children;

    // 构造函数
    @SuppressWarnings("unchecked")
    RDirNode(RTree<T> rtree, RDirNode<T> parent, int level) {
        super(rtree, parent, level); // 调用父类的构造函数
        children = new RNode[rtree.getMaxCapacity()+1]; // 新建一个RTNode类型的结点数组
    }

    /**
     * @return 对应索引下的孩子结点
     */
    RNode<T> getChild(int index) {
        return children[index];
    }

    @Override
    /*-->选择叶子结点*/
    public RNode<T> chooseLeaf(Rectangle rectangle, int level) {
        if (level == this.level) {
            return this;
        }else {
            insertIndex = findLeastEnlargement(rectangle); // 获得面积增量最小的结点的索引
            return getChild(insertIndex).chooseLeaf(rectangle, level); // 非叶子节点的chooseLeaf（）实现递归调用
        }
    }

    /**
     * @return -->返回最小重叠面积的结点的索引， 如果重叠面积相等则选择加入此Rectangle后面积增量更小的，
     *         如果面积增量还相等则选择自身面积更小的
     */
    private int findLeastOverlap(Rectangle rectangle) {
        double overlap = Double.POSITIVE_INFINITY;
        int sel = -1;

        for (int i = 0; i < usedSpace; i++) {
            RNode<T> node = getChild(i);
            double ol = 0; // 用于记录每个孩子的datas数据与传入矩形的重叠面积之和
            if (node instanceof RDataNode){
                RDataNode<T> dataNode = (RDataNode<T>) node;
                for (int j = 0; j < dataNode.usedSpace; j++)
                    // 将传入矩形与各个矩形重叠的面积累加到ol中，得到重叠的总面积
                    ol += rectangle.intersectingArea(dataNode.elems[j].MBR);
            }else {
                RDirNode<T> dirNode = (RDirNode<T>) node;
                for (int j = 0; j < dirNode.usedSpace; j++)
                    // 将传入矩形与各个矩形重叠的面积累加到ol中，得到重叠的总面积
                    ol += rectangle.intersectingArea(dirNode.children[i].MBR);
            }
            if (ol < overlap) {
                overlap = ol;// 记录重叠面积最小的
                sel = i;// 记录第几个孩子的索引
            }
            // 如果重叠面积相等则选择加入此Rectangle后面积增量更小的,如果面积增量还相等则选择自身面积更小的
            else if (ol == overlap) {
                double area1 = children[i].MBR.getUnionRectangle(rectangle).getArea() - children[i].MBR.getArea();
                double area2 = children[sel].MBR.getUnionRectangle(rectangle).getArea() - children[sel].MBR.getArea();
                if (area1 == area2)
                    sel = (children[sel].MBR.getArea() <= children[i].MBR.getArea()) ? sel : i;
                else
                    sel = (area1 < area2) ? i : sel;
            }
        }
        return sel;
    }

    /**
     * @return -->面积增量最小的结点的索引，如果面积增量相等则选择自身面积更小的
     */
    private int findLeastEnlargement(Rectangle rectangle) {
        double area = Double.POSITIVE_INFINITY; // double类型的正无穷
        int sel = -1;

        for (int i = 0; i < usedSpace; i++) {
            // 增量enlargement = 包含（datas[i]里面存储的矩形与查找的矩形）的最小矩形的面积 -
            // datas[i]里面存储的矩形的面积
            double enlargement = children[i].MBR.getUnionRectangle(rectangle).getArea() - children[i].MBR.getArea();
            if (enlargement < area) {
                area = enlargement; // 记录增量
                sel = i; // 记录引起增量的【包含（datas[i]里面存储的矩形与查找的矩形）的最小矩形】里面的datas[i]的索引
            } else if (enlargement == area) {
                sel = (children[sel].MBR.getArea() < children[i].MBR.getArea()) ? sel : i;
            }
        }

        return sel;
    }

    /**
     * --> 插入新的Rectangle后从插入的叶节点开始向上调整RTree，直到根节点
     *
     * @param node1
     *            引起需要调整的孩子结点
     * @param node2
     *            分裂的结点，若未分裂则为null
     */
    void adjustTree(RNode<T> node1, RNode<T> node2) {
        // 先要找到指向原来旧的结点（即未添加Rectangle之前）的条目的索引
        children[insertIndex] = node1;// 替换旧的结点
        node1.MBR = node1.getNodeRectangle();// 先用node1覆盖原来的结点
        if (node2 != null)
            insert(node2);// 插入新的结点
        // 还没到达根节点
        else if (!isRoot()) {
            parent.adjustTree(this, null);// 向上调整直到根节点
        }else {
            MBR = getNodeRectangle();
        }
    }

    /**
     * -->非叶子节点插入
     *
     */
    private void insert(RNode<T> node) {
        node.MBR = node.getNodeRectangle();
        // 已用结点小于树的节点容量，不需分裂，只需插入以及调整树
        if (usedSpace < rtree.getMaxCapacity()) {
            children[usedSpace++] = node;// 新加的
            node.parent = this;// 新加的
            if (parent != null) // 不是根节点
                parent.adjustTree(this, null);
        } else {// 非叶子结点需要分裂
            RDirNode<T>[] a = splitIndex(node);
            RDirNode<T> n = a[0];
            RDirNode<T> nn = a[1];
            if (isRoot()) {
                // 新建根节点，层数加1
                RDirNode<T> newRoot = new RDirNode<>(rtree, null, level + 1);
                // 把两个分裂的结点n和nn添加到根节点
                n.MBR = n.getNodeRectangle();
                nn.MBR = nn.getNodeRectangle();
                MBR = n.MBR.getUnionRectangle(nn.MBR);
                newRoot.usedSpace = 2;
                newRoot.children[0] = n;
                newRoot.children[1] = nn;
                // 设置两个分裂的结点n和nn的父节点
                n.parent = newRoot;
                nn.parent = newRoot;
                // 最后设置rtree的根节点
                rtree.setRoot(newRoot);// 新加的
            } else {
                // 如果不是根结点，向上调整树
                parent.adjustTree(n, nn);
            }
        }
    }

    /**
     * -->非叶子结点的分裂
     */
    @SuppressWarnings("unchecked")
    private RDirNode<T>[] splitIndex(RNode<T> node) {
        int[][] group;
        group = quadraticSplit(node.MBR);
        children[usedSpace] = node;// 新加的
        // 新建两个非叶子节点
        RDirNode<T> index1 = new RDirNode<>(rtree, parent, level);
        RDirNode<T> index2 = new RDirNode<>(rtree, parent, level);
        // 为index1添加数据和孩子
        for (int value : group[0]) {
            index1.children[index1.usedSpace++] = children[value];// 新加的
            // 让index1成为其父节点
            children[value].parent = index1;// 新加的
        }
        for (int value : group[1]) {
            index2.children[index2.usedSpace++] = children[value];// 新加的
            children[value].parent = index2;// 新加的
        }
        return new RDirNode[] { index1, index2 };
    }

    @Override
    // 寻找叶子
    protected RDataNode<T> findLeaf(Elem<T> elem) {
        for (int i = 0; i < usedSpace; i++) {
            if (children[i].MBR.isInternal(elem.MBR)) {
                deleteIndex = i;// 记录搜索路径
                RDataNode<T> leaf = children[i].findLeaf(elem); // 递归查找
                if (leaf != null)
                    return leaf;
            }
        }
        return null;
    }

    void search(Rectangle queryRect, List<T> list){
        for (int i = 0; i < usedSpace; i++) {
             if (queryRect.isIntersection(children[i].MBR))
                children[i].search(queryRect, list);
        }
    }



    public Rectangle[] getMBRs(Rectangle[] rectangles){
        for (int i = 0; i < usedSpace; i++)
            rectangles[i] = children[i].MBR;
        return rectangles;
    }


    void deleteData(int i){
        if (children[i + 1] != null){ // 如果为中间节点（非尾节点），采用拷贝数组的方式链接条目
            System.arraycopy(children, i + 1, children, i, usedSpace - i - 1);
            children[usedSpace - 1] = null;
        } else { // 如果为末尾节点，将节点置空
            children[i] = null;
        }
        // 删除之后已用节点自减
        usedSpace--;
    }

    void reInsert(){
        for (int i = 0; i < usedSpace; i++) {
            RNode<T> node = rtree.chooseLeaf(children[i].MBR, level);
            if (node instanceof RDataNode)
                throw new IllegalArgumentException("error");
            RDirNode<T> insertNode = (RDirNode<T>) node;
            insertNode.insert(children[i]);
        }
    }

    boolean check(){
        super.check();
        for (int i = 0; i < usedSpace; i++) {
            if (children[i].level + 1 != level)
                throw new IllegalArgumentException("error.");
        }
        return true;
    }

    int size(){
        int size = 0;
        for (int i = 0; i < usedSpace; i++)
            size += children[i].size();
        return size;
    }
}
































