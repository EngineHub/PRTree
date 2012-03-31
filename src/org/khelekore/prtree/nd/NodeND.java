package org.khelekore.prtree.nd;

import java.util.List;

/** A node in a Priority R-Tree
 * @param <T> the data type of the elements stored in this node
 */
interface NodeND<T> {
    /** Get the size of the node, that is how many data elements it holds
     * @return the number of elements (leafs or child nodes) that this node has
     */
    int size ();

    /** Get the MBR of this node
     * @param converter the MBR converter to use for the actual objects
     * @return the MBR for this node
     */
    MBRND getMBR (MBRConverterND<T> converter);

    /** Visit this node and add the leafs to the found list and add
     *  any child nodes to the list of nodes to expand.
     * @param mbr the query rectangle
     * @param converter the MBR converter to use for the actual objects
     * @param found the List of leaf nodes
     * @param nodesToExpand the List of nodes that needs to be expanded
    */
    void expand (MBRND mbr, MBRConverterND<T> converter,
		 List<T> found, List<NodeND<T>> nodesToExpand);

    /** Find nodes that intersect with the given MBR.
     * @param mbr the query rectangle
     * @param converter the MBR converter to use for the actual objects
     * @param result the List to add the found nodes to
     */
    void find (MBRND mbr, MBRConverterND<T> converter, List<T> result);
}
