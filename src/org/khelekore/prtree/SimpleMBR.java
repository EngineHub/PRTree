package org.khelekore.prtree;

/** An implementation of MBR that keeps 4 double values for the actual min and
 *  max values needed.
 */
public class SimpleMBR implements MBR {
    private final double xmin;
    private final double ymin;
    private final double xmax;
    private final double ymax;

    public SimpleMBR (double xmin, double ymin, double xmax, double ymax) {
	this.xmin = xmin;
	this.ymin = ymin;
	this.xmax = xmax;
	this.ymax = ymax;
    }

    /** Get a string representation of this mbr. 
     */
    @Override public String toString () {
	return getClass ().getSimpleName () +
	    "{xmin: " + xmin + ", ymin: " + ymin +
	    ", xmax: " + xmax + ", ymax: " + ymax + "}";
    }

    public double getMin (int ordinate) {
	if (ordinate == 0)
	    return xmin;
	if (ordinate == 1)
	    return ymin;
	throw new IllegalArgumentException ("not able to get ordinate; " +
					    ordinate);
    }

    public double getMax (int ordinate) {
	if (ordinate == 0)
	    return xmax;
	if (ordinate == 1)
	    return ymax;
	throw new IllegalArgumentException ("not able to get ordinate; " +
					    ordinate);
    }

    public MBR union (MBR other) {
	double uxmin = Math.min (xmin, other.getMin (0));
	double uymin = Math.min (ymin, other.getMin (1));
	double uxmax = Math.max (xmax, other.getMax (0));
	double uymax = Math.max (ymax, other.getMax (1));
	return new SimpleMBR (uxmin, uymin, uxmax, uymax);
    }

    public boolean intersects (MBR other) {
	return !(other.getMax (0) < xmin || other.getMin (0) > xmax ||
		 other.getMax (1) < ymin || other.getMin (1) > ymax);
    }

    public <T> boolean intersects (T t, MBRConverter<T> converter) {
	return !(converter.getMaxX (t) < xmin || converter.getMinX (t) > xmax ||
		 converter.getMaxY (t) < ymin || converter.getMinY (t) > ymax);
    }
}
