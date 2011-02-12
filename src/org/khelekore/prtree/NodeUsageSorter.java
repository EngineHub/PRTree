package org.khelekore.prtree;

import java.util.Comparator;

/** Sort NodeUsage elements on their data elements.
 * @param <T> the type of the nodes user data
 */
class NodeUsageSorter<T> implements Comparator<NodeUsage<T>> {
    private Comparator<T> sorter;

    public NodeUsageSorter (Comparator<T> sorter) {
	this.sorter = sorter;
    }

    public int compare (NodeUsage<T> n1, NodeUsage<T> n2) {
	return sorter.compare (n1.getData (), n2.getData ());
    }
}
