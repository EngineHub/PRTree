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
    private final List<NodeUsage<T>> max;
    private final NodeFactory<N> factory;
    private final int id;
    private final int size;

    private int minPos = 0;
    private int maxPos = 0;
    private int taken = 0;
    private boolean takeMin = true;

    public MinMaxNodeGetter (List<NodeUsage<T>> data,
			     NodeFactory<N> factory,
			     Comparator<T> minSorter,
			     Comparator<T> maxSorter,
			     int id) {
	min = new ArrayList<NodeUsage<T>> (data);
	Collections.sort (min, new NodeUsageSorter<T> (minSorter));
	max = new ArrayList<NodeUsage<T>> (data);
	Collections.sort (max, new NodeUsageSorter<T> (maxSorter));
	this.factory = factory;
	this.id = id;
	this.size = data.size ();
    }

    private MinMaxNodeGetter (List<NodeUsage<T>> min,
			      List<NodeUsage<T>> max,
			      NodeFactory<N> factory,
			      int id,
			      int size,
			      int minPos,
			      int maxPos) {
	this.min = min;
	this.max = max;
	this.factory = factory;
	this.id = id;
	this.size = size;
	this.minPos = minPos;
	this.maxPos = maxPos;
    }

    private boolean isUsedNode (List<NodeUsage<T>> ls, int pos) {
	NodeUsage<T> nu = ls.get (pos);
	return nu == null || nu.isUsed () || nu.getUser () != id;
    }

    private int findNextFree (List<NodeUsage<T>> ls, int pos) {
	int s = ls.size ();
	while (pos <  s && isUsedNode (ls, pos))
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

    public List<MinMaxNodeGetter<T, N>> split (int lowId, int highId) {
	int e = size - taken;
	int lowSize = (e + 1) / 2;
	int highSize = e - lowSize;

	int minPosSave = minPos;
	int maxPosSave = maxPos;

	// mark half the elements for lowId
	for (int i = 0; i < lowSize; i++)
	    markForId (lowId);

	MinMaxNodeGetter<T, N> lowPart =
	    new MinMaxNodeGetter<T, N> (min, max, factory, lowId,
					lowSize, minPosSave, maxPosSave);
	minPosSave = minPos;
	maxPosSave = maxPos;

	// mark the rest
	for (int i = 0; i < highSize; i++)
	    markForId (highId);
	MinMaxNodeGetter<T, N> highPart =
	    new MinMaxNodeGetter<T, N> (min, max, factory, highId,
					highSize, minPosSave, maxPosSave);
	List<MinMaxNodeGetter<T, N>> ret = new ArrayList<MinMaxNodeGetter<T, N>> (2);
	ret.add (lowPart);
	ret.add (highPart);
	return ret;
    }

    private void markForId (int id) {
	taken++;
	minPos = findNextFree (min, minPos);
	NodeUsage<T> nu = min.get (minPos++);
	nu.setUser (id);
    }
}