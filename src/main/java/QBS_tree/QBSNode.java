package QBS_tree;


import common.*;
import geometry.*;

import java.io.Serializable;
import java.util.*;


public abstract class QBSNode<T extends ElemRoot> implements Serializable {

	public int depth ;

	public QBSDirNode<T> parent;

	public int position;

	public Rectangle centerRegion;

	public Rectangle region;

	public List<Integer> preDepths = new ArrayList<>();

	public int elemNum;

	public QBSTree<T> tree;

	public QBSNode() {}

	public QBSNode(int depth, QBSDirNode<T> parent, int position, Rectangle centerRegion, Rectangle region, List<Integer> preDepths,
				   int elemNum, QBSTree<T> tree) {
		super();
		this.depth = depth;
		this.parent = parent;
		this.position = position;
		this.centerRegion = centerRegion;
		this.region = region;
		this.preDepths = preDepths;
		this.elemNum = elemNum;
		this.tree = tree;
	}

	public void setDepth(int depth) {
		this.depth = depth;
	}

	public QBSDirNode getParent() {
		return parent;
	}

	public void setParent(QBSDirNode parent) {
		this.parent = parent;
	}

	public Rectangle getRegion() {
		return region;
	}

	public void setRegion(Rectangle region) {
		this.region = region;
	}

	abstract QBSDataNode<T> chooseLeafNode(T elem);

	/**
	 * 为查询指定元素可能被存储的叶节点集
	 */
	abstract QBSDataNode<T> findLeaf(T elem);

	/**
	 * 本节点深度发生变化时，导致失衡的最小子树。
	 * @param cache true使用延迟更新，false不使用延迟更新
	 * @param subNode 当cache为false时该参数有效，表示非叶节点的第subNode个子节点的深度发生变化。
	 * @return	没有失衡返回null，否则返回失衡的最小子树。
	 */
	QBSDirNode<T> getUnbalancedNode(boolean cache, int subNode){
		if (this instanceof QBSDataNode){
			if(parent == null)
				return null;
			return parent.getUnbalancedNode(cache, subNode);
		}else {
			QBSDirNode<T> rcDirNode = (QBSDirNode<T>) this;
			int increment = 0;
			boolean isBalance = true;
			if (cache) {
				for(int chN1 = 0; chN1 < rcDirNode.child.length-1 && isBalance; chN1++) {
					List<Integer> L1 = rcDirNode.child[chN1].preDepths;
					for(int chN2 = chN1+1; chN2 < rcDirNode.child.length && isBalance; chN2++) {
						List<Integer> L2 = rcDirNode.child[chN2].preDepths;
						isBalance = Math.abs( L1.get(0) - L2.get(L2.size()-1) ) <= tree.balanceFactor &&
								Math.abs( L1.get(L1.size()-1) - L2.get(0) ) <= tree.balanceFactor;
					}
				}
			}else {
				while (increment < rcDirNode.child[subNode].preDepths.size() && isBalance) {
					isBalance = rcDirNode.isBalance(subNode, true, increment);
					increment++;
				}
			}
			if(!isBalance)
				return rcDirNode;
			List<Integer> list = rcDirNode.calculateDepth(cache, subNode);
			if(this.parent == null) {
				preDepths = list;
				return null;
			}
			if (cache){
				if(preDepths.size() == list.size()) {
					preDepths.removeAll(list);
					if(preDepths.isEmpty()) {
						preDepths = list;
						return null;
					}
				}
			}else {
				if(list.size() == 1 && list.get(0) == this.depth)
					return null;
			}
			preDepths = list;
			return parent.getUnbalancedNode(cache,position);
		}
	}

