package org.khelekore.prtree;

import java.util.List;

class LeafNode<T> extends NodeBase<T, T> {
    public LeafNode (int size, MBRConverter<T> converter) {
	super (size, converter);
    }

    @Override public MBR computeMBR () {
	MBR ret = null;
	for (int i = 0, s = size (); i < s; i++)
	    ret = getUnion (ret, getMBR (get (i)));
	return ret;
    }

    public void expand (MBR mbr, List<T> found, List<Node<T>> nodesToExpand) {
	find (mbr, found);
    }

    public void find (MBR mbr, List<T> result) {
	for (int i = 0, s = size (); i < s; i++) {
	    T  t = get (i);
	    if (mbr.intersects (getMBR (t)))
		result.add (t);
	}
    }
}
