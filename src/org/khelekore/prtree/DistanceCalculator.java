package org.khelekore.prtree;

/** A class that can calculate the distance to a given object
 *  stored in the PRTree
 * @param <T> the data type to calculate distances to
 */
public interface DistanceCalculator<T> {
    /** Calculate the distance between the given object and the point
     * @param t the object to calculate the distance to
     * @param x the x coordinate
     * @param y the y coordinate
     * @return The calculated distance
     */
    double distanceTo (T t, double x, double y);
}
