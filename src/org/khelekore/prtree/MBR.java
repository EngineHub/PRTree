package org.khelekore.prtree;

/** A minimum bounding rectangle
 */ 
public interface MBR {
    /** Get the minimum value in the given ordinate.
     * @param ordinate the coordinate position (0, 1, 2, ...).
     */
    double getMin (int ordinate);

    /** Get the maximum value in the given ordinate
     * @param ordinate the coordinate position (0, 1, 2, ...).
     */
    double getMax (int ordinate);

    /** Return a new MBR that is the union of this mbr and the other 
     * @param mbr the MBR to create a union with
     */
    MBR union (MBR mbr);

    /** Check if the other MBR intersects this one */
    boolean intersects (MBR other);
}