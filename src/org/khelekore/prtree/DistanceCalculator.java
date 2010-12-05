package org.khelekore.prtree;

/** A class that can calculate the distance to a given object
 *  stored in the PRTree
 * @param T the data type stored in the PRTree
 */
public interface DistanceCalculator<T> {
    /** Calculate the distance between the given object and the point
     * @param t the object to calculate the distance to
     * @param x the x coordinate
     * @param y the y coordinate
     */
    double distanceTo (T t, double x, double y);
}
