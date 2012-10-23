package org.khelekore.prtree.junit;

import java.awt.geom.Rectangle2D;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import org.junit.Before;
import org.junit.Test;
import org.khelekore.prtree.DistanceResult;
import org.khelekore.prtree.NodeFilter;
import org.khelekore.prtree.MinDist2D;
import org.khelekore.prtree.DistanceCalculator;
import org.khelekore.prtree.MBRConverter;
import org.khelekore.prtree.MBR;
import org.khelekore.prtree.PRTree;
import org.khelekore.prtree.PointND;
import org.khelekore.prtree.SimpleMBR;
import org.khelekore.prtree.SimplePointND;

import static org.junit.Assert.*;

/** Tests for PRTree.
 */
public class TestPRTree {
    private static final int BRANCH_FACTOR = 30;
    private Rectangle2DConverter converter = new Rectangle2DConverter ();
    private PRTree<Rectangle2D> tree;
    private NodeFilter<Rectangle2D> acceptAll = new AcceptAll<Rectangle2D> ();

    private static final double RANDOM_RANGE = 100000;

    @Before
    public void setUp() {
	tree = new PRTree<Rectangle2D> (converter, BRANCH_FACTOR);
    }

    private class Rectangle2DConverter implements MBRConverter<Rectangle2D> {
	public int getDimensions () {
	    return 2;
	}

	public double getMin (int axis, Rectangle2D t) {
	    return axis == 0 ? t.getMinX () : t.getMinY ();
	}

	public double getMax (int axis, Rectangle2D t) {
	    return axis == 0 ? t.getMaxX () : t.getMaxY ();
	}
    }

    @Test
    public void testEmpty () {
	tree.load (Collections.<Rectangle2D>emptyList ());
	assertEquals ("Number of leafs in empty tree is not zero",
		      0, tree.getNumberOfLeaves ());
	for (Rectangle2D r : tree.find (0, 0, 1, 1))
	    fail ("Should not get any results, found: " + r);
	assertNull ("mbr of empty tress should be null", tree.getMBR ());
	assertEquals ("height of empty tree", 1, tree.getHeight ());
    }

