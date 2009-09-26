package org.khelekore.prtree;

import java.util.Comparator;

class XMinNodeComparator<T> implements Comparator<Node<T>> {
    private MBRConverter<T> converter;

    public XMinNodeComparator (MBRConverter<T> converter) {
	this.converter = converter;
    }
    
    public int compare (Node<T> n1, Node<T> n2) {
	double d1 = n1.getMBR (converter).getMinX ();
	double d2 = n2.getMBR (converter).getMinX ();
	return Double.compare (d1, d2);
    }
}
