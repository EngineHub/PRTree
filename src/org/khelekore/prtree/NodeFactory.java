package org.khelekore.prtree;

/** A factory that creates the nodes (either leaf or internal).
 */
interface NodeFactory<N> {
    /** Create a new node 
     * @param data the data entries for the node, fully filled.
     */
    N create (Object[] data);
}
