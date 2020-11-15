package QBS_tree;


import geometry.*;

import java.util.*;


/**
 * 索引节点
 * @author zonglei.zhang
 *
 */
public class QBSDirNode<T extends ElemRoot> extends QBSNode<T> {
	/**
	 * 子节点
	 */
	public QBSNode<T>[] child;

	public QBSDirNode() {}

	QBSDirNode(int depth, QBSDirNode<T> parent, int position, Rectangle centerRegion, Rectangle region,
			   List<Integer> preDepths, int elemNum, QBSTree<T> tree, QBSNode<T>[] child) {
		super(depth, parent, position, centerRegion, region, preDepths, elemNum, tree);
		this.child = child;
	}

	public QBSNode[] getChild() {
		return child;
	}

	public void setChild(QBSNode[] child) {
		this.child = child;
	}

	@Override
	public QBSDataNode<T> chooseLeafNode(T elem) {
		QBSNode<T> res;
		elemNum++;
		if(elem.data[0] >= child[0].centerRegion.getRightBound() ) {
			if( elem.data[1] >= child[3].centerRegion.getLowBound() )
					res = child[3];
			else
					res = child[1];
		}else{
			if( elem.data[1] >= child[2].centerRegion.getLowBound() )
					res = child[2];
			else
					res = child[0];
		}
		return res.chooseLeafNode(elem);
	}

	@Override
	QBSDataNode<T> findLeaf(T elem) {
		double bound0 = child[0].centerRegion.getRightBound();
		double bound10 = child[0].centerRegion.getTopBound();
		double bound11 = child[1].centerRegion.getTopBound();
		double x = elem.data[0];
		double y = elem.data[1];
		QBSDataNode<T> tmp;
		if(x > bound0) {
			if(y > bound11) {
				return child[3].findLeaf(elem);
			}else if(y < bound11){
				return child[1].findLeaf(elem);
			}else {
				tmp = child[1].findLeaf(elem);
				return tmp == null? child[3].findLeaf(elem):tmp;
			}
		}else if(x < bound0) {
			if(y > bound10) {
				return child[2].findLeaf(elem);
			}else if(y < bound10){
				return child[0].findLeaf(elem);
			}else {
				tmp = child[0].findLeaf(elem);
				return tmp == null? child[2].findLeaf(elem):tmp;
			}
		}else {
			for (QBSNode<T> node : child) {
				tmp = node.findLeaf(elem);
				if (tmp != null)
					return tmp;
			}
			return null;
		}
	}



	@Override
	Rectangle calculateRegion(){
		Rectangle res;
		List<Rectangle> rectangles = new ArrayList<>();
		for (QBSNode<T> node : this.child) {
			if (node.region != null)
				rectangles.add(node.region);
		}
		if (rectangles.size() == 0)
			res = null;
		else
			res = Rectangle.getUnionRectangle(rectangles.toArray(new Rectangle[0]));
		return res;
	}

	@Override
	void getLeafNodes(List<QBSDataNode<T>> list){
		for (QBSNode<T> QBSNode :this.child)
			QBSNode.getLeafNodes(list);
	}

	@Override
	void getAllElement(List<T> elms) {
		for (QBSNode<T> node : this.child)
			node.getAllElement(elms);
	}

	@Override
	void queryLeaf(Rectangle rectangle, List<QBSDataNode<T>> leaves) {
		for(int chN = 0; chN < 4; chN++) {
			Rectangle mr = child[chN].region;
			if( mr != null && rectangle.isIntersection(mr))
				child[chN].queryLeaf(rectangle, leaves);
		}
	}


	/**
	 * 本节点的某个子节点的predepth改变时，更新本节点以及上层节点的preDepth信息。
	 * @param isUsePreDepth true，使用子节点的预设值深度，false使用子节点当前深度
	 * @param subNode 当isUsePreDepth为false时该参数有效，0 <= subNode < 4 ，
	 *                指定第subNode个子节点使用预设深度，其余节点使用当前深度；
	 *                subNode < 0 , 所有节点都使用当前深度。
	 */
	 void updateUpperLayerPreDepth(boolean isUsePreDepth,  int subNode) {
		List<Integer> list = calculateDepth(isUsePreDepth, subNode);
		if(preDepths.size() == list.size()) {
			preDepths.removeAll(list);
			if(preDepths.isEmpty()) {
				preDepths = list;
			}else {
				preDepths = list;
				if(parent != null)
					parent.updateUpperLayerPreDepth(isUsePreDepth, position);
			}
		}else {
			preDepths = list;
			if(parent != null)
				parent.updateUpperLayerPreDepth(isUsePreDepth, position);
		}
	}

