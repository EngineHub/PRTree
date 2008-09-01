package org.khelekore.prtree;

public interface MBRConverter<T> {
    /** Get the minimum value in the given ordinate.
     * @param T the object to get the mbr ordinate for
     * @param ordinate the coordinate position (0, 1, 2, ...).
     */
    double getMin (T t, int ordinate);

    /** Get the maximum value in the given ordinate
     * @param T the object to get the mbr ordinate for
     * @param ordinate the coordinate position (0, 1, 2, ...).
     */
    double getMax (T t, int ordinate);
}