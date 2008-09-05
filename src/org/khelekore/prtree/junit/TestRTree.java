package org.khelekore.prtree.junit;

import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.khelekore.prtree.MBR;
import org.khelekore.prtree.MBRConverter;
import org.khelekore.prtree.PRTree;
import static org.junit.Assert.*;

public class TestRTree {
    private PRTree<Rectangle2D> tree;

    @Before
    public void setUp() {
	tree = new PRTree<Rectangle2D> (new Rectangle2DConverter (), 10);
    }

    private class Rectangle2DConverter implements MBRConverter<Rectangle2D> {
	public double getMin (Rectangle2D t, int ordinate) {
	    if (ordinate == 0)
		return t.getMinX ();
	    return t.getMinY ();
	}
	public double getMax (Rectangle2D t, int ordinate) {
	    if (ordinate == 0)
		return t.getMaxX ();
	    return t.getMaxY ();
	}
    }

    @Test
    public void testEmpty () {
	tree.load (Collections.<Rectangle2D>emptyList ());
	for (Rectangle2D r : tree.find (0, 0, 1, 1))
	    fail ("should not get any results");
	assertNull ("mbr of empty tress should be null", tree.getMBR ());
    }

    @Test
    public void testSingle () {
	Rectangle2D rx = new Rectangle2D.Double (0, 0, 1, 1);
	tree.load (Collections.singletonList (rx));
	MBR mbr = tree.getMBR ();
	assertEquals ("odd min for mbr", 0, mbr.getMin (0), 0);
	assertEquals ("odd min for mbr", 0, mbr.getMin (1), 0);
	assertEquals ("odd max for mbr", 1, mbr.getMax (0), 0);
	assertEquals ("odd max for mbr", 1, mbr.getMax (1), 0);
	int count = 0;
	for (Rectangle2D r : tree.find (0, 0, 1, 1)) {
	    assertEquals ("odd rectangle returned", rx, r);
	    count++;
	}
	assertEquals ("odd number of rectangles returned", 1, count);

	for (Rectangle2D r : tree.find (5, 5, 6, 7))
	    fail ("should not find any rectangle");

	for (Rectangle2D r : tree.find (-5, -5, -2, -4))
	    fail ("should not find any rectangle");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBadQueryRectX () {
	tree.find (0, 0, -1, 1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBadQueryRectY () {
	tree.find (0, 0, 1, -1);
    }

    @Test(expected = IllegalStateException.class)
    public void testMultiLoad () {
	Rectangle2D rx = new Rectangle2D.Double (0, 0, 1, 1);
	tree.load (Collections.singletonList (rx));
	tree.load (Collections.singletonList (rx));
    }

    @Test
    public void testMany () {
	int numRects = 1000000;
	List<Rectangle2D> rects = new ArrayList<Rectangle2D> (numRects);
	for (int i = 0; i < numRects; i++)
	    rects.add (new Rectangle2D.Double (i, i, 10, 10));
	// shuffle, but make sure the shuffle is the same every time
	Random random = new Random (4711);
	Collections.shuffle (rects, random);
	tree.load (rects);
	int count = 0;

	// dx = 10, each rect is 10 so 20 in total
	for (Rectangle2D r : tree.find (495, 495, 504.9, 504.9))
	    count++;
	assertEquals ("should find some rectangles", 20, count);

	count = 0;
	for (Rectangle2D r : tree.find (1495, 495, 1504.9, 504.9))
	    count++;
	assertEquals ("should not find rectangles", 0, count);
    }

    @Test
    public void testFindSpeed () {
	int numRects = 100000;
	List<Rectangle2D> rects = new ArrayList<Rectangle2D> (numRects);
	for (int i = 0; i < numRects; i++)
	    rects.add (new Rectangle2D.Double (i, i, 10, 10));
	tree.load (rects);
	
	System.out.println ("running speed test");
	int count = 0;
	int numRounds = 100000;
	long start = System.nanoTime ();
	for (int i = 0; i < numRounds; i++) {
	    for (Rectangle2D r : tree.find (295, 295, 1504.9, 5504.9))
		count++;
	}
	long end = System.nanoTime ();
	long diff = end - start;
	System.out.println ("finding took: " + (diff / 1000000) + " millis " + 
			    "average: " + (diff / numRounds) + " nanos");
    }

    public static void main (String args[]) {
	JUnitCore.main (TestRTree.class.getName ());
    }
}
