package org.khelekore.prtree;

import java.util.ArrayList;
import java.util.List;

/** A node getter that multiplexes nodes from one or more NodeGetters.
 * @param <N> the type of the node
 */
class MultiplexingNodeGetter<T, N> implements NodeGetter<N> {
    private final List<MinMaxNodeGetter<T, N>> getters;
    private int pos;

    public MultiplexingNodeGetter (MinMaxNodeGetter<T, N> n1, 
				   MinMaxNodeGetter<T, N> n2) {
	getters = new ArrayList<MinMaxNodeGetter<T, N>> (2);
	getters.add (n1);
	getters.add (n2);
    }

    private MultiplexingNodeGetter (MinMaxNodeGetter<T, N> n) {
	getters = new ArrayList<MinMaxNodeGetter<T, N>> (3);
	getters.add (n);
    }

    private void add (MinMaxNodeGetter<T, N> mm) {
	getters.add (mm);
    }

    public N getNextNode (int maxObjects) {
	NodeGetter<N> getter = getters.get (pos++);
	pos %= getters.size ();
	return getter.getNextNode (maxObjects);
    }
 
    public boolean hasMoreNodes () {
	return getters.get (pos).hasMoreNodes ();
    }

    public boolean hasMoreData () {
	return getters.get (pos).hasMoreData ();
    }

    public List<MultiplexingNodeGetter<T, N>> split (int lowId, int highId) {
	MinMaxNodeGetter<T, N> first = getters.get (0);
	
	List<MinMaxNodeGetter<T, N>> splitted = first.split (lowId, highId);
	MinMaxNodeGetter<T, N> splitLow = splitted.get (0);
	MultiplexingNodeGetter<T, N> pLow = 
	    new MultiplexingNodeGetter<T, N> (splitLow);
	MinMaxNodeGetter<T, N> splitHigh = splitted.get (1);
	MultiplexingNodeGetter<T, N> pHigh = 
	    new MultiplexingNodeGetter<T, N> (splitHigh);
	
	TakeCounter tcLow = splitLow.getTakeCounter ();
	TakeCounter tcHigh = splitHigh.getTakeCounter ();

	for (int i = 1; i < getters.size (); i++) {
	    MinMaxNodeGetter<T, N> g = getters.get (i);
	    pLow.add (g.getCopyFor (lowId, splitLow.getSize (), tcLow));
	    pHigh.add (g.getCopyFor (highId, splitHigh.getSize (), tcHigh));
	}

	List<MultiplexingNodeGetter<T, N>> ret = 
	    new ArrayList<MultiplexingNodeGetter<T, N>> (2);
	ret.add (pLow);
	ret.add (pHigh);
	return ret;
    }
}