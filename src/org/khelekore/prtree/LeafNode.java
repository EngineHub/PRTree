package org.khelekore.prtree;

import java.util.List;
import java.util.PriorityQueue;

class LeafNode<T> extends NodeBase<T, T> {

    public LeafNode (Object[] data) {
	super (data);
    }

    public MBR getMBR (T t, MBRConverter<T> converter) {
	return new SimpleMBR (converter.getMinX (t), converter.getMinY (t),
			      converter.getMaxX (t), converter.getMaxY (t));
    }

    @Override public MBR computeMBR (MBRConverter<T> converter) {
	MBR ret = null;
	for (int i = 0, s = size (); i < s; i++)
	    ret = getUnion (ret, getMBR (get (i), converter));
	return ret;
    }

    public void expand (MBR mbr, MBRConverter<T> converter,
			List<T> found, List<Node<T>> nodesToExpand) {
	find (mbr, converter, found);
    }

    public void find (MBR mbr, MBRConverter<T> converter, List<T> result) {
	for (int i = 0, s = size (); i < s; i++) {
	    T  t = get (i);
	    if (mbr.intersects (t, converter))
		result.add (t);
	}
    }

    public DistanceResult<T> nnExpand (DistanceCalculator<T> dc,
				       NodeFilter<T> filter,
				       DistanceResult<T> dr,
				       PriorityQueue<Node<T>> queue,
				       MinDistComparator<T, Node<T>> mdc) {
	for (int i = 0, s = size (); i < s; i++) {
	    T  t = get (i);
	    if (filter.accept (t)) {
		double dist = dc.distanceTo (t, mdc.x, mdc.y);
		if (dist < dr.getDistance ())
		    dr = new DistanceResult<T> (t, dist);
	    }
	}
	return dr;
    }
}
