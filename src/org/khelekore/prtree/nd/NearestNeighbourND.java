package org.khelekore.prtree.nd;

import java.util.PriorityQueue;
import java.util.ArrayList;
import java.util.List;
import org.khelekore.prtree.DistanceResult;
import org.khelekore.prtree.NodeFilter;

class NearestNeighbourND<T> {

    private final MBRConverterND<T> converter;
    private final NodeFilter<T> filter;
    private final int maxHits;
    private final NodeND<T> root;
    private final DistanceCalculatorND<T> dc;
    private final PointND p;

    public NearestNeighbourND (MBRConverterND<T> converter,
			       NodeFilter<T> filter,
			       int maxHits,
			       NodeND<T> root,
			       DistanceCalculatorND<T> dc,
			       PointND p) {
	this.converter = converter;
	this.filter = filter;
	this.maxHits = maxHits;
	this.root = root;
	this.dc = dc;
	this.p = p;
    }

    /** 
     * @return the nearest neighbour
     */
    public List<DistanceResult<T>> find () {
	List<DistanceResult<T>> ret =
	    new ArrayList<DistanceResult<T>> (maxHits);
	MinDistComparatorND<T, NodeND<T>> nc =
	    new MinDistComparatorND<T, NodeND<T>> (converter, p);
	PriorityQueue<NodeND<T>> queue = new PriorityQueue<NodeND<T>> (20, nc);
	queue.add (root);
	while (!queue.isEmpty ()) {
	    NodeND<T> n = queue.remove ();
	    n.nnExpand (dc, filter, ret, maxHits, queue, nc);
	}
	return ret;
    }
}
