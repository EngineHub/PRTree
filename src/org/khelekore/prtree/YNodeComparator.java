package org.khelekore.prtree;

import java.util.Comparator;

class YNodeComparator<T> implements Comparator<Node<T>> {
    public int compare (Node<T> n1, Node<T> n2) {
	double d1 = n1.getMBR ().getMinY ();
	double d2 = n2.getMBR ().getMinY ();
	return Double.compare (d1, d2);
    }
}
