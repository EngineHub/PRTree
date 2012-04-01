package org.khelekore.prtree;

import java.util.List;
import org.khelekore.prtree.nd.DistanceCalculatorND;
import org.khelekore.prtree.nd.MBRConverterND;
import org.khelekore.prtree.nd.MBRND;
import org.khelekore.prtree.nd.PRTreeND;
import org.khelekore.prtree.nd.PointND;
import org.khelekore.prtree.nd.SimpleMBRND;
import org.khelekore.prtree.nd.SimplePointND;

/** A Priority R-Tree, a spatial index.
 *  This tree only supports bulk loading.
 *
 *  <pre>{@code
 *  PRTree<Rectangle2D> tree =
 *      new PRTree<Rectangle2D> (new Rectangle2DConverter (), 10);
 *  Rectangle2D rx = new Rectangle2D.Double (0, 0, 1, 1);
 *  tree.load (Collections.singletonList (rx));
 *  for (Rectangle2D r : tree.find (0, 0, 1, 1)) {
 *      System.out.println ("found a rectangle: " + r);
 *  }
 *  }</pre>
 *
 * @param <T> the data type stored in the PRTree
 */
public class PRTree<T> extends PRTreeND<T> {
    /** Create a new PRTree using the specified branch factor.
     * @param converter the MBRConverter to use for this tree
     * @param branchFactor the number of child nodes for each internal node.
     */
    public PRTree (MBRConverter<T> converter, int branchFactor) {
	super (new ToNDMBRConverter<T> (converter), branchFactor);
    }

    private static class ToNDMBRConverter<T> implements MBRConverterND<T> {
	private final MBRConverter<T> c;

	public ToNDMBRConverter (MBRConverter<T> c) {
	    this.c = c;
	}

	public int getDimensions () {
	    return 2;
	}

	public double getMin (int axis, T t) {
	    return axis == 0 ? c.getMinX (t) : c.getMinY (t);
	}

	public double getMax (int axis, T t) {
	    return axis == 0 ? c.getMaxX (t) : c.getMaxY (t);
	}
    }

    /** Get a minimum bounding rectangle of the data stored in this tree.
     * @return the MBR of the whole PRTree
     */
    public MBR getMBR () {
	MBRND mbr = getMBRND ();
	if (mbr == null)
	    return null;
	return new SimpleMBR (mbr.getMin (0), mbr.getMin (1),
			      mbr.getMax (0), mbr.getMax (1));
    }

    /** Finds all objects that intersect the given rectangle and stores
     *  the found node in the given list.
     * @param xmin the minimum value of the x coordinate when searching
     * @param ymin the minimum value of the y coordinate when searching
     * @param xmax the maximum value of the x coordinate when searching
     * @param ymax the maximum value of the y coordinate when searching
     * @param resultNodes the list that will be filled with the result
     */
    public void find (double xmin, double ymin, double xmax, double ymax,
		      List<T> resultNodes) {
	MBR mbr = new SimpleMBR (xmin, ymin, xmax, ymax);
	find (mbr, resultNodes);
    }

    /** Finds all objects that intersect the given rectangle and stores
     *  the found node in the given list.
     * @param query the bounds of the query
     * @param resultNodes the list that will be filled with the result
     */
    public void find (MBR query, List<T> resultNodes) {
	find (toMBRND (query), resultNodes);
    }

    /** Find all objects that intersect the given rectangle.
     * @param query the bounds of the query
     * @throws IllegalArgumentException if xmin &gt; xmax or ymin &gt; ymax
     * @return an iterable of the elements inside the query rectangle
     */
    public Iterable<T> find (final MBR query) {
	return find (toMBRND (query));
    }

    /** Find all objects that intersect the given rectangle.
     * @param xmin the minimum value of the x coordinate when searching
     * @param ymin the minimum value of the y coordinate when searching
     * @param xmax the maximum value of the x coordinate when searching
     * @param ymax the maximum value of the y coordinate when searching
     * @return an iterable of the elements inside the query rectangle
     * @throws IllegalArgumentException if xmin &gt; xmax or ymin &gt; ymax
     */
    public Iterable<T> find (double xmin, double ymin,
			     double xmax, double ymax) {
	MBR mbr = new SimpleMBR (xmin, ymin, xmax, ymax);
	return find (mbr);
    }

    private MBRND toMBRND (MBR mbr) {
	double[] values = {mbr.getMinX (), mbr.getMaxX (),
			   mbr.getMinY (), mbr.getMaxY ()};
	return new SimpleMBRND (values);
    }

    /** Get the nearest neighbour of the given point
     * @param dc the DistanceCalculator to use
     * @param filter a NodeFilter that can be used to ignore some leaf nodes.
     * @param maxHits the maximum number of entries to find
     * @param x the x coordinate to find the nearest neighbour to
     * @param y the y coordinate to find the nearest neighbour to
     * @return A List of DistanceResult with up to maxHits results.
     *         Will return an empty list if this tree is empty.
     */
    public List<DistanceResult<T>> nearestNeighbour (DistanceCalculator<T> dc,
						     NodeFilter<T> filter,
						     int maxHits,
						     double x, double y) {
	DistanceCalculatorND<T> dcnd = toDistanceCalculatorND (dc);
	PointND p = new SimplePointND (x, y);
	return super.nearestNeighbour (dcnd, filter, maxHits, p);
    }

    private DistanceCalculatorND<T> 
	toDistanceCalculatorND (final DistanceCalculator<T> dc) {
	return new DistanceCalculatorND<T> () {	
	    public double distanceTo (T t, PointND p) {
		return dc.distanceTo (t, p.getOrd (0), p.getOrd (1));
	    }
	};
    }
}