	/**
	 * 本节点是非叶节点，其表示的子树失衡；或者本节点是叶节点，其索引元素数越界。调用该方法获取重建的最小子树能，使
	 * 得整颗树重返平衡状态。
	 * @param cache true这棵树使用了延迟更新，false没有使用延迟更新
	 * @param subNode 当cache为false且本节点是叶节点时该参数有效，表示本叶点在父节点的第subNode个索引项。
	 * @return 返回需要重新分配元素的最小子树
	 */
	QBSDirNode<T> getMinReassignNode(boolean cache, int subNode) {
		QBSDirNode<T> trcDirNode;
		if (this instanceof QBSDataNode) {
			trcDirNode = getUnbalancedNode(cache,subNode);
			if (trcDirNode == null)
				return null;
		}
		else
			trcDirNode = (QBSDirNode<T>) this;
		if (isRoot())
			return trcDirNode;
		QBSDirNode<T> UBNode1 = trcDirNode;
		QBSDirNode<T> UBNode2 = trcDirNode;
		while (UBNode2 != null && UBNode2.parent != null) {
			UBNode1 = UBNode2;
			UBNode1.preDepths = UBNode1.calculatePreDepths();
			UBNode2 = UBNode1.parent.getUnbalancedNode(cache, UBNode1.position);
		}
		if (UBNode2 == null)
			return UBNode1;
		else
			return UBNode2;
	}


	/**
	 * 使用延迟更新时，获取本节点导致的非法状态需要重新分配元素的最小子树
	 * @return 最小子树的根节点
	 */
	QBSNode<T> getMinReassignNodeForCache() {
		if(elemNum <= tree.upBound && elemNum >= tree.lowBound) {
			if (this instanceof QBSDataNode) {
				return this;
			}else {
                preDepths = new ArrayList<>(Collections.singletonList(0));
                QBSDirNode<T> UBNode;
                UBNode = getMinReassignNode(true,0);
                if(UBNode == null || UBNode == this)
                    return this;
                else
                    return UBNode;
            }
		}else if( elemNum > tree.upBound ) {
			preDepths = calculatePreDepths();
			if (isRoot()) {
				return this;
			}else {
				QBSDirNode<T> UBNode;
				UBNode = getMinReassignNode(true, 0);
				if (UBNode == null)
					return this;
				else
					return UBNode;
			}
		}else {
			if (isRoot())
				return this;
			else
				return parent.getMinReassignNodeForCache();
		}
	}


	/**
	 * 插入、删除或更新一个元素后，更新其叶节点所在路径上的所有节点的region
	 * @param rectangle 元素
	 * @param operatorType  1：插入；2：删除。
	 */
	void updateRegion(Rectangle rectangle, int operatorType) {
		Rectangle nRegion = region;
		if (this instanceof QBSDirNode){
			region = calculateRegion();
			if( !Constants.rectangleEqual(nRegion, region) && parent != null)
				parent.updateRegion(nRegion, operatorType );
		}else {
			QBSDataNode<T> cur = (QBSDataNode<T>) this;
			switch (operatorType) {
				case 1: //插入
					if (region == null) {
						region = rectangle.clone();
					}else {
						if (!region.isInternal(rectangle))
							region = region.getUnionRectangle(rectangle);
					}
					break;
				case 2: //删除
					if (cur.elemNum == 0) {
						region = null;
					}else {
						if (region.isEdgeOverlap(rectangle)){
							region = cur.calculateRegion();
						}
					}
					break;
			}
			if(!Constants.rectangleEqual(nRegion, region) && parent != null )
				parent.updateRegion(nRegion, operatorType );
		}
	}

	/**
	 * 将本节点为根的子树重新分配数据，返回调整后的根节点。
	 */
	QBSNode<T> reAdjust() {
		QBSNode<T> result;
		if(this instanceof QBSDataNode) {
			QBSDataNode<T> node = (QBSDataNode<T>) this;
			if(elemNum <= tree.upBound && elemNum >= tree.lowBound )
				return this;
			else if( elemNum > tree.upBound ) { //一分多
				QBSDirNode<T> res;
				QBSDirNode<T> tmp = parent;
				parent = null;
				res = node.recursionSplit();
				res.depthPreDepthConvert(true);
				res.parent = tmp;
				res.position = position;
				if(res.parent != null)
					res.parent.child[position] = res;
				else
					tree.root = res;
				result = res;
			}else {
				throw new IllegalArgumentException("不应该有元素数小于下届的节点重新调整节点");
			}
		}else {
			QBSDirNode<T> node = (QBSDirNode<T>) this;
			if(elemNum <= tree.upBound && elemNum >= tree.lowBound) { //多合一
				List<T> elms = new ArrayList<>();
				node.getAllElement(elms);
				QBSDataNode<T> dataNode = new QBSDataNode<>(0,parent,position,this.centerRegion, this.region, new ArrayList<>()
						,this.elemNum,this.tree, elms);
				dataNode.preDepths.add(0);
				if(parent != null)
					parent.child[position] = dataNode;
				else
					tree.root = dataNode;
				result = dataNode;
			}else if( elemNum > tree.upBound ) { //多分多
				QBSDirNode<T> res = node.redistribution();
				res.depthPreDepthConvert(true);
				if(res.parent == null)
					tree.root = res;
				result = res;
			}else
				throw new IllegalArgumentException("不应该有元素数小于下届的节点重新调整节点");
		}
		return  result;
	}

