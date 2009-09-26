package org.khelekore.prtree;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/** A node getter that multiplexes min and max values
 * @param <N> the type of the node
 */
public class MinMaxNodeGetter<T, N> implements NodeGetter<N> {
    private final List<NodeUsage<T>> min;
    private int minPos;
    private final List<NodeUsage<T>> max;
    private int maxPos;
    private final NodeFactory<N> factory;
    private final int id;
    private final int size;
    private int taken = 0;
    private boolean takeMin = true;

    public MinMaxNodeGetter (List<NodeUsage<T>> data,
			     NodeFactory<N> factory,
			     Comparator<T> minSorter,
			     Comparator<T> maxSorter,
			     int id,
			     int size) {
	min = new ArrayList<NodeUsage<T>> (data);
	Collections.sort (min, new NodeUsageSorter<T> (minSorter));
	max = new ArrayList<NodeUsage<T>> (data);
	Collections.sort (max, new NodeUsageSorter<T> (maxSorter));
	this.factory = factory;
	this.id = id;
	this.size = size;
    }

    private boolean isUsedNode (List<NodeUsage<T>> ls, int pos) {
	NodeUsage<T> nu = ls.get (pos);
	return nu == null || nu.isUsed () || nu.getUser () != id;
    }

    private int findNextFree (List<NodeUsage<T>> ls, int pos) {
	while (pos < ls.size () && isUsedNode (ls, pos))
	    pos++;
	return pos;
    }

    private T getFirstUnusedMin () {
	taken++;
	minPos = findNextFree (min, minPos);
	NodeUsage<T> nu = min.set (minPos++, null);
	nu.use ();
	return nu.getData ();
    }

    private T getFirstUnusedMax () {
	taken++;
	maxPos = findNextFree (max, maxPos);
	NodeUsage<T> nu = min.set (maxPos++, null);
	nu.use ();
	return nu.getData ();
    }

    public N getNextNode (int maxObjects) {
	int num = Math.min (size - taken, maxObjects);
	Object[] data = new Object[num];
	for (int i = 0; i < num; i++)
	    data[i] = takeMin ? getFirstUnusedMin () : getFirstUnusedMax ();
	takeMin = !takeMin;
	return factory.create (data);
    }
}