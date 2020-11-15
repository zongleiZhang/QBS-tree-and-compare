package QBS_tree;


import geometry.*;

import java.util.*;


public class QBSDataNode<T extends ElemRoot> extends QBSNode<T> {

	public List<T> elms;

	public QBSDataNode() {}

	public QBSDataNode(int depth, QBSDirNode<T> parent, int position, Rectangle centerRegion, Rectangle region,
					   List<Integer> preDepths, int elemNum, QBSTree<T> tree, List<T> elms) {
		super(depth, parent, position, centerRegion,region, preDepths, elemNum, tree);
		this.elms = elms;
	}

	public List<T> getElms() {
		return elms;
	}

	public void setElms(List<T> elms) {
		this.elms = elms;
	}

	@Override
	public QBSDataNode<T> chooseLeafNode(T elem){
		elemNum++;
		elms.add(elem);
		if (elem instanceof RectElem)
			updateRegion( ((RectElem) elem).rect,1);
		else
			updateRegion( new Rectangle(new Point(elem.data.clone()), new Point(elem.data.clone())),1);
		return this;
	}

	@Override
	QBSDataNode<T> findLeaf(T elem) {
		for (T elm : elms) {
			if (elm.equals(elem))
				return this;
		}
		return null;
	}


	@Override
	Rectangle calculateRegion(){
		Rectangle res = null;
		if (this.elemNum != 0) {
			if (this.elms.get(0) instanceof  RectElem){
				List<RectElem> list = (List<RectElem>) elms;
				res = list.get(0).rect.clone();
				for (int i = 1; i < list.size(); i++)
					res = res.getUnionRectangle(list.get(i).rect);
			}else{
				res = new Rectangle(new Point(elms.get(0).data.clone()), new Point(elms.get(0).data.clone()));
				for (int i = 1; i < this.elms.size(); i++)
					res.getUnionRectangle(elms.get(i));
			}
		}
		return res;
	}

	@Override
	void getLeafNodes(List<QBSDataNode<T>> list){
		list.add(this);
	}

	@Override
	void getAllElement(List<T> elms) {
		elms.addAll(this.elms);
	}

	void queryLeaf(Rectangle rectangle, List<QBSDataNode<T>> leaves) {
		if(region != null && rectangle.isIntersection(region))
			leaves.add(this);
	}



	boolean insert() {
		if(this.elemNum <= this.tree.upBound) { //没有上溢
			return true;
		}else {//上溢
			QBSNode<T> UBNode;
			depth = 1;
			convertUpperLayerPD();
			UBNode = getMinReassignNode(false,position);
			QBSNode<T> newNode;
			if(UBNode == null) { //没有失衡
				newNode = split();
				UBNode = this;
			}else {  //失衡
				newNode = ((QBSDirNode<T>)UBNode).redistribution();
			}
			if(newNode.isRoot()) {
				tree.setRoot(newNode);
			}else {
				newNode.parent.updateUpperLayerDepth();
			}
		}
		return true;
	}




	boolean delete(T elem) {
		QBSNode<T> node = this;
		while(node != null) {
			node.elemNum--;
			node = node.parent;
		}
		if(!elms.remove(elem))
			return false;

		if (elem instanceof RectElem)
			updateRegion(((RectElem) elem).rect,2);
		else
			updateRegion(new Rectangle(new Point(elem.data.clone()), new Point(elem.data.clone())),2);
		if(elemNum >= this.tree.lowBound || parent == null) { //没有下溢
			return true;
		}else { //下溢
			QBSDirNode<T> UBNode;
			QBSNode<T> newNode;
			convertUpperLayerPD();
			if(parent.elemNum > tree.upBound) { //父节点不可合并
				UBNode = parent.getMinReassignNode(false,-1);
				newNode = UBNode.redistribution();
			}else{  //父节点可以合并
				if(parent.isRoot()) { //根节点合并成一个叶节点
					List<T> elms = new ArrayList<>();
					parent.getAllElement(elms);
                    QBSDataNode<T> newRoot = new QBSDataNode<>(0, null, -1, parent.centerRegion, parent.region,
                            new ArrayList<>(), parent.elemNum, tree, elms);
                    tree.setRoot(newRoot);
					return true;
				}
				parent.preDepths.clear();
				parent.preDepths.add(0);
				UBNode = parent.getMinReassignNode(false, position);
				if(UBNode == parent) { //合并父节点就能达到平衡
					List<T> sags = new ArrayList<>();
					UBNode.getAllElement(sags);
					QBSDataNode<T> dataNode = new QBSDataNode<>(0, UBNode.parent, UBNode.position,
							UBNode.centerRegion, UBNode.region, new ArrayList<>(), UBNode.elemNum, UBNode.tree, sags);
					dataNode.parent.child[dataNode.position] = dataNode;
					newNode = dataNode;
				}else {
					newNode = UBNode.redistribution();
				}
			}
			if(newNode != null) {
				if (newNode.parent == null)
					tree.setRoot(newNode);
				else
					newNode.parent.updateUpperLayerDepth();
			}
		}
		return true;
	}


