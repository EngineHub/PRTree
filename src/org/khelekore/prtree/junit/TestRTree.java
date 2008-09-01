package org.khelekore.prtree.junit;

import java.awt.geom.Rectangle2D;
import java.util.Collections;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.JUnitCore;
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
    }

    @Test
    public void testSingle () {
	Rectangle2D rx = new Rectangle2D.Double (0, 0, 1, 1);
	tree.load (Collections.singletonList (rx));
	int count = 0;
	for (Rectangle2D r : tree.find (0, 0, 1, 1)) {
	    assertEquals ("odd rectangle returned", rx, r);
	    count++;
	}
	assertEquals ("odd number of rectangles returned", 1, count);
    }

    public static void main (String args[]) {
	JUnitCore.main (TestRTree.class.getName ());
    }
}
