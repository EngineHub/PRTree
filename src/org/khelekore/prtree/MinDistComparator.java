package org.khelekore.prtree;

import java.util.Comparator;

/** A comparator that uses the MINDIST metrics to sort Nodes
 * @param <T> the data stored in the nodes
 * @param <S> the actual node
 */
class MinDistComparator<T, S extends Node<T>> implements Comparator<S> {
    public final MBRConverter<T> converter;
    public final double x;
    public final double y;

    public MinDistComparator (MBRConverter<T> converter, double x, double y) {
	this.converter = converter;
	this.x = x;
	this.y = y;
    }

    public int compare (S t1, S t2) {
	MBR mbr1 = t1.getMBR (converter);
	MBR mbr2 = t2.getMBR (converter);
	return Double.compare (MinDist.get (mbr1.getMinX (), mbr1.getMinY (),
					    mbr1.getMaxX (), mbr1.getMaxY (), 
					    x, y),
			       MinDist.get (mbr2.getMinX (), mbr2.getMinY (),
					    mbr2.getMaxX (), mbr2.getMaxY (), 
					    x, y));
    }
}
