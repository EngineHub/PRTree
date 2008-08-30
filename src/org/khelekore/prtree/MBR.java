package org.khelekore.prtree;

public interface MBR {
    /** Get the minimum value in the given ordinate.
     * @param ordinate the coordinate position (0, 1, 2, ...).
     */
    double getMin (int ordinate);

    /** Get the maximum value in the given ordinate
     * @param ordinate the coordinate position (0, 1, 2, ...).
     */
    double getMax (int ordinate);
}