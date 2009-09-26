package org.khelekore.prtree;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/** A builder of internal nodes used during bulk loading of a PR-Tree.
 *  A PR-Tree is build by building a pseudo R-Tree and grabbing the
 *  leaf nodes (and then repeating until you have just one root node).
 *  This class creates the leaf nodes without building the full pseudo tree.
 */
class LeafBuilder {

    private int branchFactor;
    private int listHolderId;

    public LeafBuilder (int branchFactor) {
	this.branchFactor = branchFactor;
    }

    public <T, N> void buildLeafs (List<? extends T> ls, List<N> leafNodes,
				   Comparator<T> xMinSorter,
				   Comparator<T> yMinSorter,
				   Comparator<T> xMaxSorter,
				   Comparator<T> yMaxSorter,
				   NodeFactory<N> nf) {
	/** To not waste so much memory we create two lists, sorted by xmin 
	 *  and ymin respectivly. The two lists hold the same objects so we
	 *  can modify the usage info from either list.
	 */
	listHolderId = 1;
	List<NodeUsage<T>> xmin = new ArrayList<NodeUsage<T>> (ls.size ());
	List<NodeUsage<T>> ymin = new ArrayList<NodeUsage<T>> (ls.size ());
	List<NodeUsage<T>> xmax = new ArrayList<NodeUsage<T>> (ls.size ());
	List<NodeUsage<T>> ymax = new ArrayList<NodeUsage<T>> (ls.size ());

	for (T t : ls) {
	    NodeUsage<T> nu = new NodeUsage<T> (t);
	    xmin.add (nu);
	    ymin.add (nu);
	    xmax.add (nu);
	    ymax.add (nu);
	}

	Collections.sort (xmin, new NodeUsageSorter<T> (xMinSorter));
	Collections.sort (ymin, new NodeUsageSorter<T> (yMinSorter));
	Collections.sort (xmax, new NodeUsageSorter<T> (xMaxSorter));
	Collections.sort (ymax, new NodeUsageSorter<T> (yMaxSorter));
	List<NodeGetter<T>> toExpand = new ArrayList<NodeGetter<T>> ();
	ListData<T> listData = new ListData<T> (xmin, ymin, xmax, ymax);
	toExpand.add (new NodeGetter<T> (listData, listHolderId++, xmin.size (),
					 0, 0, 0, 0));;
	internalBuildLeafs (toExpand, leafNodes, nf);
    }

    private static class ListData<T> {
	// Same NodeUsage objects in all lists, just ordered differently.
	public final List<NodeUsage<T>> xmin;
	public final List<NodeUsage<T>> ymin;
	public final List<NodeUsage<T>> xmax;
	public final List<NodeUsage<T>> ymax;

	public ListData (List<NodeUsage<T>> xmin, List<NodeUsage<T>> ymin, 
			 List<NodeUsage<T>> xmax, List<NodeUsage<T>> ymax) {
	    this.xmin = xmin;
	    this.ymin = ymin;
	    this.xmax = xmax;
	    this.ymax = ymax;
	}
	
	@Override public String toString () {
	    return getClass ().getSimpleName () + 
		"{" + 
		"xmin: " + xmin + ", ymin: " + ymin + 
		"xmax: " + xmax + ", ymax: " + ymax + 
		"}";
	}
    }

    private class NodeGetter<T> {
	private ListData<T> data;
	private int taken = 0;
	private int size;
	private int id;

	// indexes for list scanning
	private int xlow;  // goes up as we pick nodes
	private int ylow;
	private int xhigh;
	private int yhigh;

	/** 
	 * @param data the lists to grab node data from
	 * @param id the id of the nodes we may pick
	 * @param size the number of nodes we may pick
	 * @param xlow the lower start index for the xlow list 
	 * @param ylow the lower start index for the ylow list
	 * @param xhigh the lower start index for the xmax list
	 * @param yhigh the lower start index for the ymax list
	 */
	public NodeGetter (ListData<T> data, int id, int size,
			   int xlow, int ylow, int xhigh, int yhigh) {
	    this.data = data;
	    this.id = id;
	    this.size = size;
	    this.xlow = xlow;
	    this.ylow = ylow;
	    this.xhigh = xhigh;
	    this.yhigh = yhigh;
	}

	@Override public String toString () {
	    return getClass ().getSimpleName () + "{taken: " + taken +
		", size: " + size + ", id: " + id + ", xlow: " + xlow +
		", ylow: " + ylow + ", xhigh: " + xhigh + ", yhigh: " + 
		yhigh + ", data: " + data + "}";
	}

	public boolean hasMoreData () {
	    return elementsLeft () > 0;
	}

	public int elementsLeft () {
	    return size - taken;
	}

	private boolean isUsedNode (List<NodeUsage<T>> ls, int pos) {
	    NodeUsage<T> nu = ls.get (pos);
	    return nu == null || nu.isUsed () || nu.getUser () != id;
	}

