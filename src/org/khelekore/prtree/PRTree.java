package org.khelekore.prtree;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public class PRTree<T> {

    private MBRConverter<T> converter;
    private int branchFactor;

    private List<Node<T>> root;

    /** Create a new PRTree using the specified branch factor.
     * @param branchFactor the number of child nodes for each internal node.
     */
    public PRTree (MBRConverter<T> converter, int branchFactor) {
	this.converter = converter;
	this.branchFactor = branchFactor;
    }

    public void load (Collection<? extends T> data) {
	List<T> ls = new ArrayList<T> (data);
	OrdComparator<T> xSorter = new OrdComparator<T> (0, converter);
	OrdComparator<T> ySorter = new OrdComparator<T> (1, converter);
	List<LeafNode<T>> leafNodes = new ArrayList<LeafNode<T>> ();
	LeafBuilder lb = new LeafBuilder (branchFactor);
	lb.buildLeafs (ls, leafNodes, xSorter, ySorter, new LeafNodeFactory ());
	
	if (leafNodes.size () < branchFactor) {
	    setRoot (leafNodes);
	} else {
	    NodeComparator<T> xs = new NodeComparator<T> (0);
	    NodeComparator<T> ys = new NodeComparator<T> (1);
	    List<Node<T>> nodes = new ArrayList<Node<T>> (leafNodes);
	    do {
		List<InternalNode<T>> internalNodes = 
		    new ArrayList<InternalNode<T>> ();
		lb.buildLeafs (nodes, internalNodes, xs, ys, new InternalNodeFactory ());
		nodes = new ArrayList<Node<T>> (internalNodes);
	    } while (nodes.size () > branchFactor);
	    setRoot (nodes);
	}
    }

    private <N extends Node<T>> void setRoot (List<N> nodes) {
	root = new InternalNode<T> (nodes.size (), converter);
	for (Node<T> n : nodes)
	    root.add (n);
    }

    private class LeafNodeFactory 
	implements LeafBuilder.NodeFactory<LeafNode<T>, T> {
	public LeafNode<T> create () {
	    return new LeafNode<T> (branchFactor, converter);
	}

	public void add (LeafNode<T> node, T data) {
	    node.add (data);
	}
    }

    private class InternalNodeFactory 
	implements LeafBuilder.NodeFactory<InternalNode<T>, Node<T>> {
	public InternalNode<T> create () {
	    return new InternalNode<T> (branchFactor, converter);
	}

	public void add (InternalNode<T> node, Node<T> data) {
	    node.add (data);
	}
    }

    /** Find all objects that intersect the given rectangle
     * @throws IllegalArgumentException if xmin &gt; xmax or ymin &gt; ymax 
     */
    public Iterable<T> find (final double xmin, final double ymin,
			     final double xmax, final double ymax) {
	if (xmax < xmin)
	    throw new IllegalArgumentException ("xmax: " + xmax + " < xmin: " + xmin);
	if (ymax < ymin)
	    throw new IllegalArgumentException ("ymax: " + ymax + " < ymin: " + ymin);
	return new Iterable<T> () {
	    public Iterator<T> iterator () {
		return new Finder (xmin, ymin, xmax, ymax);
	    }
	};
    }

    private class Finder implements Iterator<T> {
	private MBR mbr;

	private List<T> ts = new ArrayList<T> ();
	private List<Node<T>> toVisit;
	private T next;

	public Finder (double xmin, double ymin, double xmax, double ymax) {
	    mbr = new SimpleMBR (xmin, ymin, xmax, ymax);
	    toVisit = new ArrayList<Node<T>> (root);
	    findNext ();
	}

	public boolean hasNext () {
	    return next != null;
	}

	public T next () {
	    T toReturn = next;
	    findNext ();
	    return toReturn;
	}

	private void findNext () {
	    while (ts.isEmpty () && !toVisit.isEmpty ()) {
		Node<T> n = toVisit.remove (toVisit.size () - 1);
		n.expand (mbr, ts, toVisit);
	    }
	    if (ts.isEmpty ())
		next = null;
	    else
		next = ts.remove (ts.size () - 1);
	}

	public void remove () {
	    throw new UnsupportedOperationException ("Not implemented");
	}
    }
}