    @Test
    public void testSingle () {
	Rectangle2D rx = new Rectangle2D.Double (0, 0, 1, 1);
	tree.load (Collections.singletonList (rx));
	assertEquals ("Number of leafs in tree is not correct",
		      1, tree.getNumberOfLeaves ());
	MBR mbr = tree.getMBR ();
	assertEquals ("odd min for mbr", 0, mbr.getMin (0), 0);
	assertEquals ("odd max for mbr", 1, mbr.getMax (0), 0);
	assertEquals ("odd min for mbr", 0, mbr.getMin (1), 0);
	assertEquals ("odd max for mbr", 1, mbr.getMax (1), 0);
	assertEquals ("height of tree with one entry", 1, tree.getHeight ());
	int count = 0;
	for (Rectangle2D r : tree.find (0, 0, 1, 1)) {
	    assertEquals ("odd rectangle returned", rx, r);
	    count++;
	}
	assertEquals ("odd number of rectangles returned", 1, count);

	for (Rectangle2D r : tree.find (5, 5, 6, 7))
	    fail ("Should not find any rectangle, got: " + r);

	for (Rectangle2D r : tree.find (-5, -5, -2, -4))
	    fail ("Should not find any rectangle, got: " + r);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBadQueryRectX () {
	tree.find (new SimpleMBR (0, -1, 0, 1));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBadQueryRectY () {
	tree.find (new SimpleMBR (0, 1, 0, -1));
    }

    @Test(expected = IllegalStateException.class)
    public void testMultiLoad () {
	Rectangle2D rx = new Rectangle2D.Double (0, 0, 1, 1);
	tree.load (Collections.singletonList (rx));
	tree.load (Collections.singletonList (rx));
    }

    @Test
    public void testHeight () {
	// root and below it we have two leaf nodes
	int numRects = BRANCH_FACTOR + 1;
	List<Rectangle2D> rects = new ArrayList<Rectangle2D> (numRects);
	for (int i = 0; i < numRects; i++) {
	    rects.add (new Rectangle2D.Double (i, i, 10, 10));
	}
	tree.load (rects);
	assertEquals ("Number of leafs in tree is not correct",
		      rects.size (), tree.getNumberOfLeaves ());
	assertEquals ("height of tree", 2, tree.getHeight ());
    }

    @Test
    public void testMany () {
	int numRects = 300000 / 2;
	MBR queryInside = new SimpleMBR (495, 504.9, 495, 504.9);
	MBR queryOutside = new SimpleMBR (1495, 1504.9, 495, 504.9);
	int shouldFindInside = 0;
	int shouldFindOutside = 0;
	List<Rectangle2D> rects = new ArrayList<Rectangle2D> (numRects * 2);
	// build an "X"
	System.err.println ("TestPRTree: Building random rectangles");
	for (int i = 0; i < numRects; i++) {
	    Rectangle2D r1 = new Rectangle2D.Double (i, i, 10, 10);
	    Rectangle2D r2 = new Rectangle2D.Double (i, numRects - i, 10, 10);
	    if (queryInside.intersects (r1, converter))
		shouldFindInside++;
	    if (queryOutside.intersects (r1, converter))
		shouldFindOutside++;
	    if (queryInside.intersects (r2, converter))
		shouldFindInside++;
	    if (queryOutside.intersects (r2, converter))
		shouldFindOutside++;
	    rects.add (r1);
	    rects.add (r2);
	}

	System.err.println ("TestPRTree: Shuffling rectangles");
	// shuffle, but make sure the shuffle is the same every time
	Random random = new Random (4711);
	Collections.shuffle (rects, random);
	System.err.println ("TestPRTree: Loading tree with " + rects.size ());
	long start = System.nanoTime();
	tree.load (rects);
	long end = System.nanoTime();
	System.err.println ("TestPRTree: Tree loaded in " +
			    (end - start) + " nanos");
	assertEquals ("Number of leafs in tree is not correct",
		      rects.size (), tree.getNumberOfLeaves ());

	int count = 0;
	// dx = 10, each rect is 10 so 20 in total
 	for (Rectangle2D r : tree.find (queryInside))
	    count++;
	assertEquals ("should find some rectangles", shouldFindInside, count);
	
	count = 0;
	for (Rectangle2D r : tree.find (queryOutside))
	    count++;
	assertEquals ("should not find rectangles", shouldFindOutside, count);
    }

    @Test
    public void testRandom () {
	System.err.println ("TestPRTree: TestRandom");
	int numRects = 200; /* qwerty 100000 */;
	int numRounds = 10;

	Random random = new Random (1234);  // same random every time
	for (int round = 0; round < numRounds; round++) {
	    tree = new PRTree<Rectangle2D> (converter, 10);
	    List<Rectangle2D> rects = new ArrayList<Rectangle2D> (numRects);
	    for (int i = 0; i < numRects; i++) {
		Rectangle2D r =
		    new Rectangle2D.Double (getRandomRectangleSize (random),
					    getRandomRectangleSize (random),
					    getRandomRectangleSize (random),
					    getRandomRectangleSize (random));
		rects.add (r);
	    }
	    tree.load (rects);
	    double x1 = getRandomRectangleSize (random);
	    double y1 = getRandomRectangleSize (random);
	    double x2 = getRandomRectangleSize (random);
	    double y2 = getRandomRectangleSize (random);
	    MBR query =
		new SimpleMBR (Math.min (x1, x2), Math.max (x1, x2), 
				 Math.min (y1, y2), Math.max (y1, y2));
	    int countSimple = 0;
	    for (Rectangle2D r : rects) {
		if (query.intersects (r, converter))
		    countSimple++;
	    }
	    int countTree = 0;
	    for (Rectangle2D r : tree.find (query))
		countTree++;
	    assertEquals (round + ": should find same number of rectangles",
			  countSimple, countTree);
	}
    }

    private double getRandomRectangleSize (Random random) {
	return random.nextDouble () * RANDOM_RANGE - RANDOM_RANGE / 2;
    }

    @Test
    public void testFindSpeed () {
	System.err.println ("TestPRTree: Test find speed");
	int numRects = 100000;
	List<Rectangle2D> rects = new ArrayList<Rectangle2D> (numRects);
	for (int i = 0; i < numRects; i++)
	    rects.add (new Rectangle2D.Double (i, i, 10, 10));

	System.out.println ("TestPRTree: Running speed test");
	tree.load (rects);
	for (int i = 0; i < 3; i++) {
	    testFindSpeedIterator ();
	    testFindSpeedArray ();
	}
    }

    private void testFindSpeedIterator () {
	int count = 0;
	int numRounds = 100000;
	long start = System.nanoTime ();
	MBR mbr = new SimpleMBR (295, 1504.9, 295, 5504.9);
	for (int i = 0; i < numRounds; i++) {
	    for (Rectangle2D r : tree.find (mbr))
		count++;
	}
	long end = System.nanoTime ();
	long diff = end - start;
	System.out.println ("TestPRTree: Finding " + count + " took: " + 
			    (diff / 1000000) + " millis, " + 
			    "average: " + (diff / numRounds) + " nanos");
    }

    private void testFindSpeedArray () {
 	int count = 0;
	int numRounds = 100000;
	long start = System.nanoTime ();
	MBR mbr = new SimpleMBR (295, 1504.9, 295, 5504.9);
	for (int i = 0; i < numRounds; i++) {
	    List<Rectangle2D> result = new ArrayList<Rectangle2D> (150);
	    tree.find (mbr, result);
	    for (Rectangle2D r : result)
		count++;
	}
	long end = System.nanoTime ();
	long diff = end - start;
	System.out.println ("TestPRTree: Finding " + count + " took: " +
			    (diff / 1000000) + " millis, " +
			    "average: " + (diff / numRounds) + " nanos");
    }

    @Test
    public void testNNEmpty () {
	System.out.println ("TestPRTree: Testing nn empty");
	tree.load (Collections.<Rectangle2D>emptyList ());
	DistanceCalculator<Rectangle2D> dc = new RectDistance ();
	List<DistanceResult<Rectangle2D>> nnRes =
	    tree.nearestNeighbour (dc, acceptAll, 10, new SimplePointND (0, 0));
	assertNotNull ("Nearest neighbour should return a list ", nnRes);
	assertEquals ("Nearest neighbour on empty tree should be empty", 0,
		      nnRes.size ());
    }

    @Test
    public void testNNSingle () {
	System.out.println ("TestPRTree: Testing nn single");
	Rectangle2D rx = new Rectangle2D.Double (0, 0, 1, 1);
	tree.load (Collections.singletonList (rx));
	DistanceCalculator<Rectangle2D> dc = new RectDistance ();
	List<DistanceResult<Rectangle2D>> nnRes =
	    tree.nearestNeighbour (dc, acceptAll, 10, 
				   new SimplePointND (0.5, 0.5));
	assertNotNull ("Nearest neighbour should have a value ", nnRes);
	assertEquals ("Wrong size of the result", 1, nnRes.size ());
	DistanceResult<Rectangle2D> dr = nnRes.get (0);
	assertEquals ("Nearest neighbour on rectangle should be 0", 0,
		      dr.getDistance (), 0.0001);

	nnRes = tree.nearestNeighbour (dc, acceptAll, 10, 
				       new SimplePointND (2, 1));
	assertEquals ("Wrong size of the result", 1, nnRes.size ());
	dr = nnRes.get (0);
	assertEquals ("Nearest neighbour give wrong distance", 1,
		      dr.getDistance (), 0.0001);
    }

    @Test
    public void testNNMany () {
	System.out.println ("TestPRTree: Testing nn many");
	int numRects = 100000;
	List<Rectangle2D> rects = new ArrayList<Rectangle2D> (numRects);
	for (int i = 0; i < numRects; i++)
	    rects.add (new Rectangle2D.Double (i * 10, i * 10, 10, 10));
	tree.load (rects);

	DistanceCalculator<Rectangle2D> dc = new RectDistance ();
	List<DistanceResult<Rectangle2D>> nnRes =
	    tree.nearestNeighbour (dc, acceptAll, 10,
				   new SimplePointND (-1, -1));
	DistanceResult<Rectangle2D> dr = nnRes.get (0);
	assertEquals ("Wrong size of the result", 10, nnRes.size ());
	assertEquals ("Got wrong element back", rects.get (0), dr.get ());

	nnRes = tree.nearestNeighbour (dc, acceptAll, 10,
				       new SimplePointND (105, 99));
	assertEquals ("Wrong size of the result", 10, nnRes.size ());
	dr = nnRes.get (0);
	assertEquals ("Got wrong element back", rects.get (10), dr.get ());

	Random random = new Random (6789);  // same random every time
	for (int r = 0; r < 1000; r++) {
	    double dd = numRects * 10 * random.nextDouble ();
	    double x = dd + random.nextInt (2000) - 1000;
	    double y = dd + random.nextInt (2000) - 1000;
	    PointND p = new SimplePointND (x, y);
	    nnRes = tree.nearestNeighbour (dc, acceptAll, 10,p);
	    double minDist = Double.MAX_VALUE;
	    Rectangle2D minRect = null;
	    for (int i = 0; i < numRects; i++) {
		Rectangle2D rx = rects.get (i);
		double rdist = dc.distanceTo (rx, p);
		if (rdist < minDist) {
		    minDist = rdist;
		    minRect = rx;
		}
	    }
	    dr = nnRes.get (0);
	    assertEquals ("Got wrong element back",
			  minRect, dr.get ());
	    checkNNSortOrder (nnRes);
	}
    }

    @Test
    public void testNNDuplicates () {
	System.out.println ("TestPRTree: Testing nn duplicates");
	Rectangle2D a = new Rectangle2D.Double (0, 0, 1, 1);
	Rectangle2D b = new Rectangle2D.Double (-1, -1, 0, 0);
	Rectangle2D c = new Rectangle2D.Double (0, 0, 1, 1);
	tree.load (Arrays.asList(a, b, c));
	PointND p = new SimplePointND (0, 0);
	int maxHits = 5;
	DistanceCalculator<Rectangle2D> dc = new RectDistance ();
	List<DistanceResult<Rectangle2D>> nnRes =
	    tree.nearestNeighbour (dc, acceptAll, maxHits, p);
	assertEquals ("Wrong number of nearest neighbours", 3, nnRes.size ());
	checkNNSortOrder (nnRes);
    }

    private void checkNNSortOrder (List<DistanceResult<Rectangle2D>> nnRes) {
	DistanceResult<Rectangle2D> dr = nnRes.get (0);
	for (int i = 1, s = nnRes.size (); i < s; i++) {
	    DistanceResult<Rectangle2D> dr2 = nnRes.get (i);
	    assertTrue ("Bad sort order: i: " + i,
			dr.getDistance () <= dr2.getDistance ());
	    dr = dr2;
	}
    }

    private static class AcceptAll<T> implements NodeFilter<T> {
	public boolean accept (T t) {
	    return true;
	}
    }

    private static class RectDistance
	implements DistanceCalculator<Rectangle2D> {
	public double distanceTo (Rectangle2D r, PointND p) {
	    double md = MinDist2D.get (r.getMinX (), r.getMinY (),
				       r.getMaxX (), r.getMaxY (),
				       p.getOrd (0), p.getOrd (1));
	    return Math.sqrt (md);
	}
    }
}
