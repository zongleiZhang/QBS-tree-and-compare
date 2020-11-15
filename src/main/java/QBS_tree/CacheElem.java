package QBS_tree;


import common.*;

import java.io.Serializable;

@SuppressWarnings("rawtypes")
public class CacheElem implements Serializable{
	public QBSDataNode leaf;
	public Path path;
	
	public CacheElem() {}

	CacheElem(QBSDataNode leaf) {
		super();
		this.leaf = leaf;
		this.path = new Path(leaf);
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof CacheElem) {
			CacheElem cur = (CacheElem) obj;
			return cur.leaf.equals(this.leaf);
		}else {
			return false;
		}
	}
	
}
