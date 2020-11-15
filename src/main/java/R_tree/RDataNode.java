package R_tree;

import java.util.*;

import common.RectItem;
import geometry.*;

public class RDataNode<T extends RectItem> extends RNode<T> {
    Elem<T>[] elems;

    @SuppressWarnings("unchecked")
    RDataNode(RTree<T> rTree, RDirNode<T> parent) {
        super(rTree, parent, 0);
        elems = new Elem[rTree.getMaxCapacity()+1];
    }

    /**
     * -->叶节点中插入Rectangle 在叶节点中插入Rectangle，插入后如果其父节点不为空则需要向上调整树直到根节点；
     * 如果其父节点为空，则是从根节点插入 若插入Rectangle之后超过结点容量则需要分裂结点 【注】插入数据后，从parent处开始调整数据
     *
     */
    void insert(Elem<T> elem) {
        if (usedSpace < rtree.getMaxCapacity()){ // 已用节点小于节点容量
            elems[usedSpace++] = elem;
            if (parent != null)
                // 调整树，但不需要分裂节点，因为 节点小于节点容量，还有空间
                parent.adjustTree(this, null);
        }
        // 超过结点容量
        else {
            RDataNode<T>[] splitNodes = splitLeaf(elem);
            RDataNode<T> l = splitNodes[0];
            RDataNode<T> ll = splitNodes[1];
            if (isRoot()) {
                // 根节点已满，需要分裂。创建新的根节点
                RDirNode<T> rDirNode = new RDirNode<>(rtree, null, level + 1);
                rtree.setRoot(rDirNode);
                // getNodeRectangle()返回包含结点中所有条目的最小Rectangle
                l.MBR = l.getNodeRectangle();
                ll.MBR = ll.getNodeRectangle();
                rDirNode.usedSpace = 2;
                ll.parent = rDirNode;
                l.parent = rDirNode;
                rDirNode.children[0] = l;
                rDirNode.children[1] = ll;
            } else {// 不是根节点
                parent.adjustTree(l, ll);
            }

        }
    }

    /**
     * 叶子节点的分裂 插入Rectangle之后超过容量需要分裂
     */
    @SuppressWarnings("unchecked")
    private RDataNode<T>[] splitLeaf(Elem<T> elem) {
        int[][] group;
        group = quadraticSplit(elem.MBR);
        elems[usedSpace] = elem;
        RDataNode<T> l = new RDataNode<>(rtree, parent);
        RDataNode<T> ll = new RDataNode<>(rtree, parent);
        for (int value : group[0])
            l.elems[l.usedSpace++] = elems[value];
        for (int value : group[1])
            ll.elems[ll.usedSpace++] = elems[value];
        return new RDataNode[] { l, ll };
    }

    @Override
    public RNode<T> chooseLeaf(Rectangle rectangle, int level) {
        insertIndex = usedSpace;// 记录插入路径的索引
        return this;
    }

    /**
     * 从叶节点中删除此条目rectangle
     * <p>
     * 先删除此rectangle，再调用condenseTree()返回删除结点的集合，把其中的叶子结点中的每个条目重新插入；
     * 非叶子结点就从此结点开始遍历所有结点，然后把所有的叶子结点中的所有条目全部重新插入
     *
     */
    void delete() {
        deleteData(deleteIndex);
        // 用于存储被删除的结点包含的条目的链表Q
        List<RNode<T>> deleteEntriesList = new ArrayList<>();
        condenseTree(deleteEntriesList);

        // 重新插入删除结点中剩余的条目
        for (RNode<T> node : deleteEntriesList)
            node.reInsert();
    }

    @Override
    protected RDataNode<T> findLeaf(Elem<T> elem) {
        for (int i = 0; i < usedSpace; i++) {
            if (elems[i].equals(elem)) {
                deleteIndex = i;// 记录搜索路径
                return this;
            }
        }
        return null;
    }

    void search(Rectangle queryRect, List<T> list){
        for (int i = 0; i < usedSpace; i++) {
            if (queryRect.isIntersection(elems[i].MBR))
                list.add(elems[i].data);
        }
    }


    public Rectangle[] getMBRs(Rectangle[] rectangles){
        if (usedSpace >= 0){
            for (int i = 0; i < usedSpace; i++)
                rectangles[i] = elems[i].MBR;
        }
        return rectangles;
    }

    void deleteData(int i){
        if (elems[i + 1] != null){ // 如果为中间节点（非尾节点），采用拷贝数组的方式链接条目
            System.arraycopy(elems, i + 1, elems, i, usedSpace - i - 1);
            elems[usedSpace - 1] = null;
        } else { // 如果为末尾节点，将节点置空
            elems[i] = null;
        }
        // 删除之后已用节点自减
        usedSpace--;
    }

    void reInsert(){
        for (int i = 0; i < usedSpace; i++)
            rtree.insert(elems[i]);
    }

    boolean check(){
        super.check();
        if (level != 0)
            throw new IllegalArgumentException("error.");
        return true;
    }

    int size(){
        return usedSpace;
    }

}






































