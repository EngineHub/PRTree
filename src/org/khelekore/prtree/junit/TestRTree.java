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
import org.khelekore.prtree.SimpleMBR;

import static org.junit.Assert.*;

public class TestRTree {
    private Rectangle2DConverter converter = new Rectangle2DConverter ();
    private PRTree<Rectangle2D> tree;

    @Before
    public void setUp() {
	tree = new PRTree<Rectangle2D> (converter, 10);
    }

    private class Rectangle2DConverter implements MBRConverter<Rectangle2D> {
	public double getMinX (Rectangle2D t) {
	    return t.getMinX ();
	}

	public double getMinY (Rectangle2D t) {
	    return t.getMinY ();
	}

	public double getMaxX (Rectangle2D t) {
	    return t.getMaxX ();
	}

	public double getMaxY (Rectangle2D t) {
	    return t.getMaxY ();
	}
    }

    @Test
    public void testEmpty () {
	tree.load (Collections.<Rectangle2D>emptyList ());
	for (Rectangle2D r : tree.find (0, 0, 1, 1))
	    fail ("should not get any results");
	assertNull ("mbr of empty tress should be null", tree.getMBR ());
	assertEquals ("height of empty tree", 1, tree.getHeight ());
    }

    @Test
    public void testSingle () {
	Rectangle2D rx = new Rectangle2D.Double (0, 0, 1, 1);
	tree.load (Collections.singletonList (rx));
	MBR mbr = tree.getMBR ();
	assertEquals ("odd min for mbr", 0, mbr.getMinX (), 0);
	assertEquals ("odd min for mbr", 0, mbr.getMinY (), 0);
	assertEquals ("odd max for mbr", 1, mbr.getMaxX (), 0);
	assertEquals ("odd max for mbr", 1, mbr.getMaxY (), 0);
	assertEquals ("height of tree with one entry", 1, tree.getHeight ());
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
    public void testHeight () {
	int numRects = 11;  // root and below it we have two leaf nodes 
	List<Rectangle2D> rects = new ArrayList<Rectangle2D> (numRects);
	for (int i = 0; i < numRects; i++) {
	    rects.add (new Rectangle2D.Double (i, i, 10, 10));
	}
	tree.load (rects);
	assertEquals ("height of tree", 2, tree.getHeight ());
    }

    @Test
    public void testMany () {
	int numRects = 1000000;
	MBR queryInside = new SimpleMBR (495, 495, 504.9, 504.9);
	MBR queryOutside = new SimpleMBR (1495, 495, 1504.9, 504.9);
	int shouldFindInside = 0;
	int shouldFindOutside = 0;
	List<Rectangle2D> rects = new ArrayList<Rectangle2D> (numRects);
	for (int i = 0; i < numRects; i++) {
	    Rectangle2D r = new Rectangle2D.Double (i, i, 10, 10);
	    if (queryInside.intersects (r, converter)) 
		shouldFindInside++;
	    if (queryOutside.intersects (r, converter)) 
		shouldFindOutside++;
	    rects.add (r);
	}

	// shuffle, but make sure the shuffle is the same every time
	Random random = new Random (4711);
	Collections.shuffle (rects, random);
	tree.load (rects);
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
	System.out.println ("finding " + count + " took: " + (diff / 1000000) +
			    " millis, average: " + (diff / numRounds) + 
			    " nanos");

	
	count = 0;
	start = System.nanoTime ();
	for (int i = 0; i < numRounds; i++) {
	    List<Rectangle2D> result = new ArrayList<Rectangle2D> (150);
	    tree.find (295, 295, 1504.9, 5504.9, result);
	    for (Rectangle2D r : result)
		count++;
	}
	end = System.nanoTime ();
	diff = end - start;
	System.out.println ("finding " + count + " took: " + (diff / 1000000) +
			    " millis, average: " + (diff / numRounds) + 
			    " nanos");
    }

    public static void main (String args[]) {
	JUnitCore.main (TestRTree.class.getName ());
    }
}
