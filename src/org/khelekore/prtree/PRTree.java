package org.khelekore.prtree;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;

public class PRTree<T> {

    private MBRConverter<T> converter;
    private int branchFactor;

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
	List<LeafNode> leafNodes = new ArrayList<LeafNode> ();
	buildLeafs (ls, leafNodes);

	if (leafNodes.size () > branchFactor) {
	}
    }

    private void buildLeafs (List<T> ls, List<LeafNode> leafNodes) {
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
			     List<LeafNode> leafNodes) {
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

    private LeafNode getLowLeafNode (LinkedHashMap<T, T> lsBase, 
					LinkedHashMap<T, T> sx, 
					LinkedHashMap<T, T> sy) {
	List<T> ls = new ArrayList<T> (lsBase.keySet ());
	LeafNode vxMin = new LeafNode (branchFactor);
	fillLow (vxMin, getNum (ls), ls, sx, sy);
	return vxMin;
    }

    private LeafNode getHighLeafNode (LinkedHashMap<T, T> lsBase, 
					 LinkedHashMap<T, T> sx, 
					 LinkedHashMap<T, T> sy) {
	List<T> ls = new ArrayList<T> (lsBase.keySet ());
	LeafNode vxMin = new LeafNode (branchFactor);
	fillHigh (vxMin, getNum (ls), ls, sx, sy);
	return vxMin;
    }

    private void fillLow (LeafNode ln, int num, List<T> ls, 
			  LinkedHashMap<T, T> sx, LinkedHashMap<T, T> sy) {
	for (int i = 0; i < num; i++) {
	    T t = ls.get (i);
	    ln.add (t);
	    sx.remove (t);
	    sy.remove (t);
	}
    }

    private void fillHigh (LeafNode ln, int num, List<T> ls, 
			   LinkedHashMap<T, T> sx, LinkedHashMap<T, T> sy) {
	int s = ls.size ();
	for (int i = 0; i < num; i++) {
	    T t = ls.get (s - i -1);
	    ln.add (t);
	    sx.remove (t);
	    sy.remove (t);
	}
    }

    private interface Node {
	MBR getMBR ();
    }

    private abstract class NodeBase<N> extends ArrayList<N> implements Node {
	private MBR mbr;
	
	public NodeBase (int size) {
	    super (size);
	}

	public MBR getMBR () {
	    if (mbr == null)
		mbr = computeMBR ();
	    return mbr;
	}
	
	public abstract MBR computeMBR ();
    }

    private class LeafNode extends NodeBase<T> {
	public LeafNode (int size) {
	    super (size);
	}

	@Override public MBR computeMBR () {
	    return null; // qwerty TODO: implement 
	}
    }

    private class InternalNode extends NodeBase<InternalNode> {
	public InternalNode (int size) {
	    super (size);
	}

	@Override public MBR computeMBR () {
	    return null; // qwerty TODO: implement 
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
     */
    public Iterable<T> find (double xmin, double ymin,
			     double xmax, double ymax) {
	return Collections.<T>emptyList ();
    }
}
