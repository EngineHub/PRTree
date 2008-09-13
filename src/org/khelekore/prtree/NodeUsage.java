package org.khelekore.prtree;

/** Information needed to be able to figure out how an
 *  element of the tree is currently used.
 */
class NodeUsage<T> {
    /** The actual data of the node. */
    private T data;
    /** The leaf node builder user id (split id). */
    private int usage = 1;

    public NodeUsage (T data) {
	this.data = data;
    }

    public T getData () {
	return data;
    }

    public void use () {
	if (usage >= 0)
	    usage = -usage;
	else 
	    throw new RuntimeException ("using already used node");
    }

    public boolean isUsed () {
	return usage < 0;
    }

    public void setUser (int id) {
	if (id < 0)
	    throw new IllegalArgumentException ("id must be positive");
	usage = id;
    }

    public int getUser () {
	return Math.abs (usage);
    }

    @Override public String toString () {
	return getClass ().getSimpleName () + "{data: " + data +
	    ", used: " + isUsed () + ", user: " + getUser () + "}";
    }
}
