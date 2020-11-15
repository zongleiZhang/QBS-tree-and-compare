package R_tree;

import java.util.*;

import common.RectItem;
import geometry.*;

public class RTree<T extends RectItem> {
	RNode<T> root; // 根节点
	private int maxCapacity; // 结点容量
	private int minCapacity; // 结点填充因子 ，用于计算每个结点最小条目个数
	private int dimension; // 维度

	public RTree(int maxCapacity, int fillFactor, int dimension) {
		this.minCapacity = fillFactor;
		this.maxCapacity = maxCapacity;
		this.dimension = dimension;
		root = new RDataNode<>(this, null); // 根节点的父节点为NULL
	}

	/**
	 * @return RTree的维度
	 */
	private int getDimension() {
		return dimension;
	}

	/** 设置跟节点 */
	void setRoot(RNode<T> root) {
		this.root = root;
	}

	/**
	 * @return 填充因子
	 */
	int getMinCapacity() {
		return minCapacity;
	}

	/**
	 * @return 返回结点容量
	 */
	int getMaxCapacity() {
		return maxCapacity;
	}


	/**
	 * --> 向Rtree中插入Rectangle 1、先找到合适的叶节点 2、再向此叶节点中插入
	 *
	 */
	public void insert(T t) {
		if (t == null)
			throw new IllegalArgumentException("Rectangle cannot be null.");
		insert(new Elem<>(t));
	}

	public void insert(Elem<T> elem) {
		if (elem.MBR.getHigh().getDimension() != getDimension()) // 矩形维度与树的维度不一致
			throw new IllegalArgumentException("Rectangle dimension different than RTree dimension.");
		RDataNode<T> leaf = (RDataNode<T>) root.chooseLeaf(elem.MBR, 0);
		leaf.insert(elem);
	}

	/**
	 * 从R树中删除Rectangle
	 * <p>
	 * 1、寻找包含记录的结点--调用算法findLeaf()来定位包含此记录的叶子结点L，如果没有找到则算法终止。<br>
	 * 2、删除记录--将找到的叶子结点L中的此记录删除<br>
	 * 3、调用算法condenseTree<br>
	 *
	 */
	public void delete(T t) {
		if (t == null)
			throw new IllegalArgumentException("Rectangle cannot be null.");
		delete(new Elem<>(t));
	}

	public void delete(Elem<T> elem) {
		if (elem.MBR.getHigh().getDimension() != getDimension())
			throw new IllegalArgumentException("Rectangle dimension different than RTree dimension.");
		RDataNode leaf = root.findLeaf(elem);
		if (leaf != null) {
			leaf.delete();
		}
	}

	public List<T> search(Rectangle queryRect){
		List<T> list = new ArrayList<>();
		root.search(queryRect, list);
		return list;
	}

	RNode<T> chooseLeaf(Rectangle MBR, int level) {
		if (root.level < level)
			throw new IllegalArgumentException("error level");
		return root.chooseLeaf(MBR, level);
	}

	public boolean check(){
		if (!root.isRoot())
			throw new IllegalArgumentException("error");
		return root.check();
	}

	public int size(){
		return root.size();
	}
}