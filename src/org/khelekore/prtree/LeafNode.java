package org.khelekore.prtree;

import java.util.List;

class LeafNode<T> extends NodeBase<T, T> {
    public LeafNode (int size, MBRConverter<T> converter) {
	super (size, converter);
    }

    @Override public MBR computeMBR () {
	MBR ret = null;
	for (T t : this)
	    ret = getUnion (ret, getMBR (t));
	return ret;
    }

    public void expand (MBR mbr, List<T> found, List<Node<T>> nodesToExpand) {
	for (T t : this)
	    if (mbr.intersects (getMBR (t)))
		found.add (t);
    }
}
