package org.khelekore.prtree;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.PriorityQueue;

class InternalNode<T> extends NodeBase<Node<T>, T> {
    public InternalNode (Object[] data) {
	super (data);
    }

    @Override public MBR computeMBR (MBRConverter<T> converter) {
	MBR ret = null;
	for (int i = 0, s = size (); i < s; i++)
	    ret = getUnion (ret, get (i).getMBR (converter));
	return ret;
    }

    public void expand (MBR mbr, MBRConverter<T> converter, List<T> found,
			List<Node<T>> nodesToExpand) {
	for (int i = 0, s = size (); i < s; i++) {
	    Node<T> n = get (i);
	    if (mbr.intersects (n.getMBR (converter)))
		nodesToExpand.add (n);
	}
    }

    public void find (MBR mbr, MBRConverter<T> converter, List<T> result) {
	for (int i = 0, s = size (); i < s; i++) {
	    Node<T> n = get (i);
	    if (mbr.intersects (n.getMBR (converter)))
		n.find (mbr, converter, result);
	}
    }

    public DistanceResult<T> nnExpand (DistanceCalculator<T> dc,
				       DistanceResult<T> dr,
				       PriorityQueue<Node<T>> queue,
				       MinDistComparator<T, Node<T>> mdc) {
	int s = size ();
	List<Node<T>> adds = new ArrayList<Node<T>> (s);
	for (int i = 0; i < s; i++) {
	    Node<T> n = get (i);
	    MBR mbr = n.getMBR (mdc.converter);
	    double minDist = MinDist.get (mbr.getMinX (), mbr.getMinY (),
					  mbr.getMaxX (), mbr.getMaxY (), 
					  mdc.x, mdc.y);
	    if (minDist <= dr.getDistance ())
		adds.add (n);
	}
	Collections.sort (adds, mdc);
	queue.addAll (adds);
	return dr;
    }
}
