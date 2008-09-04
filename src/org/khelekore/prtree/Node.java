package org.khelekore.prtree;

import java.util.List;

/** A node in a Priority R-Tree
 */
interface Node<T> {
    /** Get the size of the node, that is how many data elements it holds */
    int size ();
    
    /** Get the MBR of this node */
    MBR getMBR ();
    
    /** Visit this node and add the leafs to the found list and add 
     *  any child nodes to the list of nodes to expand.
     */
    void expand (MBR mbr, List<T> found, List<Node<T>> nodesToExpand);
}
