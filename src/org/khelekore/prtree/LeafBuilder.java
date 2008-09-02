package org.khelekore.prtree;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
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

    public <T, N> void buildLeafs (List<T> ls, List<N> leafNodes,
				   Comparator<T> xSorter,
				   Comparator<T> ySorter,
				   NodeFactory<N, T> nf) {
	Collections.sort (ls, xSorter);
	LinkedHashMap<T, T> rsx = getLHM (ls);
	Collections.sort (ls, ySorter);
	LinkedHashMap<T, T> rsy = getLHM (ls);
	buildLeafs (rsx, rsy, leafNodes, xSorter, ySorter, nf);
    }

    private <T> LinkedHashMap<T, T> getLHM (List<T> sorted) {
	LinkedHashMap<T, T> ret = new LinkedHashMap<T, T> (sorted.size ());
	for (T t : sorted)
	    ret.put (t, t);
	return ret;
    }

    private <T, N> void buildLeafs (LinkedHashMap<T, T> sx,
				    LinkedHashMap<T, T> sy,
				    List<N> leafNodes,
				    Comparator<T> xSorter,
				    Comparator<T> ySorter,
				    NodeFactory<N, T> nf) {
	if (sx.size () > 0)
	    leafNodes.add (getLowLeafNode (sx, sx, sy, nf));

	if (sy.size () > 0)
	    leafNodes.add (getLowLeafNode (sy, sx, sy, nf));

	if (sx.size () > 0)
	    leafNodes.add (getHighLeafNode (sx, sx, sy, nf));

	if (sy.size () > 0)
	    leafNodes.add (getHighLeafNode (sy, sx, sy, nf));

	if (sx.size () > 0) {
	    List<T> ls = new ArrayList<T> (sx.keySet ());
	    int s = ls.size () / 2;
	    LinkedHashMap<T, T> lowX = getLHM (ls.subList (0, s));
	    LinkedHashMap<T, T> lowY = getY (sy, lowX);
	    LinkedHashMap<T, T> highX = getLHM (ls.subList (s, ls.size ()));
	    LinkedHashMap<T, T> highY = getY (sy, lowX);
	    buildLeafs (lowX, lowY, leafNodes, xSorter, ySorter, nf);
	    buildLeafs (highX, highY, leafNodes, xSorter, ySorter, nf);
	}
    }

    private <T> LinkedHashMap<T, T> getY (LinkedHashMap<T, T> fullY, LinkedHashMap<T, T> parts) {
	LinkedHashMap<T, T> ret = new LinkedHashMap<T, T> (parts.size ());
	for (T t : fullY.keySet ())
	    if (parts.get (t) != null)
		ret.put (t, t);
	return ret;
    }

    private <T> int getNum (List<T> ls) {
	int s = ls.size ();
	if (s > branchFactor)
	    return branchFactor;
	return s;
    }

    private <T, N> N getLowLeafNode (LinkedHashMap<T, T> lsBase,
				     LinkedHashMap<T, T> sx,
				     LinkedHashMap<T, T> sy,
				     NodeFactory<N, T> nf) {
	List<T> ls = new ArrayList<T> (lsBase.keySet ());
	N vxMin = nf.create ();
	fillLow (vxMin, getNum (ls), ls, sx, sy, nf);
	return vxMin;
    }

    private <T, N> N getHighLeafNode (LinkedHashMap<T, T> lsBase,
				      LinkedHashMap<T, T> sx,
				      LinkedHashMap<T, T> sy,
				      NodeFactory<N, T> nf) {
	List<T> ls = new ArrayList<T> (lsBase.keySet ());
	N vxMin = nf.create ();
	fillHigh (vxMin, getNum (ls), ls, sx, sy, nf);
	return vxMin;
    }

    private <T, N> void fillLow (N ln, int num, List<T> ls,
				 LinkedHashMap<T, T> sx,
				 LinkedHashMap<T, T> sy,
				 NodeFactory<N, T> nf) {
	for (int i = 0; i < num; i++) {
	    T t = ls.get (i);
	    nf.add (ln, t);
	    sx.remove (t);
	    sy.remove (t);
	}
    }

    private <T, N> void fillHigh (N ln, int num, List<T> ls,
				  LinkedHashMap<T, T> sx,
				  LinkedHashMap<T, T> sy,
				  NodeFactory<N, T> nf) {
	int s = ls.size ();
	for (int i = 0; i < num; i++) {
	    T t = ls.get (s - i -1);
	    nf.add (ln, t);
	    sx.remove (t);
	    sy.remove (t);
	}
    }
}
