package org.khelekore.prtree.nd;

import java.util.List;
import java.util.PriorityQueue;
import org.khelekore.prtree.DistanceResult;
import org.khelekore.prtree.NodeFilter;

class InternalNodeND<T> extends NodeBaseND<NodeND<T>, T> {
    public InternalNodeND (Object[] data) {
	super (data);
    }

    @Override public MBRND computeMBR (MBRConverterND<T> converter) {
	MBRND ret = null;
	for (int i = 0, s = size (); i < s; i++)
	    ret = getUnion (ret, get (i).getMBR (converter));
	return ret;
    }

    public void expand (MBRND mbr, MBRConverterND<T> converter, List<T> found,
			List<NodeND<T>> nodesToExpand) {
	for (int i = 0, s = size (); i < s; i++) {
	    NodeND<T> n = get (i);
	    if (mbr.intersects (n.getMBR (converter)))
		nodesToExpand.add (n);
	}
    }

    public void find (MBRND mbr, MBRConverterND<T> converter, List<T> result) {
	for (int i = 0, s = size (); i < s; i++) {
	    NodeND<T> n = get (i);
	    if (mbr.intersects (n.getMBR (converter)))
		n.find (mbr, converter, result);
	}
    }

    public void nnExpand (DistanceCalculatorND<T> dc,
			  NodeFilter<T> filter,
			  List<DistanceResult<T>> drs,
			  int maxHits,
			  PriorityQueue<NodeND<T>> queue,
			  MinDistComparatorND<T, NodeND<T>> mdc) {
	int s = size ();
	for (int i = 0; i < s; i++) {
	    NodeND<T> n = get (i);
	    MBRND mbr = n.getMBR (mdc.converter);
	    double minDist = MinDistND.get (mbr, mdc.p);
	    int t = drs.size ();
	    // drs is sorted so we can check only the last entry
	    if (t < maxHits || minDist <= drs.get (t - 1).getDistance ())
		queue.add (n);
	}
    }
}
