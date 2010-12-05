package org.khelekore.prtree;

import java.util.Comparator;
import java.util.PriorityQueue;

class NearestNeighbour<T> {

    private final MBRConverter<T> converter;
    private final Node<T> root;
    private final DistanceCalculator<T> dc;
    private final double x;
    private final double y;

    public NearestNeighbour (MBRConverter<T> converter, Node<T> root,
			     DistanceCalculator<T> dc, double x, double y) {
	this.converter = converter;
	this.root = root;
	this.dc = dc;
	this.x = x;
	this.y = y;
    }

    /** 
     * @return the nearest neighbour
     */
    public DistanceResult<T> find () {
	DistanceResult<T> res = new DistanceResult<T> (null, Double.MAX_VALUE);
	MinDistComparator<T, Node<T>> nc =
	    new MinDistComparator<T, Node<T>> (converter, x, y);
	PriorityQueue<Node<T>> queue = new PriorityQueue<Node<T>> (20, nc);
	queue.add (root);
	while (!queue.isEmpty ()) {
	    Node<T> n = queue.remove ();
	    res = n.nnExpand (dc, res, queue, nc);
	}
	return res;
    }
}
