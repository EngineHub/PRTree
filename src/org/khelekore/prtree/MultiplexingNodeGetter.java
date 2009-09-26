package org.khelekore.prtree;

import java.util.ArrayList;
import java.util.List;

/** A node getter that multiplexes nodes from one or more NodeGetters.
 * @param <N> the type of the node
 */
public class MultiplexingNodeGetter<N> implements NodeGetter<N> {
    private final List<NodeGetter<N>> getters;
    private int pos;

    public MultiplexingNodeGetter (NodeGetter<N> n1, NodeGetter<N> n2) {
	getters = new ArrayList<NodeGetter<N>> (2);
	getters.add (n1);
	getters.add (n2);
    }

    public N getNextNode (int maxObjects) {
	NodeGetter<N> getter = getters.get (pos++);
	pos %= getters.size ();
	return getter.getNextNode (maxObjects);
    }    
}