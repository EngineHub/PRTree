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
    public interface NodeFactory<N, T> {
	/** Create a new node */
	N create ();

	/** Add a data element to the node */
	void add (N node, T data);
    }

    /** Information needed to be able to figure out how an
     *  element of the tree is currently used.
     */
    private static class NodeUsage<T> {
	private T data;
	private short user;
	private boolean used;

	public NodeUsage (T data) {
	    this.data = data;
	}

	public T getData () {
	    return data;
	}

	public void use () {
	    used = true;
	}

	public boolean isUsed () {
	    return used;
	}

	public void setUser (int id) {
	    user = (short)id;
	}

	public int getUser () {
	    return (user & 0xffff);
	}

	@Override public String toString () {
	    return getClass ().getSimpleName () + "{data: " + data +
		", used: " + isUsed () + ", user: " + getUser () + "}";
	}
    }

    /** Sort NodeUsage elements on their data elements.
     */
    private static class NodeUsageSorter<T> implements Comparator<NodeUsage<T>> {
	private Comparator<T> sorter;

	public NodeUsageSorter (Comparator<T> sorter) {
	    this.sorter = sorter;
	}

	public int compare (NodeUsage<T> n1, NodeUsage<T> n2) {
	    return sorter.compare (n1.getData (), n2.getData ());
	}
    }

    public <T, N> void buildLeafs (List<? extends T> ls, List<N> leafNodes,
				   Comparator<T> xSorter,
				   Comparator<T> ySorter,
				   NodeFactory<N, T> nf) {
	listHolderId = 0;
	List<NodeUsage<T>> lsx = new ArrayList<NodeUsage<T>> (ls.size ());
	List<NodeUsage<T>> lsy = new ArrayList<NodeUsage<T>> (ls.size ());

	for (T t : ls) {
	    NodeUsage<T> nu = new NodeUsage<T> (t);
	    lsx.add (nu);
	    lsy.add (nu);
	}

	Collections.sort (lsx, new NodeUsageSorter<T> (xSorter));
	Collections.sort (lsy, new NodeUsageSorter<T> (ySorter));
	List<ListHolder<T>> toExpand = new ArrayList<ListHolder<T>> ();
	ListData<T> listData = new ListData<T> (lsx, lsy);
	int top = lsx.size () - 1;
	toExpand.add (new ListHolder<T> (listData, listHolderId++, lsx.size (),
					 0, 0, top, top));;
	internalBuildLeafs (toExpand, leafNodes, nf);
    }

    private class ListData<T> {
	// Same NodeUsage objects in both lists, just ordered differently.
	public List<NodeUsage<T>> sx;
	public List<NodeUsage<T>> sy;

	public ListData (List<NodeUsage<T>> sx, List<NodeUsage<T>> sy) {
	    this.sx = sx;
	    this.sy = sy;
	}
    }

    private class ListHolder<T> {
	private ListData<T> data;
	private int taken = 0;
	private int size;
	private int id;

	private int xlow;
	private int ylow;
	private int xhigh;
	private int yhigh;

	public ListHolder (ListData<T> data, int id, int size,
			   int xlow, int ylow, int xhigh, int yhigh) {
	    this.data = data;
	    this.id = id;
	    this.size = size;
	    this.xlow = xlow;
	    this.ylow = ylow;
	    this.xhigh = xhigh;
	    this.yhigh = yhigh;
	}

	public boolean hasMoreData () {
	    return elementsLeft () > 0;
	}

	public int elementsLeft () {
	    return size - taken;
	}

	private boolean isUsedNode (List<NodeUsage<T>> ls, int pos) {
	    NodeUsage<T> nu = ls.get (pos);
	    return nu.isUsed () || nu.getUser () != id;
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
	    return nu.getData ();
	}

	public T getFirstUnusedY () {
	    taken++;
	    while (ylow < yhigh && isUsedNode (data.sy, ylow))
		ylow++;
	    NodeUsage<T> nu = data.sy.get (ylow++);
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
	    return nu.getData ();
	}

	public T getLastUnusedY () {
	    taken++;
	    while (yhigh > ylow && isUsedNode (data.sy, yhigh))
		yhigh--;
	    NodeUsage<T> nu = data.sy.get (yhigh--);
	    nu.use ();
	    return nu.getData ();
	}

	/** Split the remaining data into two parts,
	 *  one part with the low x values and one with the high x values.
	 */
	public List<ListHolder<T>> split () {
	    int e = elementsLeft ();
	    int lowSize = (e + 1) / 2;
	    int highSize = e - lowSize;
	    int lowId = listHolderId++;
	    int highId = listHolderId++;

	    // save positions
	    int xl = xlow;
	    int yl = ylow;
	    int xh = xhigh;
	    int yh = yhigh;

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

	    ListHolder<T> lhLow =
		new ListHolder<T> (data, lowId, lowSize, xl, yl, xh, yh);
	    ListHolder<T> lhHigh =
		new ListHolder<T> (data, highId, highSize, xl, yl, xh, yh);
	    List<ListHolder<T>> ret = new ArrayList<ListHolder<T>> (2);
	    ret.add (lhLow);
	    ret.add (lhHigh);
	    return ret;
	}
    }

    private <T, N> void internalBuildLeafs (List<ListHolder<T>> toExpand,
					    List<N> leafNodes,
					    NodeFactory<N, T> nf) {
	while (!toExpand.isEmpty ()) {
	    ListHolder<T> lh = toExpand.remove (0);
	    if (lh.hasMoreData ())
		leafNodes.add (getLowXNode (lh, nf));

	    if (lh.hasMoreData ())
		leafNodes.add (getLowYNode (lh, nf));

	    if (lh.hasMoreData ())
		leafNodes.add (getHighXNode (lh, nf));

	    if (lh.hasMoreData ())
		leafNodes.add (getHighYNode (lh, nf));

	    if (lh.hasMoreData ()) {
		List<ListHolder<T>> splitted = lh.split ();
		toExpand.addAll (splitted);
	    }
	}
    }

    private <T> int getNum (ListHolder<T> lh) {
	int s = lh.elementsLeft ();
	if (s > branchFactor)
	    return branchFactor;
	return s;
    }

    private <T, N> N getLowXNode (ListHolder<T> lh, NodeFactory<N, T> nf) {
	N node = nf.create ();
	for (int i = 0, s = getNum (lh); i < s; i++)
	    nf.add (node, lh.getFirstUnusedX ());
	return node;
    }

    private <T, N> N getLowYNode (ListHolder<T> lh, NodeFactory<N, T> nf) {
	N node = nf.create ();
	for (int i = 0, s = getNum (lh); i < s; i++)
	    nf.add (node, lh.getFirstUnusedY ());
	return node;
    }

    private <T, N> N getHighXNode (ListHolder<T> lh, NodeFactory<N, T> nf) {
	N node = nf.create ();
	for (int i = 0, s = getNum (lh); i < s; i++)
	    nf.add (node, lh.getLastUnusedX ());
	return node;
    }

    private <T, N> N getHighYNode (ListHolder<T> lh, NodeFactory<N, T> nf) {
	N node = nf.create ();
	for (int i = 0, s = getNum (lh); i < s; i++)
	    nf.add (node, lh.getLastUnusedY ());
	return node;
    }
}