	/**
	 * 自底向上，初始化本节点以及本节点的祖先节点的预设置深度predDepths
	 */
	void convertUpperLayerPD() {
		preDepths.clear();
		preDepths.add(depth);
		if(parent != null) {
			parent.convertUpperLayerPD();
		}
	}

	/**
	 * 计算某个节点重新分配后的深度范围
	 */
	List<Integer> calculatePreDepths() {
		double low, up;
		double tmp1, tmp2;
		tmp1 = (((double) this.elemNum)/((double)tree.upBound));
		tmp2 = ((double)tree.precision*2)/((double)tree.precision + 1);
		low = Math.log10(tmp1) / Math.log10(Math.pow(2.0,2));
		up  = Math.log10(tmp1) / Math.log10(Math.pow(tmp2,2));
		int low1, up1;
        low1 = Math.max( (int) Math.ceil(low), 0);
        up1 = Math.max( (int) Math.ceil(up), 0);
		List<Integer> res = new ArrayList<>();
		for(int resNum = low1; resNum<up1+1; resNum++)
			res.add(resNum);
		return res;
	}


	boolean check() {
		if (this instanceof QBSDataNode) {
			if (!checkRCDataNode())
				return false;
		}else {
			if (!checkRCDirNode())
				return false;
		}
		if(this.parent != null) {
			if (parent.child[position] != this)
				throw new IllegalArgumentException("parent child error");
		}
		if(this instanceof QBSDirNode){
			for(int chNum = 0; chNum<4; chNum++)
				((QBSDirNode<T>) this).child[chNum].check();
		}
		return true;
	}

	private boolean checkRCDirNode() {
		QBSDirNode<T> cur = (QBSDirNode<T>) this;
		if(cur.centerRegion.getLeftBound() != cur.child[0].centerRegion.getLeftBound() ||
				cur.centerRegion.getLeftBound() != cur.child[2].centerRegion.getLeftBound() ||
				cur.centerRegion.getRightBound() != cur.child[1].centerRegion.getRightBound() ||
				cur.centerRegion.getRightBound() != cur.child[3].centerRegion.getRightBound() ||
				cur.centerRegion.getLowBound() != cur.child[0].centerRegion.getLowBound() ||
				cur.centerRegion.getLowBound() != cur.child[1].centerRegion.getLowBound() ||
				cur.centerRegion.getTopBound() != cur.child[2].centerRegion.getTopBound() ||
				cur.centerRegion.getTopBound() != cur.child[3].centerRegion.getTopBound() ||
				cur.child[0].centerRegion.getRightBound() != cur.child[1].centerRegion.getLeftBound() ||
				cur.child[0].centerRegion.getRightBound() != cur.child[3].centerRegion.getLeftBound() ||
				cur.child[0].centerRegion.getRightBound() != cur.child[2].centerRegion.getRightBound() ||
				cur.child[0].centerRegion.getTopBound() != cur.child[2].centerRegion.getLowBound() ||
				cur.child[1].centerRegion.getTopBound() != cur.child[3].centerRegion.getLowBound() )
			throw new IllegalArgumentException("Bound error");
		Rectangle rectangle = cur.calculateRegion();
		if (!Constants.rectangleEqual(rectangle, cur.region))
			throw new IllegalArgumentException("!Constants.rectangleEqual(rectangle, cur.region)");
		if(cur.depth != (cur.calculateDepth(false, -1)).get(0))
			throw new IllegalArgumentException("cur.depth != (cur.calculateDepth(false, -1)).get(0)");
		if(cur.elemNum != cur.child[0].elemNum + cur.child[1].elemNum +cur.child[2].elemNum +cur.child[3].elemNum)
			throw new IllegalArgumentException("elemNum error");
		if (!cur.isBalance(0, false, 0) || !cur.isBalance(1, false, 0) || !cur.isBalance(2, false, 0) ||
				!cur.isBalance(3, false, 0))
			throw new IllegalArgumentException("Balance error");
		return true;
	}

