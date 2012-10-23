PRTree
======

The original implementation can be found here:
http://www.khelekore.org/prtree/index.shtml

This is a mirror that has been Maven-ized.

Background
----------

PRTree is a Priority R-Tree, a spatial index.

For some background read this:
http://www.cs.umd.edu/class/spring2005/cmsc828s/slides/prtree.pdf

PRTree is written to be fast and use as little memory aspossible. 

The source for org.khelekore.prtree.junit.TestRTree has a few examples
of setting up and querying a PRTree.

Basic usage goes something like this: 

    PRTree<Rectangle2D> tree = 
        new PRTree<Rectangle2D> (new Rectangle2DConverter (), 10);
    Rectangle2D rx = new Rectangle2D.Double (0, 0, 1, 1);
    tree.load (Collections.singletonList (rx));
    for (Rectangle2D r : tree.find (0, 0, 1, 1)) {
        System.out.println ("found a rectangle: " + r);
    }