	private int findNextFree (List<NodeUsage<T>> ls, int pos) {
	    while (pos < ls.size () && isUsedNode (ls, pos))
		pos++;
	    return pos;
	}
	
	private NodeUsage<T> getFirstUnusedXNode () {
	    xlow = findNextFree (data.xmin, xlow);
	    return data.xmin.get (xlow++);
	}

	public T getFirstUnusedX () {
	    taken++;
	    NodeUsage<T> nu = getFirstUnusedXNode ();
	    nu.use ();
	    data.xmin.set (xlow - 1, null);
	    return nu.getData ();
	}

	public T getFirstUnusedY () {
	    taken++;
	    ylow = findNextFree (data.ymin, ylow);
	    NodeUsage<T> nu = data.ymin.set (ylow++, null);
	    nu.use ();
	    return nu.getData ();
	}

	public T getLastUnusedX () {
	    taken++;
	    xhigh = findNextFree (data.xmax, xhigh);
	    NodeUsage<T> nu = data.xmax.set (xhigh++, null);
	    nu.use ();
	    return nu.getData ();
	}

	public T getLastUnusedY () {
	    taken++;
	    yhigh = findNextFree (data.ymax, yhigh);
	    NodeUsage<T> nu = data.ymax.set (yhigh++, null);
	    nu.use ();
	    return nu.getData ();
	}

	/** Split the remaining data into two parts,
	 *  one part with the low x values and one with the high x values.
	 */
	public List<NodeGetter<T>> split () {
	    int e = elementsLeft ();
	    int lowSize = (e + 1) / 2;
	    int highSize = e - lowSize;
	    int lowId = listHolderId++;
	    int highId = listHolderId++;

	    // save positions
	    int xmn = xlow;
	    int ymn = ylow;
	    int xmx = xhigh;
	    int ymx = yhigh;

	    // mark half the elements for lowId and half for highId
	    for (int i = 0; i < lowSize; i++)
		markForId (lowId);
	    for (int i = 0; i < highSize; i++)
		markForId (highId);

	    NodeGetter<T> lhLow =
		new NodeGetter<T> (data, lowId, lowSize, xmn, ymn, xmx, ymx);
	    NodeGetter<T> lhHigh =
		new NodeGetter<T> (data, highId, highSize, xmn, ymn, xmx, ymx);
	    
	    List<NodeGetter<T>> ret = new ArrayList<NodeGetter<T>> (2);
	    ret.add (lhLow);
	    ret.add (lhHigh);
	    return ret;
	}

	private void markForId (int id) {
	    taken++;
	    NodeUsage<T> nu = getFirstUnusedXNode ();
	    nu.setUser (id);	
	}
    }

    /** Construct the four edge nodes then split the rests of the nodes 
     *  in the middle and loop with the two middle sets.
     */
    private <T, N> void internalBuildLeafs (List<NodeGetter<T>> toExpand,
					    List<N> leafNodes,
					    NodeFactory<N> nf) {
	while (!toExpand.isEmpty ()) {
	    NodeGetter<T> lh = toExpand.remove (0);
	    if (lh.hasMoreData ())
		leafNodes.add (getLowXNode (lh, nf));
	    
	    if (lh.hasMoreData ())
		leafNodes.add (getLowYNode (lh, nf));

	    if (lh.hasMoreData ())
		leafNodes.add (getHighXNode (lh, nf));

	    if (lh.hasMoreData ())
		leafNodes.add (getHighYNode (lh, nf));

	    if (lh.hasMoreData ()) {
		List<NodeGetter<T>> splitted = lh.split ();
		toExpand.addAll (splitted);
	    }
	}
    }

    /** Get the size of the node we are about to create, 
     *  limit by elements left and by branchfactor
     */
    private <T> int getNum (NodeGetter<T> lh) {
	return Math.min (lh.elementsLeft (), branchFactor);
    }

    private <T, N> N getLowXNode (NodeGetter<T> lh, NodeFactory<N> nf) {
	Object[] data = new Object[getNum (lh)];
	for (int i = 0; i < data.length; i++)
	    data[i] = lh.getFirstUnusedX ();
	return nf.create (data);
    }

    private <T, N> N getLowYNode (NodeGetter<T> lh, NodeFactory<N> nf) {
	Object[] data = new Object[getNum (lh)];
	for (int i = 0; i < data.length; i++)
	    data[i] = lh.getFirstUnusedY ();
	return nf.create (data);
    }

    private <T, N> N getHighXNode (NodeGetter<T> lh, NodeFactory<N> nf) {
	Object[] data = new Object[getNum (lh)];
	for (int i = 0; i < data.length; i++)
	    data[i] = lh.getLastUnusedX ();
	return nf.create (data);
    }

    private <T, N> N getHighYNode (NodeGetter<T> lh, NodeFactory<N> nf) {
	Object[] data = new Object[getNum (lh)];
	for (int i = 0; i < data.length; i++)
	    data[i] = lh.getLastUnusedY ();
	return nf.create (data);
    }
}
