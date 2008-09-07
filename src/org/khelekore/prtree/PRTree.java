package org.khelekore.prtree;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/** A Priority R-Tree, a spatial index.
 *  This tree only supports bulk loading.
 *
 *  <pre>{@code
 *  PRTree<Rectangle2D> tree = 
 *      new PRTree<Rectangle2D> (new Rectangle2DConverter (), 10);
 *  Rectangle2D rx = new Rectangle2D.Double (0, 0, 1, 1);
 *  tree.load (Collections.singletonList (rx));
 *  for (Rectangle2D r : tree.find (0, 0, 1, 1)) {
 *      System.out.println ("found a rectangle: " + r);
 *  }
 *  }</pre>
 * 
 * @param <T> the data type stored in the PRTree
 */
public class PRTree<T> {

    private MBRConverter<T> converter;
    private int branchFactor;

    private Node<T> root;
    private int numLeafs;
    private int height;

    /** Create a new PRTree using the specified branch factor.
     * @param branchFactor the number of child nodes for each internal node.
     */
    public PRTree (MBRConverter<T> converter, int branchFactor) {
	this.converter = converter;
	this.branchFactor = branchFactor;
    }

    private int estimateSize (int dataSize) {
	return (int)(1.0 / (branchFactor - 1) * dataSize);
    }

    /** Bulk load data into this tree.
     * @param data the collection of data to store in the tree.
     * @throws IllegalStateException if the tree is already loaded
     */
    public void load (List<? extends T> data) {
	if (root != null)
	    throw new IllegalStateException ("Tree is already loaded");
	numLeafs = data.size ();
	XComparator<T> xSorter = new XComparator<T> (converter);
	YComparator<T> ySorter = new YComparator<T> (converter);
	List<LeafNode<T>> leafNodes =
	    new ArrayList<LeafNode<T>> (estimateSize (numLeafs));
	LeafBuilder lb = new LeafBuilder (branchFactor);
	lb.buildLeafs (data, leafNodes, xSorter, ySorter, new LeafNodeFactory ());

	height = 1;
	if (leafNodes.size () < branchFactor) {
	    setRoot (leafNodes);
	} else {
	    NodeComparator<T> xs = new NodeComparator<T> (0);
	    NodeComparator<T> ys = new NodeComparator<T> (1);
	    List<? extends Node<T>> nodes = leafNodes;
	    do {
		height++;
		int es = estimateSize (nodes.size ());
		List<InternalNode<T>> internalNodes =
		    new ArrayList<InternalNode<T>> (es);
		lb.buildLeafs (nodes, internalNodes, xs, ys,
			       new InternalNodeFactory ());
		nodes = internalNodes;
	    } while (nodes.size () > branchFactor);
	    setRoot (nodes);
	}
    }

    /** Get a minimum bounding rectangle of the data stored in this tree.
     */
    public MBR getMBR () {
	return root.getMBR ();
    }
    
    /** Get the number of data leafs in this tree. 
     */
    public int getNumberOfLeaves () {
	return numLeafs;
    }

    /** Get the height of this tree. 
     */
    public int getHeight () {
	return height;
    }

    private <N extends Node<T>> void setRoot (List<N> nodes) {
	if (nodes.size () == 1) {
	    root = nodes.get (0);
	} else {
	    height++;
	    root = new InternalNode<T> (nodes.toArray ());
	}
    }

    private class LeafNodeFactory
	implements LeafBuilder.NodeFactory<LeafNode<T>, T> {
	public LeafNode<T> create (Object[] data) {
	    return new LeafNode<T> (data, converter);
	}
    }

    private class InternalNodeFactory
	implements LeafBuilder.NodeFactory<InternalNode<T>, Node<T>> {
	public InternalNode<T> create (Object[] data) {
	    return new InternalNode<T> (data);
	}
    }

    private void validateRect (final double xmin, final double ymin,
			       final double xmax, final double ymax) {
	if (xmax < xmin)
	    throw new IllegalArgumentException ("xmax: " + xmax +
						" < xmin: " + xmin);
	if (ymax < ymin)
	    throw new IllegalArgumentException ("ymax: " + ymax +
						" < ymin: " + ymin);
    }

    /** Finds all objects that intersect the given rectangle and stores
     *  the found node in the given list.
     * @param resultNodes the list that will be filled with the result
     */
    public void find (final double xmin, final double ymin,
		      final double xmax, final double ymax, 
		      List<T> resultNodes) {
	validateRect (xmin, ymin, xmax, ymax);
	MBR mbr = new SimpleMBR (xmin, ymin, xmax, ymax);
	root.find (mbr, resultNodes);
    }

    /** Find all objects that intersect the given rectangle.
     * @throws IllegalArgumentException if xmin &gt; xmax or ymin &gt; ymax
     */
    public Iterable<T> find (final double xmin, final double ymin,
			     final double xmax, final double ymax) {
	validateRect (xmin, ymin, xmax, ymax);
	return new Iterable<T> () {
	    public Iterator<T> iterator () {
		return new Finder (xmin, ymin, xmax, ymax);
	    }
	};
    }

    private class Finder implements Iterator<T> {
	private MBR mbr;

	private List<T> ts = new ArrayList<T> ();
	private List<Node<T>> toVisit = new ArrayList<Node<T>> ();
	private T next;

	private int visitedNodes = 0;
	private int dataNodesVisited = 0;

	public Finder (double xmin, double ymin, double xmax, double ymax) {
	    mbr = new SimpleMBR (xmin, ymin, xmax, ymax);
	    toVisit.add (root);
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
		visitedNodes++;
		n.expand (mbr, ts, toVisit);
	    }
	    if (ts.isEmpty ()) {
		next = null;
	    } else {
		next = ts.remove (ts.size () - 1);
		dataNodesVisited++;
	    }
	}

	public void remove () {
	    throw new UnsupportedOperationException ("Not implemented");
	}
    }
}
