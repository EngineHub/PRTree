package org.khelekore.prtree;

/** A class that given a T can tell the minimum and maximum 
 *  ordinates for that object.
 * @param <T> the data type stored in the PRTree
 */
public interface MBRConverter<T> {
    /** Get the minimum x coordinate value for the given t.
     * @param t the object to get the mbr ordinate for
     * @return the x min value
     */
    double getMinX (T t);

    /** Get the minimum y coordinate value for the given t.
     * @param t the object to get the mbr ordinate for
     * @return the y min value
     */
    double getMinY (T t);

    /** Get the maximum x coordinate value for the given t
     * @param t the object to get the mbr ordinate for
     * @return the x max value
     */
    double getMaxX (T t);

    /** Get the maximum y coordinate value for the given t
     * @param t the object to get the mbr ordinate for
     * @return the y max value
     */
    double getMaxY (T t);
}