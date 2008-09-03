package org.khelekore.prtree;

/** A class that given a T can tell the minimum and maximum 
 *  ordinates for that object.
 * @param T the data type stored in the PRTree
 */
public interface MBRConverter<T> {
    /** Get the minimum value in the given ordinate.
     * @param t the object to get the mbr ordinate for
     * @param ordinate the coordinate position (0, 1, 2, ...).
     */
    double getMin (T t, int ordinate);

    /** Get the maximum value in the given ordinate
     * @param t the object to get the mbr ordinate for
     * @param ordinate the coordinate position (0, 1, 2, ...).
     */
    double getMax (T t, int ordinate);
}