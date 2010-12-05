package org.khelekore.prtree;

/** Class to hold object and distance to it
 * @param T the type of object to calculate the distance to
 */
public class DistanceResult<T> {
    private final T t;
    private final double dist;
    
    public DistanceResult (T t, double dist) {
	this.t = t;
	this.dist = dist;
    }

    public T get () {
	return t;
    }

    public double getDistance () {
	return dist;
    }
}