	/**
	 * 本节点的某个子节点的depth发生变化更新本节点以及上层节点的depth信息
	 */
	void updateUpperLayerDepth() {
		int depth;
		depth = (calculateDepth(false,-1)).get(0);
		if(depth != this.depth) {
			this.setDepth(depth);
			if(parent != null)
				parent.updateUpperLayerDepth();
		}
	}

	/**
	 * 使用子节点的预设深度或者当前深度计算计算本节点的深度
	 * @param isUsePreDepth true，使用子节点的预设值深度，false使用子节点当前深度
	 * @param subNode	当isUsePreDepth为false时该参数有效，0 <= subNode < 4 ，指定第subNode个子节点使用预设深度，其余节点使用当前深度； subNode < 0 , 所有节点都使用当前深度。
	 * @return 返回
	 */
	 List<Integer> calculateDepth(boolean isUsePreDepth, int subNode) {
		List<Integer> res = new ArrayList<>();
		Set<Integer> set = new HashSet<>();
		int max = -1;
		if(isUsePreDepth) {
			for (QBSNode<T> trcNode : child) {
				for (Object o:trcNode.preDepths){
					int preDepth = (Integer) o;
					boolean[] canD = new boolean[4];
					for (int chN2 = 0; chN2 < 4; chN2++)
						canD[chN2] = preDepth >= child[chN2].preDepths.get(0);
					if (canD[0] && canD[1] && canD[2] && canD[3])
						set.add(preDepth + 1);
				}
			}
			res = new ArrayList<>(set);
			res.sort(Integer::compareTo);
		}else {
			if(subNode >= 0) {
				for(int subNum = 0;subNum<4;subNum++) {
					if(subNum != subNode)
						max = Math.max(max, child[subNum].depth);
				}
				boolean flag = true;
				for (Integer preDepth:child[subNode].preDepths){
					if(max <= preDepth) {
						flag = false;
						res.add(preDepth+1);
					}
				}
				if(flag)
					res.add(max+1);
			}else {
				for (QBSNode<T> trcNode : child) max = Math.max(max, trcNode.depth);
				res.add(max+1);
			}
		}
		return res;
	 }

	/**
	 * 根据预设值深度preDepth或者当前深度depth判断当前节点是否失衡
	 * @param subNode 更新的子节点
	 * @param isPreDepth 使用预设置的深度设置为true，使用当前深度用false
	 * @param preNum 指定使用第几个预设值深度, isPreDepth为true时该参数有效
	 * @return 失衡返回false，没有失衡返回true
	 */
	boolean isBalance(int subNode, boolean isPreDepth, int preNum) {
		int subNum = 0;
		boolean balance  = true;
		int depth;
		if(isPreDepth)
			depth = child[subNode].preDepths.get(preNum);
		else
			depth = child[subNode].depth;
		while( balance &&  subNum < 4) {
			if(subNum != subNode)
				balance = Math.abs(child[subNum].depth - depth) <= tree.balanceFactor;
			subNum++;
		}
		return balance;
	}

	/**
	 * 重建非叶节点子树
	 * @return 重建后的子树根节点
	 */
	QBSDirNode<T> redistribution () {
		List<T> elms = new ArrayList<>();
		getAllElement(elms);
		QBSDirNode<T> res;
		QBSDataNode<T> dataNode = new QBSDataNode<>(0,null,-1, centerRegion, region, new ArrayList<>()
				,elemNum,tree,elms);
		res = dataNode.recursionSplit();
		res.parent = parent;
		res.position = position;
		if(parent != null)
			parent.child[position] = res;
		return res;
	}


}
