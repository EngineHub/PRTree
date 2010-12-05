package org.khelekore.prtree;

import java.util.Comparator;
import java.util.PriorityQueue;

class NearestNeighbour<T> {

    private Node<T> root;

    public NearestNeighbour (Node<T> root) {
	this.root = root;
    }

    /** 
     * @return the nearest neighbour
     */
    public T find () {
	NeighbourComparator<Node<T>> nc =
	    new NeighbourComparator<Node<T>> ();
	PriorityQueue<Node<T>> toExpand =
	    new PriorityQueue<Node<T>> (20, nc);
	/**
	   For each element in toExpand 
	   * if it is an internal node expand it and push into toExpand
	   *  check the maximum distance to each element and do insertion sort
	   * if it is a leaf node check each element and push into toExpand
	 */
	
	return null;
    }

    private static class NeighbourComparator<T extends Node<?>>
	implements Comparator<T> {

	public int compare (T t1, T t2) {
	    return 0;
	}
    }
}
