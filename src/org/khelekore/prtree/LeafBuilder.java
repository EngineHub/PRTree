package org.khelekore.prtree;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Collection;
import java.util.List;

/** A builder of internal nodes used during bulk loading of a PR-Tree.
 *  A PR-Tree is build by building a pseudo R-Tree and grabbing the
 *  leaf nodes (and then repeating until you have just one root node).
 *  This class creates the leaf nodes without building the full pseudo tree.
 */
class LeafBuilder {

    private int branchFactor;

    public LeafBuilder (int branchFactor) {
	this.branchFactor = branchFactor;
    }

    public <T, N> void buildLeafs (Collection<? extends T> ls,
				   List<N> leafNodes,
				   Comparator<T> xMinSorter,
				   Comparator<T> yMinSorter,
				   Comparator<T> xMaxSorter,
				   Comparator<T> yMaxSorter,
				   NodeFactory<N> nf) {
	List<NodeUsage<T>> nodes = new ArrayList<NodeUsage<T>> (ls.size ());
	for (T t : ls)
	    nodes.add (new NodeUsage<T> (t));

	TakeCounter tc = new TakeCounter (ls.size ());
	int id = 1;
	MinMaxNodeGetter<T, N> mmx =
	    getMM (nodes, nf, xMinSorter, xMaxSorter, tc, id);
	MinMaxNodeGetter<T, N> mmy =
	    getMM (nodes, nf, yMinSorter, yMaxSorter, tc, id);

	MultiplexingNodeGetter<T, N> plex =
	    new MultiplexingNodeGetter<T, N> (mmx, mmy);

	doExpansion (leafNodes, id, plex);
    }

    private <N> void doExpansion (List<N> leafNodes, int id,
				  NodeGetter<N> plex) {
	List<NodeGetter<N>> toExpand = new ArrayList<NodeGetter<N>> ();
	toExpand.add (plex);

	while (!toExpand.isEmpty ()) {
	    NodeGetter<N> ng = toExpand.remove (0);
	    while (ng.hasMoreNodes ()) {
		leafNodes.add (ng.getNextNode (branchFactor));
	    }
	    if (ng.hasMoreData ()) {
		int lowId = id++;
		int highId = id++;
		toExpand.addAll (ng.split (lowId, highId));
	    }
	}
    }

    private <T, N> MinMaxNodeGetter<T, N> getMM (List<NodeUsage<T>> nodes,
						 NodeFactory<N> nf,
						 Comparator<T> min,
						 Comparator<T> max,
						 TakeCounter tc,
						 int id) {
	return new MinMaxNodeGetter<T, N> (nodes, nf, min, max, tc, id);
    }
}
