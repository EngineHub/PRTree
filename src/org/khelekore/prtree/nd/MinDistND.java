package org.khelekore.prtree.nd;

/** Class that can calculate the MINDIST between a point and a rectangle
 */
public class MinDistND {
    /** Do not instantiate
     */
    private MinDistND () {
	// empty
    }

    /** Calculate the MINDIST between the given MBRND and the given point
     * @param mbr the bounding box to use
     * @param p the point
     * @return the squared distance
     */
    public static double get (MBRND mbr, PointND p) {
	double res = 0;
	for (int i = 0; i < p.getDimensions (); i++) {
	    double o = p.getOrd (i);
	    double rv = r (o, mbr.getMin (i), mbr.getMax (i));
	    double dr = o - rv;
	    res += dr * dr;
	}
	return res;
    }

    private static double r (double x, double min, double max) {
	double r = x;
	if (x < min)
	    r = min;
	if (x > max)
	    r = max;
	return r;
    }
}
