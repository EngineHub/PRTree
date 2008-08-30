package org.khelekore.prtree;

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
}
