package org.khelekore.prtree;

import java.util.List;

class InternalNode<T> extends NodeBase<Node<T>, T> {
    public InternalNode (int size, MBRConverter<T> converter) {
	super (size, converter);
    }
    
    @Override public MBR computeMBR () {
	MBR ret = null;
	for (int i = 0, s = size (); i < s; i++)
	    ret = getUnion (ret, get (i).getMBR ());
	return ret;
    }
    
    public void expand (MBR mbr, List<T> found, List<Node<T>> nodesToExpand) {
	for (int i = 0, s = size (); i < s; i++) {
	    Node<T> n = get (i);
	    if (mbr.intersects (n.getMBR ()))
		nodesToExpand.add (n);
	}
    }

    public void find (MBR mbr, List<T> result) {
	for (int i = 0, s = size (); i < s; i++) {
	    Node<T> n = get (i);
	    if (mbr.intersects (n.getMBR ()))
		n.find (mbr, result);
	}
    }
}
