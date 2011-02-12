package org.khelekore.prtree;

import java.util.PriorityQueue;
import java.util.ArrayList;
import java.util.List;

class NearestNeighbour<T> {

    private final MBRConverter<T> converter;
    private final NodeFilter<T> filter;
    private final int maxHits;
    private final Node<T> root;
    private final DistanceCalculator<T> dc;
    private final double x;
    private final double y;

    public NearestNeighbour (MBRConverter<T> converter,
			     NodeFilter<T> filter,
			     int maxHits,
			     Node<T> root,
			     DistanceCalculator<T> dc,
			     double x, double y) {
	this.converter = converter;
	this.filter = filter;
	this.maxHits = maxHits;
	this.root = root;
	this.dc = dc;
	this.x = x;
	this.y = y;
    }

    /** 
     * @return the nearest neighbour
     */
    public List<DistanceResult<T>> find () {
	List<DistanceResult<T>> ret =
	    new ArrayList<DistanceResult<T>> (maxHits);
	MinDistComparator<T, Node<T>> nc =
	    new MinDistComparator<T, Node<T>> (converter, x, y);
	PriorityQueue<Node<T>> queue = new PriorityQueue<Node<T>> (20, nc);
	queue.add (root);
	while (!queue.isEmpty ()) {
	    Node<T> n = queue.remove ();
	    n.nnExpand (dc, filter, ret, maxHits, queue, nc);
	}
	return ret;
    }
}
