package QBS_tree;

import common.*;
import geometry.*;

import java.io.Serializable;
import java.util.*;


public class QBSTree<T extends ElemRoot> implements Serializable {

	QBSNode<T> root;

	int upBound;

	int lowBound;

	int balanceFactor;

	int precision;

	List<CacheElem> cache;

	int cacheSize;

	public QBSTree(){}

	/**
	 * initialize the tree
	 * @param lowBound the upper bound of the leaf node's data number.
	 * @param balanceFactor the low bound of the leaf node's data number.
	 */
	public QBSTree(int lowBound, int balanceFactor, int precision, Rectangle centerRegion, int cacheSize) {
		this.lowBound = lowBound;
		this.balanceFactor = balanceFactor;
		this.upBound = 5*lowBound;
		this.precision = precision;
		cache = new ArrayList<>(0);
		this.cacheSize = cacheSize;
		root = new QBSDataNode<>(0, null, -1, centerRegion, null, new ArrayList<>(), 0, this, new ArrayList<>());
	}

	private void addToCache(CacheElem elem) {
		cache.add(elem);
		if(cache.size() >= cacheSize)
			clearCache();
	}

	void setRoot(QBSNode<T> newRoot) {
		root = newRoot;
	}

	private void clearCache(){
		QBSDataNode<T> cur;
		root.depthPreDepthConvert(true);
		//记录需要调整的所有子树
		List<QBSNode<T>> adjustNodes = new ArrayList<>();
		for(int caN1 = 0; caN1 < cache.size(); caN1++) {
			cur = cache.get(caN1).leaf;
			QBSNode<T> node = cur.getMinReassignNodeForCache();
			Path path = new Path(node);
			//之前加入的子树是新加入的子树的子树，新加入的子树的调整可以覆盖之前加入的子树的调整
			for(int nodeN1 = 0; nodeN1 < adjustNodes.size(); nodeN1++) {
				switch(adjustNodes.get(nodeN1).isSameWay(node) ) {
                    case 0:
                        break;
                    case -1:
                        adjustNodes.remove(nodeN1);
                        nodeN1--;
                        break;
					default:
						throw new IllegalArgumentException("clearCache error.");
				}
			}
			//cache中剩余的元素如果在新加入的子树中，也可以被该子树覆盖
			for(int caN2 = caN1+1; caN2 < cache.size(); caN2++) {
				if(cache.get(caN2).path.isSameWay(path)) {
					cache.remove(caN2);
					caN2--;
				}
			}
			adjustNodes.add(node);
		}

		//记录调整后的节点
		List<QBSNode<T>> newNodes = new ArrayList<>();
		for (QBSNode<T> adjustNode : adjustNodes) newNodes.add(adjustNode.reAdjust());
		//更新树中各个节点的深度信息
		for (QBSNode<T> node : newNodes){
			if (node.parent != null)
				node.parent.updateUpperLayerPreDepth(true,-1);
			else
				root = node;
		}
		root.depthPreDepthConvert(false);
		cache.clear();
	}


	public QBSDataNode<T> insert(T elem) {
		if (cacheSize == 0) {
			QBSDataNode<T> leafNode = root.chooseLeafNode(elem);
			leafNode.insert();
			return leafNode;
		} else {
			if (root.region == null) {
				if (elem instanceof RectElem)
					root.region = ((RectElem) elem).rect;
				else
					root.region = new Rectangle(new Point(elem.data.clone()), new Point(elem.data.clone()));
			}
			QBSDataNode<T> leafNode = root.chooseLeafNode(elem);
			if (leafNode.elemNum > upBound)
				addToCache(new CacheElem(leafNode));
			return leafNode;
		}
	}




	public boolean delete(T elem) {
		if (cacheSize == 0) {
			QBSDataNode<T> leafNode = findLeaf(elem);
			if (leafNode != null)
				return leafNode.delete(elem);
			else
				return false;
		} else {
			QBSDataNode<T> leafNode = root.findLeaf(elem);
			if (leafNode == null)
				return false;
			if (!leafNode.elms.remove(elem))
				return false;
			QBSNode<T> node = leafNode;
			while (node != null) {
				node.elemNum--;
				node = node.parent;
			}
			if (elem instanceof RectElem)
				leafNode.updateRegion(((RectElem) elem).rect, 2);
			else {
				leafNode.updateRegion(new Rectangle(new Point(elem.data.clone()), new Point(elem.data.clone())), 2);
			}
			if (leafNode.elemNum < lowBound && leafNode.parent != null) {
				addToCache(new CacheElem(leafNode));
			}
		}
		return true;
	}


	public List<T> pointQuery(Rectangle rectangle) {
		List<T> res = new ArrayList<>();
		List<QBSDataNode<T>> leaves = new ArrayList<>();
		root.queryLeaf(rectangle, leaves);
		for (QBSDataNode<T> leaf : leaves) {
			for (T elm : leaf.elms) {
				if (rectangle.isInternal(elm))
					res.add(elm);
			}
		}
		return res;
	}

	@SuppressWarnings("unchecked")
	public <M extends RectElem> List<M> rectQuery(Rectangle rectangle, boolean isInternal) {
		List<M> res = new ArrayList<>();
		List<QBSDataNode<T>> leaves = new ArrayList<>();
		root.queryLeaf(rectangle, leaves);
		if (isInternal) {
			for (QBSDataNode<T> lef : leaves) {
				QBSDataNode<M> leaf = (QBSDataNode<M>) lef;
				for (M elm : leaf.elms) {
					if (rectangle.isInternal(elm.rect))
						res.add(elm);
				}
			}
		}else {
			for (QBSDataNode<T> lef : leaves) {
				QBSDataNode<M> leaf = (QBSDataNode<M>) lef;
				for (M elm : leaf.elms) {
					if (rectangle.isIntersection(elm.rect))
						res.add(elm);
				}
			}
		}
		return res;
	}


	public boolean check() {
		return root.check();
	}


    private QBSDataNode<T> findLeaf(T elem) {
		if(root.centerRegion.isInternal(elem))
			return root.findLeaf(elem);
		else
			throw new IllegalArgumentException("find leaf error.");

	}


}
