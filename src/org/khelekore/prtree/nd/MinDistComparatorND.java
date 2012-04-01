package org.khelekore.prtree.nd;

import java.util.Comparator;

/** A comparator that uses the MINDIST metrics to sort Nodes
 * @param <T> the data stored in the nodes
 * @param <S> the actual node
 */
class MinDistComparatorND<T, S extends NodeND<T>> implements Comparator<S> {
    public final MBRConverterND<T> converter;
    public final PointND p;

    public MinDistComparatorND (MBRConverterND<T> converter, PointND p) {
	this.converter = converter;
	this.p = p;
    }

    public int compare (S t1, S t2) {
	MBRND mbr1 = t1.getMBR (converter);
	MBRND mbr2 = t2.getMBR (converter);
	return Double.compare (MinDistND.get (mbr1, p),
			       MinDistND.get (mbr2, p));
    }
}
