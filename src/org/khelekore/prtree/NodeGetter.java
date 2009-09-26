package org.khelekore.prtree;

/** A class that can get the next available node.
 * @param <N> the type of the node
 */
interface NodeGetter<N> {
    /** Get the next node. 
     * @param maxObject use at most this many objects
     */
    N getNextNode (int maxObjects);

    /** Get the number of nodes that we can get before we need to split
     */
    int getNumberOfNodes ();
}
