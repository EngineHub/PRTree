package org.khelekore.prtree;

import java.util.List;

class InternalNode<T> extends NodeBase<Node<T>, T> {
    public InternalNode (int size, MBRConverter<T> converter) {
	super (size, converter);
    }
    
    @Override public MBR computeMBR () {
	MBR ret = null;
	for (Node<T> n : this)
	    ret = getUnion (ret, n.getMBR ());
	return ret;
    }
    
    public void expand (MBR mbr, List<T> found, List<Node<T>> nodesToExpand) {
	for (Node<T> n : this)
	    if (mbr.intersects (n.getMBR ()))
		nodesToExpand.add (n);
    }
}
