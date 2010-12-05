package org.khelekore.prtree;

/** Class to hold object and distance to it
 * @param <T> the type of object to calculate the distance to
 */
public class DistanceResult<T> {
    private final T t;
    private final double dist;
    
    /** Create a new DistanceResult with a given object and distance
     * @param t the object we are measuring the distance to
     * @param dist the actual distance to the object
     */
    public DistanceResult (T t, double dist) {
	this.t = t;
	this.dist = dist;
    }

    /** Get the object
     * @return the object
     */
    public T get () {
	return t;
    }

    /** Get the distance
     * @return the distance
     */
    public double getDistance () {
	return dist;
    }
}

