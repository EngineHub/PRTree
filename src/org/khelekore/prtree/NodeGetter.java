package org.khelekore.prtree;

import java.util.List;

/** A class that can get the next available node.
 * @param <N> the type of the node
 */
interface NodeGetter<N> {
    /** Get the next node. 
     * @param maxObject use at most this many objects
     */
    N getNextNode (int maxObjects);

    /** Check if we can get more nodes from this NodeGetter. */
    boolean hasMoreNodes ();

    /** Check if there is unused data in this NodeGetter. */
    boolean hasMoreData ();

    /** Split this NodeGetter into the low and high parts. */
    List<? extends NodeGetter<N>> split (int lowId, int highId);
}
