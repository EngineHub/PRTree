package org.khelekore.prtree.junit;

import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import org.junit.Before;
import org.junit.Test;
import org.khelekore.prtree.nd.MBRConverterND;
import org.khelekore.prtree.nd.MBRND;
import org.khelekore.prtree.nd.PRTreeND;
import org.khelekore.prtree.nd.SimpleMBRND;

import static org.junit.Assert.*;

/** Tests for PRTreeND.
 */
public class TestPRTreeND {
    private static final int BRANCH_FACTOR = 30;
    private Rectangle2DConverter converter = new Rectangle2DConverter ();
    private PRTreeND<Rectangle2D> tree;

    private static final double RANDOM_RANGE = 100000;

    @Before
    public void setUp() {
	tree = new PRTreeND<Rectangle2D> (converter, BRANCH_FACTOR);
    }

    private class Rectangle2DConverter implements MBRConverterND<Rectangle2D> {
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
	for (Rectangle2D r : tree.find (new SimpleMBRND (0, 1, 0, 1)))
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
	MBRND mbr = tree.getMBR ();
	assertEquals ("odd min for mbr", 0, mbr.getMin (0), 0);
	assertEquals ("odd max for mbr", 1, mbr.getMax (0), 0);
	assertEquals ("odd min for mbr", 0, mbr.getMin (1), 0);
	assertEquals ("odd max for mbr", 1, mbr.getMax (1), 0);
	assertEquals ("height of tree with one entry", 1, tree.getHeight ());
	int count = 0;
	for (Rectangle2D r : tree.find (new SimpleMBRND (0, 1, 0, 1))) {
	    assertEquals ("odd rectangle returned", rx, r);
	    count++;
	}
	assertEquals ("odd number of rectangles returned", 1, count);

	for (Rectangle2D r : tree.find (new SimpleMBRND (5, 6, 5, 7)))
	    fail ("Should not find any rectangle, got: " + r);

	for (Rectangle2D r : tree.find (new SimpleMBRND (-5, -2, -5, -4)))
	    fail ("Should not find any rectangle, got: " + r);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBadQueryRectX () {
	tree.find (new SimpleMBRND (0, -1, 0, 1));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBadQueryRectY () {
	tree.find (new SimpleMBRND (0, 1, 0, -1));
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
	MBRND queryInside = new SimpleMBRND (495, 504.9, 495, 504.9);
	MBRND queryOutside = new SimpleMBRND (1495, 1504.9, 495, 504.9);
	int shouldFindInside = 0;
	int shouldFindOutside = 0;
	List<Rectangle2D> rects = new ArrayList<Rectangle2D> (numRects * 2);
	// build an "X"
	System.err.println ("TestPRTreeND: Building random rectangles");
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

	System.err.println ("TestPRTreeND: Shuffling rectangles");
	// shuffle, but make sure the shuffle is the same every time
	Random random = new Random (4711);
	Collections.shuffle (rects, random);
	System.err.println ("TestPRTreeND: Loading tree with " + rects.size ());
	long start = System.nanoTime();
	tree.load (rects);
	long end = System.nanoTime();
	System.err.println ("TestPRTreeND: Tree loaded in " +
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
	System.err.println ("TestPRTreeND: TestRandom");
	int numRects = 200; /* qwerty 100000 */;
	int numRounds = 10;

	Random random = new Random (1234);  // same random every time
	for (int round = 0; round < numRounds; round++) {
	    tree = new PRTreeND<Rectangle2D> (converter, 10);
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
	    MBRND query =
		new SimpleMBRND (Math.min (x1, x2), Math.max (x1, x2), 
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
	System.err.println ("TestPRTreeND: Test find speed");
	int numRects = 100000;
	List<Rectangle2D> rects = new ArrayList<Rectangle2D> (numRects);
	for (int i = 0; i < numRects; i++)
	    rects.add (new Rectangle2D.Double (i, i, 10, 10));

	System.out.println ("TestPRTreeND: Running speed test");
	tree.load (rects);
	testFindSpeedIterator ();
	testFindSpeedArray ();
    }

    private void testFindSpeedIterator () {
	int count = 0;
	int numRounds = 100000;
	long start = System.nanoTime ();
	MBRND mbr = new SimpleMBRND (295, 1504.9, 295, 5504.9);
	for (int i = 0; i < numRounds; i++) {
	    for (Rectangle2D r : tree.find (mbr))
		count++;
	}
	long end = System.nanoTime ();
	long diff = end - start;
	System.out.println ("TestPRTreeND: Finding " + count + " took: " + 
			    (diff / 1000000) + " millis, " + 
			    "average: " + (diff / numRounds) + " nanos");
    }

    private void testFindSpeedArray () {
 	int count = 0;
	int numRounds = 100000;
	long start = System.nanoTime ();
	MBRND mbr = new SimpleMBRND (295, 1504.9, 295, 5504.9);
	for (int i = 0; i < numRounds; i++) {
	    List<Rectangle2D> result = new ArrayList<Rectangle2D> (150);
	    tree.find (mbr, result);
	    for (Rectangle2D r : result)
		count++;
	}
	long end = System.nanoTime ();
	long diff = end - start;
	System.out.println ("TestPRTreeND: Finding " + count + " took: " +
			    (diff / 1000000) + " millis, " +
			    "average: " + (diff / numRounds) + " nanos");
    }
}
