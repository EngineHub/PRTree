package org.khelekore.prtree;

import java.util.Collection;
import java.util.Collections;

public class PRTree<T> {

    private int branchFactor;

    /** Create a new PRTree using the specified branch factor.
     * @param branchFactor the number of child nodes for each internal node.
     */
    public PRTree (int branchFactor) {
	this.branchFactor = branchFactor;
    }

    public void load (Collection<? extends T> data) {
	// TODO: implement
    }

    public Iterable<T> find (MBR mbr) {
	return Collections.<T>emptyList ();
    }
}
