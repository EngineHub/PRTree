package org.khelekore.prtree.nd;

import java.util.Comparator;

class InternalNodeComparators<T> implements NodeComparators<NodeND<T>> {
    private final MBRConverterND<T> converter;

    public InternalNodeComparators (MBRConverterND<T> converter) {
	this.converter = converter;
    }

    public Comparator<NodeND<T>> getMinComparator (final int axis) {
	return new Comparator<NodeND<T>> () {
	    public int compare (NodeND<T> n1, NodeND<T> n2) {
		double d1 = n1.getMBR (converter).getMin (axis);
		double d2 = n2.getMBR (converter).getMin (axis);
		return Double.compare (d1, d2);
	    }
	};
    }

    public Comparator<NodeND<T>> getMaxComparator (final int axis) {
	return new Comparator<NodeND<T>> () {
	    public int compare (NodeND<T> n1, NodeND<T> n2) {
		double d1 = n1.getMBR (converter).getMax (axis);
		double d2 = n2.getMBR (converter).getMax (axis);
		return Double.compare (d1, d2);
	    }
	};
    }
}