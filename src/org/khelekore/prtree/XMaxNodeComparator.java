package org.khelekore.prtree;

import java.util.Comparator;

class XMaxNodeComparator<T> implements Comparator<Node<T>> {
    private MBRConverter<T> converter;

    public XMaxNodeComparator (MBRConverter<T> converter) {
	this.converter = converter;
    }
    
    public int compare (Node<T> n1, Node<T> n2) {
	double d1 = n1.getMBR (converter).getMaxX ();
	double d2 = n2.getMBR (converter).getMaxX ();
	return Double.compare (d2, d1);
    }
}
