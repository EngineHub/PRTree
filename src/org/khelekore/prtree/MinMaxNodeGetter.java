package org.khelekore.prtree;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/** A node getter that multiplexes min and max values
 * @param <N> the type of the node
 */
class MinMaxNodeGetter<T, N> implements NodeGetter<N> {
    private final List<NodeUsage<T>> min;
    private final List<NodeUsage<T>> max;
    private final NodeFactory<N> factory;
    private final TakeCounter takeCounter;
    private final int id;
    
    private int minPos = 0;
    private int maxPos = 0;
    private int takenNodes = 0;

    public MinMaxNodeGetter (List<NodeUsage<T>> data,
			     NodeFactory<N> factory,
			     Comparator<T> minSorter,
			     Comparator<T> maxSorter,
			     TakeCounter takeCounter,
			     int id) {
	min = new ArrayList<NodeUsage<T>> (data);
	Collections.sort (min, new NodeUsageSorter<T> (minSorter));
	max = new ArrayList<NodeUsage<T>> (data);
	Collections.sort (max, new NodeUsageSorter<T> (maxSorter));
	this.factory = factory;
	this.takeCounter = takeCounter;
	this.id = id;
    }

    private MinMaxNodeGetter (List<NodeUsage<T>> min,
			      List<NodeUsage<T>> max,
			      NodeFactory<N> factory,
			      TakeCounter takeCounter,
			      int id,
			      int minPos,
			      int maxPos) {
	this.min = min;
	this.max = max;
	this.factory = factory;
	this.takeCounter = takeCounter;
	this.id = id;
	this.minPos = minPos;
	this.maxPos = maxPos;
    }

    public MinMaxNodeGetter<T, N> getCopyFor (int newId, int size, 
					      TakeCounter takeCounter) {
	return new MinMaxNodeGetter<T, N> (min, max, factory, takeCounter, 
					   newId, minPos, maxPos);
    }

    public int getSize () {
	return takeCounter.getSize ();
    }

    public TakeCounter getTakeCounter () {
	return takeCounter;
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
	takeCounter.take ();
	minPos = findNextFree (min, minPos);
	NodeUsage<T> nu = min.set (minPos++, null);
	nu.use ();
	return nu.getData ();
    }

    private T getFirstUnusedMax () {
	takeCounter.take ();
	maxPos = findNextFree (max, maxPos);
	NodeUsage<T> nu = max.set (maxPos++, null);
	nu.use ();
	return nu.getData ();
    }

    public N getNextNode (int maxObjects) {
	int num = Math.min (takeCounter.getNumLeft (), maxObjects);
	Object[] data = new Object[num];
	for (int i = 0; i < num; i++)
	    data[i] = takenNodes == 0 ? getFirstUnusedMin () : getFirstUnusedMax ();
	takenNodes++;
	return factory.create (data);
    }

    public boolean hasMoreNodes () {
	return takenNodes < 2 && hasMoreData ();
    }

    public boolean hasMoreData () {
	return takeCounter.canTakeMore ();
    }

    public List<MinMaxNodeGetter<T, N>> split (int lowId, int highId) {
	int e = takeCounter.getNumLeft ();
	int lowSize = (e + 1) / 2;
	int highSize = e - lowSize;

	// Store start pos, but only min that changes
	int minPosSave = minPos;

	// mark half the elements for lowId
	for (int i = 0; i < lowSize; i++)
	    markForId (lowId);

	TakeCounter tcLow = new TakeCounter (lowSize);
	MinMaxNodeGetter<T, N> lowPart =
	    new MinMaxNodeGetter<T, N> (min, max, factory, tcLow, 
					lowId, minPosSave, maxPos);
	minPosSave = minPos;

	// mark the rest
	for (int i = 0; i < highSize; i++)
	    markForId (highId);
	TakeCounter tcHigh = new TakeCounter (highSize);
	MinMaxNodeGetter<T, N> highPart =
	    new MinMaxNodeGetter<T, N> (min, max, factory, tcHigh, 
					highId, minPosSave, maxPos);
	List<MinMaxNodeGetter<T, N>> ret =
	    new ArrayList<MinMaxNodeGetter<T, N>> (2);
	ret.add (lowPart);
	ret.add (highPart);
	return ret;
    }

    private void markForId (int id) {
	takeCounter.take ();
	minPos = findNextFree (min, minPos);
	NodeUsage<T> nu = min.get (minPos++);
	nu.setUser (id);
    }
}