	/**
	 * 将叶节点递归的分裂。使用时需要先将该叶节点的parent设置为null，分裂后重新设置parent
	 * @return 返回分裂后的子树的根节点
	 */
	QBSDirNode<T> recursionSplit() {
		QBSDirNode<T> res;
		res = split();
		for(int childNum = 0; childNum< 4; childNum++) {
			if( ((QBSDataNode<T>) res.child[childNum]).elms.size() > tree.upBound )
				((QBSDataNode<T>) res.child[childNum]).recursionSplit();
			else {
				if(res.parent != null)
					res.parent.updateUpperLayerDepth();
			}
		}
		return res;
	}

	private QBSDirNode<T> split() {
		//本层节点赋值2
		QBSDirNode<T> node = new QBSDirNode<>(1, parent, position, centerRegion, region, new ArrayList<>(),
				elemNum, tree, new QBSNode[4]);
		ElemRoot[] tapElms = elms.toArray(new ElemRoot[0]);
		ElemRoot[][] divide0, divide1, divide2;
		divide0 = binaryDivide(tapElms, 0, tree.precision);
		double bound0 = divide0[1][0].data[0];
		divide1 = binaryDivide(divide0[0], 1, tree.precision);
		double bound10 = divide1[1][0].data[1];
		divide2 = binaryDivide(divide0[1], 1, tree.precision);
		double bound11 = divide2[1][0].data[1];
		ElemRoot[][] divide = new ElemRoot[4][];
		divide[0] = divide1[0];
		divide[1] = divide2[0];
		divide[2] = divide1[1];
		divide[3] = divide2[1];
		Rectangle[] grids = new Rectangle[4];
		grids[0] = new Rectangle(centerRegion.low, new Point(bound0,bound10));
		grids[1] = new Rectangle(new Point(bound0,centerRegion.getLowBound()),
				new Point(centerRegion.getRightBound(),bound11));
		grids[2] = new Rectangle(new Point(centerRegion.getLeftBound(),bound10),
				new Point(bound0,centerRegion.getTopBound()));
		grids[3] = new Rectangle(new Point(bound0,bound11), centerRegion.high);
		for(int chNum = 0; chNum < 4; chNum++) {
			QBSDataNode<T> newLeaf = new QBSDataNode<>(0, node, chNum, grids[chNum], null, new ArrayList<>(),
					divide[chNum].length, tree, new ArrayList<>(Arrays.asList((T[]) divide[chNum])));
			newLeaf.region = newLeaf.calculateRegion();
			node.child[chNum] = newLeaf;
		}
		if(parent != null)
			parent.child[position] = node;
		return node;
	}

	/**
	 * 分堆
	 * @param elems 	被分堆的轨迹段集合
	 * @param depend	依据x坐标分堆传入0， 依据y坐标分堆传入1
	 * @param precision 集合二分点的精度
	 * @return	二维数组表示分堆结果
	 */
	private ElemRoot[][] binaryDivide(ElemRoot[] elems, int depend, int precision){
		ElemRoot[][] res = new ElemRoot[2][];
		int axis, start, end;
		int low = (elems.length*(precision/2))/precision;
		int up = (elems.length*((precision/2)+1))/precision;
		start = 0;
		end = elems.length-1;
		axis = -1;
		while(axis < low || axis > up ) {
			if(axis < low) {
				start = axis + 1;
			}else {
				end = axis - 1;
			}
			choseCandidate(elems, depend, start, end, low);
			axis = divide(elems, start, end, depend);
		}
		res[0] = new ElemRoot[axis];
		res[1] = new ElemRoot[elems.length-axis];
		System.arraycopy(elems, 0, res[0], 0,axis);
		System.arraycopy(elems, axis, res[1], 0,elems.length-axis);
		return res;
	}

	/**
	 *  选择快排候选值，将候选元素同start位置处的元素交换位置
	 */
	private void choseCandidate(ElemRoot[] elems, int depend, int start, int end, int low) {
		int[] candidates = new int[5];
		for(int canNum = 0; canNum<5; canNum++) {
			candidates[canNum] = start + (canNum*(end - start))/4;
		}
		ElemRoot tmp;
		for(int canNum1 = 0; canNum1<4;canNum1++) {
			for(int canNum2 = canNum1+1; canNum2 < 5; canNum2++) {
				if(elems[candidates[canNum1]].data[depend] >
						elems[candidates[canNum2]].data[depend]) {
					tmp = elems[candidates[canNum1]];
					elems[candidates[canNum1]] = elems[candidates[canNum2]];
					elems[candidates[canNum2]] = tmp;
				}
			}
		}
		int compareSiteNum;
		for(compareSiteNum = 0; low > candidates[compareSiteNum]; )
			compareSiteNum++;
		tmp = elems[start];
		elems[start] = elems[candidates[compareSiteNum]];
		elems[candidates[compareSiteNum]] = tmp;
	}

	private int divide(ElemRoot[] elems, int start, int end, int depend) {
		ElemRoot tmp;
		int s = start, e = end;
		boolean direct = true;
		while(s < e) {
			if(direct) {
				while(elems[s].data[depend] <= elems[e].data[depend] && s < e)
					e--;
				tmp = elems[e];
				elems[e] = elems[s];
				elems[s] = tmp;
				if(s < e)
					s++;
				direct = false;
			}else {
				while(elems[s].data[depend] < elems[e].data[depend] && s < e)
					s++;
				tmp = elems[e];
				elems[e] = elems[s];
				elems[s] = tmp;
				if(s < e)
					e--;
				direct = true;
			}
		}
		return s;
	}
}
