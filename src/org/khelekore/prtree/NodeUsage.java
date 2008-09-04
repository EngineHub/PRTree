package org.khelekore.prtree;


/** Information needed to be able to figure out how an
 *  element of the tree is currently used.
 */
class NodeUsage<T> {
    private T data;
    private short user;
    private boolean used;

    public NodeUsage (T data) {
	this.data = data;
    }

    public T getData () {
	return data;
    }

    public void use () {
	used = true;
    }

    public boolean isUsed () {
	return used;
    }

    public void setUser (int id) {
	user = (short)id;
    }

    public int getUser () {
	return (user & 0xffff);
    }

    @Override public String toString () {
	return getClass ().getSimpleName () + "{data: " + data +
	    ", used: " + isUsed () + ", user: " + getUser () + "}";
    }
}
