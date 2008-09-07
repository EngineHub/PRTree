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

    public double getMinX () {
	return xmin;
    }

    public double getMinY () {
	return ymin;
    }

    public double getMaxX () {
	return xmax;
    }

    public double getMaxY () {
	return xmax;
    }

    public MBR union (MBR other) {
	double uxmin = Math.min (xmin, other.getMinX ());
	double uymin = Math.min (ymin, other.getMinY ());
	double uxmax = Math.max (xmax, other.getMaxX ());
	double uymax = Math.max (ymax, other.getMaxY ());
	return new SimpleMBR (uxmin, uymin, uxmax, uymax);
    }

    public boolean intersects (MBR other) {
	return !(other.getMaxX () < xmin || other.getMinX () > xmax ||
		 other.getMaxY () < ymin || other.getMinY () > ymax);
    }

    public <T> boolean intersects (T t, MBRConverter<T> converter) {
	return !(converter.getMaxX (t) < xmin || converter.getMinX (t) > xmax ||
		 converter.getMaxY (t) < ymin || converter.getMinY (t) > ymax);
    }
}
