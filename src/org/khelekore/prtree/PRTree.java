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
     *
     *  Create the leaf nodes that each hold (up to) branchFactor data entries.
     *  Then use the leaf nodes as data until we can fit all nodes into 
     *  the root node.
     *
     * @param data the collection of data to store in the tree.
     * @throws IllegalStateException if the tree is already loaded
     */
    public void load (List<? extends T> data) {
	if (root != null)
	    throw new IllegalStateException ("Tree is already loaded");
	numLeafs = data.size ();
	XMinComparator<T> xMinSorter = new XMinComparator<T> (converter);
	YMinComparator<T> yMinSorter = new YMinComparator<T> (converter);
	XMaxComparator<T> xMaxSorter = new XMaxComparator<T> (converter);
	YMaxComparator<T> yMaxSorter = new YMaxComparator<T> (converter);
	List<LeafNode<T>> leafNodes =
	    new ArrayList<LeafNode<T>> (estimateSize (numLeafs));
	LeafBuilder lb = new LeafBuilder (branchFactor);
	lb.buildLeafs (data, leafNodes, xMinSorter, yMinSorter, 
		       xMaxSorter, yMaxSorter, new LeafNodeFactory ());

	height = 1;
	if (leafNodes.size () < branchFactor) {
	    setRoot (leafNodes);
	} else {
	    XMinNodeComparator<T> xMins = new XMinNodeComparator<T> (converter);
	    YMinNodeComparator<T> yMins = new YMinNodeComparator<T> (converter);
	    XMaxNodeComparator<T> xMaxs = new XMaxNodeComparator<T> (converter);
	    YMaxNodeComparator<T> yMaxs = new YMaxNodeComparator<T> (converter);
	    List<? extends Node<T>> nodes = leafNodes;
	    do {
		height++;
		int es = estimateSize (nodes.size ());
		List<InternalNode<T>> internalNodes =
		    new ArrayList<InternalNode<T>> (es);
		lb.buildLeafs (nodes, internalNodes, xMins, yMins,
			       xMaxs, yMaxs, new InternalNodeFactory ());
		nodes = internalNodes;
	    } while (nodes.size () > branchFactor);
	    setRoot (nodes);
	}
    }

    /** Get a minimum bounding rectangle of the data stored in this tree.
     */
    public MBR getMBR () {
	return root.getMBR (converter);
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
	if (nodes.size () == 0)
	    root = new InternalNode<T> (new Object[0]);
	else if (nodes.size () == 1) {
	    root = nodes.get (0);
	} else {
	    height++;
	    root = new InternalNode<T> (nodes.toArray ());
	}
    }

    private class LeafNodeFactory
	implements LeafBuilder.NodeFactory<LeafNode<T>> {
	public LeafNode<T> create (Object[] data) {
	    return new LeafNode<T> (data);
	}
    }

    private class InternalNodeFactory
	implements LeafBuilder.NodeFactory<InternalNode<T>> {
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
    public void find (double xmin, double ymin, double xmax, double ymax, 
		      List<T> resultNodes) {
	MBR mbr = new SimpleMBR (xmin, ymin, xmax, ymax);
	find (mbr, resultNodes);
    }

    /** Finds all objects that intersect the given rectangle and stores
     *  the found node in the given list.
     * @param resultNodes the list that will be filled with the result
     */
    public void find (MBR query, List<T> resultNodes) {
	validateRect (query.getMinX (), query.getMinY (), 
		      query.getMaxX (), query.getMaxY ());
	root.find (query, converter, resultNodes);
    }

    /** Find all objects that intersect the given rectangle.
     * @throws IllegalArgumentException if xmin &gt; xmax or ymin &gt; ymax
     */
    public Iterable<T> find (final MBR query) {
	validateRect (query.getMinX (), query.getMinY (), 
		      query.getMaxX (), query.getMaxY ());
	return new Iterable<T> () {
	    public Iterator<T> iterator () {
		return new Finder (query);
	    }
	};
    }

    /** Find all objects that intersect the given rectangle.
     * @throws IllegalArgumentException if xmin &gt; xmax or ymin &gt; ymax
     */
    public Iterable<T> find (double xmin, double ymin, 
			     double xmax, double ymax) {
	MBR mbr = new SimpleMBR (xmin, ymin, xmax, ymax);
	return find (mbr);
    }

    private class Finder implements Iterator<T> {
	private MBR mbr;

	private List<T> ts = new ArrayList<T> ();
	private List<Node<T>> toVisit = new ArrayList<Node<T>> ();
	private T next;

	private int visitedNodes = 0;
	private int dataNodesVisited = 0;

	public Finder (MBR mbr) {
	    this.mbr = mbr;
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
		n.expand (mbr, converter, ts, toVisit);
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
