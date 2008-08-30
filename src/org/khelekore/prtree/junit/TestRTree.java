package org.khelekore.prtree.junit;

import java.awt.geom.Rectangle2D;
import java.util.Collections;
import org.khelekore.prtree.MBR;
import org.khelekore.prtree.SimpleMBR;
import org.khelekore.prtree.PRTree;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.JUnitCore;
import static org.junit.Assert.*;

public class TestRTree {
    private PRTree<Rectangle2D> tree;
    
    @Before 
    public void setUp() {
	tree = new PRTree<Rectangle2D> (10);
    }


    @Test
    public void testEmpty () {
	tree.load (Collections.<Rectangle2D>emptyList ());
	for (Rectangle2D r : tree.find (getMBR (0, 0, 1, 1)))
	    fail ("should not get any results");
    }

    @Test
    public void testSingle () {
	Rectangle2D rx = new Rectangle2D.Double (0, 0, 1, 1);
	tree.load (Collections.singletonList (rx));
	int count = 0;
	for (Rectangle2D r : tree.find (getMBR (0, 0, 1, 1))) {
	    assertEquals ("odd rectangle returned", rx, r);
	    count++;
	}
	assertEquals ("odd number of rectangles returned", 1, count);
    }

    
    private MBR getMBR (double xmin, double ymin, double xmax, double ymax) {
	return new SimpleMBR (xmin, ymin, xmax, ymax);
    }

    public static void main (String args[]) {
	JUnitCore.main (TestRTree.class.getName ());
    }
}
