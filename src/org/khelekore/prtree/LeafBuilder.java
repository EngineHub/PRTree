package org.khelekore.prtree;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

class LeafBuilder {

    private int branchFactor;

    public LeafBuilder (int branchFactor) {
	this.branchFactor = branchFactor;
    }

    public interface NodeFactory<N, T> {
	/** Create a new node */
	N create ();
	/** Add a data element to the node */
	void add (N node, T data);
    }

    private static class NodeUsage<T> {
	private T data;
	private byte used = 0;

	public NodeUsage (T data) {
	    this.data = data;
	}
	
	public T getData () {
	    return data;
	}
	
	public void use () {
	    used = 1;
	}

	public void useLow () {
	    used = 2;
	}

	public void useHigh () {
	    used = 4;
	}

	public void clearUsage () {
	    used = 0;
	}

	public boolean isUsed () {
	    return used != 0;
	}

	public boolean isUsedLow () {
	    return used == 2;
	}

	public boolean isUsedHigh () {
	    return used == 4;
	}
    }

    private static class NodeUsageSorter<T> implements Comparator<NodeUsage<T>> {
	private Comparator<T> sorter;
	
	public NodeUsageSorter (Comparator<T> sorter) {
	    this.sorter = sorter;
	}
	
	public int compare (NodeUsage<T> n1, NodeUsage<T> n2) {
	    return sorter.compare (n1.getData (), n2.getData ());
	}
    }

    public <T, N> void buildLeafs (List<T> ls, List<N> leafNodes,
				   Comparator<T> xSorter,
				   Comparator<T> ySorter,
				   NodeFactory<N, T> nf) {
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
	toExpand.add (new ListHolder<T> (lsx, lsy));
	internalBuildLeafs (toExpand, leafNodes, nf);
    }

    private static class ListHolder<T> {
	public List<NodeUsage<T>> sx;
	public List<NodeUsage<T>> sy;
	private int xlow = 0;
	private int ylow = 0;
	private int xhigh;
	private int yhigh;
	private int taken = 0;

	public ListHolder (List<NodeUsage<T>> sx, List<NodeUsage<T>> sy) {
	    this.sx = sx;
	    this.sy = sy;
	    xhigh = sx.size () - 1;
	    yhigh = xhigh;
	}

	public List<NodeUsage<T>> getUnusedElements () {
	    ArrayList<NodeUsage<T>> ls = 
		new ArrayList<NodeUsage<T>> (elementsLeft ());
	    for (NodeUsage<T> nu : sx)
		if (!nu.isUsed ())
		    ls.add (nu);
	    return ls;
	}
	
	public boolean hasMoreData () {
	    return elementsLeft () > 0;
	}

	public int elementsLeft () {
	    return sx.size () - taken;
	}

	private NodeUsage<T> getFirstUnusedXNode () {
	    while (xlow < xhigh && sx.get (xlow).isUsed ())
		xlow++;
	    return sx.get (xlow++);
	}
	
	public T getFirstUnusedX () {
	    taken++;
	    NodeUsage<T> nu = getFirstUnusedXNode ();
	    nu.use ();
	    return nu.getData ();
	}

	public T getFirstUnusedY () {
	    taken++;
	    while (ylow < yhigh && sy.get (ylow).isUsed ())
		ylow++;
	    NodeUsage<T> nu = sy.get (ylow++);
	    nu.use ();
	    return nu.getData ();
	}

	private NodeUsage<T> getLastUnusedXNode () {
	    while (xhigh > xlow && sx.get (xhigh).isUsed ())
		xhigh--;
	    return sx.get (xhigh--);
	}
	
	public T getLastUnusedX () {
	    taken++;
	    NodeUsage<T> nu = getLastUnusedXNode ();
	    nu.use ();
	    return nu.getData ();
	}

	public T getLastUnusedY () {
	    taken++;
	    while (yhigh > ylow && sy.get (yhigh).isUsed ())
		yhigh--;
	    NodeUsage<T> nu = sy.get (yhigh--);
	    nu.use ();
	    return nu.getData ();
	}

	/** Split the remaining data into two parts, 
	 *  one part with the low x values and one with the high x values.
	 */
	public List<ListHolder<T>> split () {
	    int e = elementsLeft ();
	    int sizeLow = (e + 1) / 2;
	    int sizeHigh = e - sizeLow;
	    List<NodeUsage<T>> lowX = new ArrayList<NodeUsage<T>> (sizeLow);
	    List<NodeUsage<T>> highX = new ArrayList<NodeUsage<T>> (sizeHigh);
	    
	    // fill with null so that we can set it from the back later on.
	    for (int i = 0; i < sizeHigh; i++)
		highX.add (null);
	    
	    int highPos = sizeHigh - 1;
	    while (hasMoreData ()) {
		taken++;
		NodeUsage<T> nu = getFirstUnusedXNode ();
		nu.useLow ();
		lowX.add (nu);
		if (hasMoreData ()) {
		    taken++;
		    nu = getLastUnusedXNode ();
		    nu.useHigh ();
		    highX.set (highPos--, nu);
		}
	    }
	    sx = null;

	    List<NodeUsage<T>> lowY = new ArrayList<NodeUsage<T>> (sizeLow);
	    List<NodeUsage<T>> highY = new ArrayList<NodeUsage<T>> (sizeHigh);
	    for (NodeUsage<T> nu : sy) {
		if (nu.isUsedLow ())
		    lowY.add (nu);
		else if (nu.isUsedHigh ())
		    highY.add (nu);
		nu.clearUsage ();
	    }
	    sy = null;

	    ListHolder<T> lhLow = new ListHolder<T> (lowX, lowY);
	    ListHolder<T> lhHigh = new ListHolder<T> (highX, highY);
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
