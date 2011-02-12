package org.khelekore.prtree;

/** A node object filterer.
 * @param <T> the node type
 */
public interface NodeFilter<T> {
    /** Check if the given node object is accepted
     * @param t the node user data
     * @return true if t is accepted
     */
    boolean accept (T t);
}