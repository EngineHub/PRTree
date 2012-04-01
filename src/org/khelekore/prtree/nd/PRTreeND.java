package org.khelekore.prtree.nd;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import org.khelekore.prtree.DistanceResult;
import org.khelekore.prtree.NodeFilter;

/** A Priority R-Tree, a spatial index, for N dimensions.
 *  This tree only supports bulk loading.
 *
 * @param <T> the data type stored in the PRTree
 */
public class PRTreeND<T> {

    private MBRConverterND<T> converter;
    private int branchFactor;

    private NodeND<T> root;
    private int numLeafs;
    private int height;

    /** Create a new PRTree using the specified branch factor.
     * @param converter the MBRConverter to use for this tree
     * @param branchFactor the number of child nodes for each internal node.
     */
    public PRTreeND (MBRConverterND<T> converter, int branchFactor) {
	this.converter = converter;
	this.branchFactor = branchFactor;
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
    public void load (Collection<? extends T> data) {
	if (root != null)
	    throw new IllegalStateException ("Tree is already loaded");
	numLeafs = data.size ();
	LeafBuilderND lb = new LeafBuilderND (converter.getDimensions (), branchFactor);

	List<LeafNodeND<T>> leafNodes =
	    new ArrayList<LeafNodeND<T>> (estimateSize (numLeafs));
	lb.buildLeafs (data, new DataComparators<T> (converter),
		       new LeafNodeFactory (), leafNodes);

	height = 1;
	List<? extends NodeND<T>> nodes = leafNodes;
	while (nodes.size () > branchFactor) {
	    height++;
	    List<InternalNodeND<T>> internalNodes =
		new ArrayList<InternalNodeND<T>> (estimateSize (nodes.size ()));
	    lb.buildLeafs (nodes, new InternalNodeComparators<T> (converter),
			   new InternalNodeFactory (), internalNodes);
	    nodes = internalNodes;
	}
	setRoot (nodes);
    }

    private int estimateSize (int dataSize) {
	return (int)(1.0 / (branchFactor - 1) * dataSize);
    }

    private <N extends NodeND<T>> void setRoot (List<N> nodes) {
	if (nodes.size () == 0)
	    root = new InternalNodeND<T> (new Object[0]);
	else if (nodes.size () == 1) {
	    root = nodes.get (0);
	} else {
	    height++;
	    root = new InternalNodeND<T> (nodes.toArray ());
	}
    }

    private class LeafNodeFactory
	implements NodeFactoryND<LeafNodeND<T>> {
	public LeafNodeND<T> create (Object[] data) {
	    return new LeafNodeND<T> (data);
	}
    }

    private class InternalNodeFactory
	implements NodeFactoryND<InternalNodeND<T>> {
	public InternalNodeND<T> create (Object[] data) {
	    return new InternalNodeND<T> (data);
	}
    }

    /** Get a minimum bounding box of the data stored in this tree.
     * @return the MBRND of the whole PRTree
     */
    public MBRND getMBR () {
	return root.getMBR (converter);
    }

    /** Get the number of data leafs in this tree.
     * @return the total number of leafs in this tree
     */
    public int getNumberOfLeaves () {
	return numLeafs;
    }

    /** Check if this tree is empty
     * @return true if the number of leafs is 0, false otherwise
     */
    public boolean isEmpty () {
	return numLeafs == 0;
    }

    /** Get the height of this tree.
     * @return the total height of this tree
     */
    public int getHeight () {
	return height;
    }

    /** Finds all objects that intersect the given rectangle and stores
     *  the found node in the given list.
     * @param query the bounds of the query
     * @param resultNodes the list that will be filled with the result
     */
    public void find (MBRND query, List<T> resultNodes) {
	validateRect (query);
	root.find (query, converter, resultNodes);
    }

    /** Find all objects that intersect the given rectangle.
     * @param query the bounds of the query
     * @throws IllegalArgumentException if xmin &gt; xmax or ymin &gt; ymax
     * @return an iterable of the elements inside the query rectangle
     */
    public Iterable<T> find (final MBRND query) {
	validateRect (query);
	return new Iterable<T> () {
	    public Iterator<T> iterator () {
		return new Finder (query);
	    }
	};
    }

    private void validateRect (MBRND query) {
	for (int i = 0; i < converter.getDimensions (); i++) {
	    double max = query.getMax (i);
	    double min = query.getMin (i);
	    if (max < min)
		throw new IllegalArgumentException ("max: " + max +
						    " < min: " + min +
						    ", axis: " + i + 
						    ", query: " + query);
	}
    }

    private class Finder implements Iterator<T> {
	private MBRND mbr;

	private List<T> ts = new ArrayList<T> ();
	private List<NodeND<T>> toVisit = new ArrayList<NodeND<T>> ();
	private T next;

	private int visitedNodes = 0;
	private int dataNodesVisited = 0;

	public Finder (MBRND mbr) {
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
		NodeND<T> n = toVisit.remove (toVisit.size () - 1);
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

    /** Get the nearest neighbour of the given point
     * @param dc the DistanceCalculator to use.
     * @param filter a NodeFilter that can be used to ignore some leaf nodes.
     * @param maxHits the maximum number of entries to find.
     * @param p the point to find the nearest neighbour to.
     * @return A List of DistanceResult with up to maxHits results.
     *         Will return an empty list if this tree is empty.
     */
    public List<DistanceResult<T>> nearestNeighbour (DistanceCalculatorND<T> dc,
						     NodeFilter<T> filter,
						     int maxHits,
						     PointND p) {
	if (isEmpty ())
	    return Collections.emptyList ();
	NearestNeighbourND<T> nn =
	    new NearestNeighbourND<T> (converter, filter, maxHits, root, dc, p);
	return nn.find ();
    }
}