	/**
	 * 检查叶子结点是否合法
	 */
	private boolean checkRCDataNode() {
		QBSDataNode<T> dataNode = (QBSDataNode<T>) this;

		if (!dataNode.elms.isEmpty() && dataNode.elms.get(0) instanceof RectElem){
			for (T elem : dataNode.elms){
				RectElem rectElem = (RectElem) elem;
				if (!dataNode.region.isInternal(rectElem.rect))
					throw new IllegalArgumentException("RectElem rect error " + elem);
				if (!rectElem.rect.getCenter().equals(rectElem))
					throw new IllegalArgumentException("RectElem Center error " + elem);
			}
		}

		for (T elem : dataNode.elms) {
			if (!dataNode.centerRegion.isInternal(elem))
				throw new IllegalArgumentException("!cur.centerRegion.isInternal(elem)");
		}
		Rectangle checkRectangle = dataNode.calculateRegion();
		if (!Constants.rectangleEqual(checkRectangle, region))
			throw new IllegalArgumentException("!Constants.rectangleEqual(checkRectangle, region)");
		if (dataNode.depth != 0)
			throw new IllegalArgumentException("cur.depth != 0");
		if (tree.cacheSize <= 0 && dataNode.elms.size() > tree.upBound)
			throw new IllegalArgumentException("tree.cacheSize <= 0 && cur.elms.size() > tree.upBound");
		if (tree.cacheSize <= 0 && !dataNode.isRoot() && dataNode.elms.size() < tree.lowBound)
			throw new IllegalArgumentException("tree.cacheSize <= 0 && !cur.isRoot() && cur.elms.size() < tree.lowBound");
		if (dataNode.elemNum != dataNode.elms.size())
			throw new IllegalArgumentException("cur.depth != 0");
		return true;
	}

	/**
	 * 查找指定矩形rectangle包含的叶节点集合
	 * @param rectangle 矩形rectangle
	 * @param leaves 叶节点集合
	 */
	abstract void queryLeaf(Rectangle rectangle, List<QBSDataNode<T>> leaves);

	/**
	 * 自顶向下，将全部节点的depth与predepths互换
	 * @param flag  true，depth转换为predepths；false，predepths转换为depth
	 */
	void depthPreDepthConvert(boolean flag) {
		if(flag) {
			preDepths.clear();
			preDepths.add(depth);
		}else {
			if(preDepths.size() != 1)
				throw new IllegalArgumentException("preDepths的元素数不等于1");
			depth = preDepths.get(0);
			preDepths = new ArrayList<>();
		}
		if(this instanceof QBSDataNode)
			return;
		for(int chN = 0; chN < (int) Math.pow(2.0, 2); chN++) {
			((QBSDirNode<T>) this).child[chN].depthPreDepthConvert(flag);
		}
	}


	/**
	 * 判断本节点和指定节点node是否在同一条路径上
	 * @return 本节点是node节点的同路径的高层节点返回1，node节点是本节点的同路径的高层节点返回-1。两者不在同一路径返回0
	 */
	int isSameWay(QBSNode<T> node) {
		QBSNode<T> n1 = this;
		QBSNode<T> n2 = node;
		while(n1 != null) {
			if(n1 != n2)
				n1 = n1.parent;
			else
				return -1;
		}
		n1 = this;
		n2 = node;
		while(n2 != null) {
			if(n1 != n2)
				n2 = n2.parent;
			else
				return 1;
		}
		return 0;
	}

	/**
	 * 获取子树中所有叶子结点，存储在list中。
	 * @param list 存储叶子结点
	 */
	abstract void getLeafNodes(List<QBSDataNode<T>> list);

	abstract Rectangle calculateRegion();

	abstract void getAllElement(List<T> elms);

	boolean isRoot(){return parent==null;}



}