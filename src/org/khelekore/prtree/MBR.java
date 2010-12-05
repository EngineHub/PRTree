package org.khelekore.prtree;

/** A minimum bounding rectangle
 */ 
public interface MBR {
    /** Get the minimum x value 
     * @return the x min value
     */
    double getMinX ();

    /** Get the minimum y value
     * @return the y min value
     */
    double getMinY ();

    /** Get the maximum x value
     * @return the x max value
     */
    double getMaxX ();

    /** Get the maximum y value
     * @return the y max value
     */
    double getMaxY ();

    /** Return a new MBR that is the union of this mbr and the other 
     * @param mbr the MBR to create a union with
     * @return the new MBR
     */
    MBR union (MBR mbr);

    /** Check if the other MBR intersects this one
     * @param other the MBR to check against
     * @return true if the given MBR intersects with this MBR
     */
    boolean intersects (MBR other);

    /** Check if this MBR intersects the rectangle given by the object 
     *  and the MBRConverter.
     * @param t a rectangular object
     * @param converter the MBRConverter
     * @return true if the given MBR intersects with the given object
     * @param <T> the object type
     */
    <T> boolean intersects (T t, MBRConverter<T> converter);
}