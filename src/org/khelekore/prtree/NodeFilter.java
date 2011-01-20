package org.khelekore.prtree;

/** A node object filterer.
 */
public interface NodeFilter<T> {
    /** Check if the given node object is accepted
     *
     * @return true if t is accepted
     */
    boolean accept (T t);
}