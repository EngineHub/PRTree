package org.khelekore.prtree;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
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
	Collections.sort (ls, new OrdComparator (0));
	List<LeafNode<T>> leafNodes = new ArrayList<LeafNode<T>> ();
	buildLeafs (ls, leafNodes);

	if (leafNodes.size () > branchFactor) {
	    // TODO: build
	    root = Collections.emptyList ();
	} else {
	    root = new InternalNode<T> (leafNodes.size (), converter);
	    for (LeafNode<T> n : leafNodes)
		root.add (n);
	}
    }

    private void buildLeafs (List<T> ls, List<LeafNode<T>> leafNodes) {
	LinkedHashMap<T, T> rsx = getLHM (ls);
	Collections.sort (ls, new OrdComparator (1));
	LinkedHashMap<T, T> rsy = getLHM (ls);
	buildLeafs (rsx, rsy, leafNodes);
    }

    private LinkedHashMap<T, T> getLHM (List<T> sorted) {
	LinkedHashMap<T, T> ret = new LinkedHashMap<T, T> (sorted.size ());
	for (T t : sorted)
	    ret.put (t, t);
	return ret;
    }

    private void buildLeafs (LinkedHashMap<T, T> sx,
			     LinkedHashMap<T, T> sy,
			     List<LeafNode<T>> leafNodes) {
	if (sx.size () > 0)
	    leafNodes.add (getLowLeafNode (sx, sx, sy));

	if (sy.size () > 0)
	    leafNodes.add (getLowLeafNode (sy, sx, sy));

	if (sx.size () > 0)
	    leafNodes.add (getHighLeafNode (sx, sx, sy));

	if (sy.size () > 0)
	    leafNodes.add (getHighLeafNode (sy, sx, sy));

	if (sx.size () > 0) {
	    List<T> ls = new ArrayList<T> (sx.keySet ());
	    int s = ls.size () / 2;
	    List<T> low = ls.subList (0, s);
	    List<T> high = ls.subList (s, ls.size ());
	    buildLeafs (low, leafNodes);
	    buildLeafs (high, leafNodes);
	}
    }

    private int getNum (List<T> ls) {
	int s = ls.size ();
	if (s > branchFactor)
	    return branchFactor;
	return s;
    }

    private LeafNode<T> getLowLeafNode (LinkedHashMap<T, T> lsBase,
				     LinkedHashMap<T, T> sx,
				     LinkedHashMap<T, T> sy) {
	List<T> ls = new ArrayList<T> (lsBase.keySet ());
	LeafNode<T> vxMin = new LeafNode<T> (branchFactor, converter);
	fillLow (vxMin, getNum (ls), ls, sx, sy);
	return vxMin;
    }

    private LeafNode<T> getHighLeafNode (LinkedHashMap<T, T> lsBase,
					 LinkedHashMap<T, T> sx,
					 LinkedHashMap<T, T> sy) {
	List<T> ls = new ArrayList<T> (lsBase.keySet ());
	LeafNode<T> vxMin = new LeafNode<T> (branchFactor, converter);
	fillHigh (vxMin, getNum (ls), ls, sx, sy);
	return vxMin;
    }

    private void fillLow (LeafNode<T> ln, int num, List<T> ls,
			  LinkedHashMap<T, T> sx, LinkedHashMap<T, T> sy) {
	for (int i = 0; i < num; i++) {
	    T t = ls.get (i);
	    ln.add (t);
	    sx.remove (t);
	    sy.remove (t);
	}
    }

    private void fillHigh (LeafNode<T> ln, int num, List<T> ls,
			   LinkedHashMap<T, T> sx, LinkedHashMap<T, T> sy) {
	int s = ls.size ();
	for (int i = 0; i < num; i++) {
	    T t = ls.get (s - i -1);
	    ln.add (t);
	    sx.remove (t);
	    sy.remove (t);
	}
    }

    private class OrdComparator implements Comparator<T> {
	private int ord;
	public OrdComparator (int ord) {
	    this.ord = ord;
	}

	public int compare (T t1, T t2) {
	    double d1 = converter.getMin (t1, ord);
	    double d2 = converter.getMin (t1, ord);
	    return Double.compare (d1, d2);
	}
    }

    /** Find all objects that intersect the given rectangle
     * @throws IllegalArgumentException if xmin &gt; xmax or ymin &gt; ymax 
     */
    public Iterable<T> find (final double xmin, final double ymin,
			     final double xmax, final double ymax) {
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
	    if (xmax < xmin)
		throw new IllegalArgumentException ("xmax: " + xmax + " < xmin: " + xmin);
	    if (ymax < ymin)
		throw new IllegalArgumentException ("ymax: " + ymax + " < ymin: " + ymin);
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
