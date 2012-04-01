package org.khelekore.prtree.nd;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;
import org.khelekore.prtree.DistanceResult;
import org.khelekore.prtree.NodeFilter;

class LeafNodeND<T> extends NodeBaseND<T, T> {

    public LeafNodeND (Object[] data) {
	super (data);
    }

    public MBRND getMBR (T t, MBRConverterND<T> converter) {
	return new SimpleMBRND (t, converter);
    }

    @Override public MBRND computeMBR (MBRConverterND<T> converter) {
	MBRND ret = null;
	for (int i = 0, s = size (); i < s; i++)
	    ret = getUnion (ret, getMBR (get (i), converter));
	return ret;
    }

    public void expand (MBRND mbr, MBRConverterND<T> converter,
			List<T> found, List<NodeND<T>> nodesToExpand) {
	find (mbr, converter, found);
    }

    public void find (MBRND mbr, MBRConverterND<T> converter, List<T> result) {
	for (int i = 0, s = size (); i < s; i++) {
	    T  t = get (i);
	    if (mbr.intersects (t, converter))
		result.add (t);
	}
    }

    public void nnExpand (DistanceCalculatorND<T> dc,
			  NodeFilter<T> filter,
			  List<DistanceResult<T>> drs,
			  int maxHits,
			  PriorityQueue<NodeND<T>> queue,
			  MinDistComparatorND<T, NodeND<T>> mdc) {
	for (int i = 0, s = size (); i < s; i++) {
	    T  t = get (i);
	    if (filter.accept (t)) {
		double dist = dc.distanceTo (t, mdc.p);
		int n = drs.size ();
		if (n < maxHits || dist < drs.get (n - 1).getDistance ()) {
		    add (drs, new DistanceResult<T> (t, dist), maxHits);
		}
	    }
	}
    }

    private void add (List<DistanceResult<T>> drs,
		      DistanceResult<T> dr,
		      int maxHits) {
	int n = drs.size ();
	if (n == maxHits)
	    drs.remove (n - 1);
	// binarySearch return -(pos + 1) for new entries and we always
	// have a new entry
	int insertionPoint = -(Collections.binarySearch (drs, dr, comp) + 1);
	drs.add (insertionPoint, dr);
    }

    private static final Comparator<DistanceResult<?>> comp =
	new Comparator<DistanceResult<?>> () {
	public int compare (DistanceResult<?> d1, DistanceResult<?> d2) {
	    return Double.compare (d1.getDistance (), d2.getDistance ());
	}
    };
}
