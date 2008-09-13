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

    /** A factory that creates the nodes (either leaf or internal).
     */
    public interface NodeFactory<N> {
	/** Create a new node 
	 * @param data the data entries for the node, fully filled.
	 */
	N create (Object[] data);
    }

    public <T, N> void buildLeafs (List<? extends T> ls, List<N> leafNodes,
				   Comparator<T> xSorter,
				   Comparator<T> ySorter,
				   NodeFactory<N> nf) {
	/** To not waste so much memory we create two lists, sorted by xmin 
	 *  and ymin respectivly. The two lists hold the same objects so we
	 *  can modify the usage info from either list.
	 */
	listHolderId = 1;
	List<NodeUsage<T>> lsx = new ArrayList<NodeUsage<T>> (ls.size ());
	List<NodeUsage<T>> lsy = new ArrayList<NodeUsage<T>> (ls.size ());

	for (T t : ls) {
	    NodeUsage<T> nu = new NodeUsage<T> (t);
	    lsx.add (nu);
	    lsy.add (nu);
	}

	Collections.sort (lsx, new NodeUsageSorter<T> (xSorter));
	Collections.sort (lsy, new NodeUsageSorter<T> (ySorter));
	List<NodeGetter<T>> toExpand = new ArrayList<NodeGetter<T>> ();
	ListData<T> listData = new ListData<T> (lsx, lsy);
	int top = lsx.size () - 1;
	toExpand.add (new NodeGetter<T> (listData, listHolderId++, lsx.size (),
					 0, 0, top, top));;
	internalBuildLeafs (toExpand, leafNodes, nf);
    }

    private static class ListData<T> {
	// Same NodeUsage objects in both lists, just ordered differently.
	public List<NodeUsage<T>> sx;
	public List<NodeUsage<T>> sy;

	public ListData (List<NodeUsage<T>> sx, List<NodeUsage<T>> sy) {
	    this.sx = sx;
	    this.sy = sy;
	}
	
	@Override public String toString () {
	    return getClass ().getSimpleName () + 
		"{sx: " + sx + ", sy: " + sy + "}";
	}
    }

    private class NodeGetter<T> {
	private ListData<T> data;
	private int taken = 0;
	private int size;
	private int id;

	// indexes for list scanning
	private int xlow;  // goes up as we pick low nodes
	private int ylow;
	private int xhigh; // goes down as we pick high nodes
	private int yhigh;

	/** 
	 * @param data the lists to grab node data from
	 * @param id the id of the nodes we may pick
	 * @param size the number of nodes we may pick
	 * @param xlow the lower start index for the x list 
	 * @param ylow the lower start index for the x list
	 * @param xhigh the upper start index for the x list
	 * @param yhigh the upper start index for the x list
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

	private NodeUsage<T> getFirstUnusedXNode () {
	    while (xlow < xhigh && isUsedNode (data.sx, xlow))
		xlow++;
	    return data.sx.get (xlow++);
	}

	public T getFirstUnusedX () {
	    taken++;
	    NodeUsage<T> nu = getFirstUnusedXNode ();
	    nu.use ();
	    data.sx.set (xlow - 1, null);
	    return nu.getData ();
	}

	public T getFirstUnusedY () {
	    taken++;
	    while (ylow < yhigh && isUsedNode (data.sy, ylow))
		ylow++;
	    NodeUsage<T> nu = data.sy.set (ylow++, null);
	    nu.use ();
	    return nu.getData ();
	}

	private NodeUsage<T> getLastUnusedXNode () {
	    while (xhigh > xlow && isUsedNode (data.sx, xhigh))
		xhigh--;
	    return data.sx.get (xhigh--);
	}

	public T getLastUnusedX () {
	    taken++;
	    NodeUsage<T> nu = getLastUnusedXNode ();
	    nu.use ();
	    data.sx.set (xlow - 1, null);
	    return nu.getData ();
	}

	public T getLastUnusedY () {
	    taken++;
	    while (yhigh > ylow && isUsedNode (data.sy, yhigh))
		yhigh--;
	    NodeUsage<T> nu = data.sy.set (yhigh--, null);
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
	    int xl = xlow;
	    int xh = xhigh;

	    // pick a low element to the low list, mark as low,
	    // pick a high element to the high list, mark as high
	    while (hasMoreData ()) {
		taken++;
		NodeUsage<T> nu = getFirstUnusedXNode ();
		nu.setUser (lowId);
		if (hasMoreData ()) {
		    taken++;
		    nu = getLastUnusedXNode ();
		    nu.setUser (highId);
		}
	    }

	    NodeGetter<T> lhLow =
		new NodeGetter<T> (data, lowId, lowSize, 
				   xl, ylow, xhigh, yhigh);
	    NodeGetter<T> lhHigh =
		new NodeGetter<T> (data, highId, highSize, 
				   xlow, ylow, xh, yhigh);
	    List<NodeGetter<T>> ret = new ArrayList<NodeGetter<T>> (2);
	    ret.add (lhLow);
	    ret.add (lhHigh);
	    return ret;
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
