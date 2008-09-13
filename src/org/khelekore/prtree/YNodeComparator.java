package org.khelekore.prtree;

import java.util.Comparator;

class YNodeComparator<T> implements Comparator<Node<T>> {
    private MBRConverter<T> converter;

    public YNodeComparator (MBRConverter<T> converter) {
	this.converter = converter;
    }
    
    public int compare (Node<T> n1, Node<T> n2) {
	double d1 = n1.getMBR (converter).getMinY ();
	double d2 = n2.getMBR (converter).getMinY ();
	return Double.compare (d1, d2);
    }
}
