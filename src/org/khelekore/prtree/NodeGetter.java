package org.khelekore.prtree;

/** A class that can get the next available node.
 * @param <N> the type of the node
 */
interface NodeGetter<N> {
    N getNextNode (int maxObjects);
}
