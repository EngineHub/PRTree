package org.khelekore.prtree;

import java.util.Comparator;

class NodeComparator<T> implements Comparator<Node<T>> {
    private int ord;

    public NodeComparator (int ord) {
	this.ord = ord;
    }

    public int compare (Node<T> n1, Node<T> n2) {
	double d1 = n1.getMBR ().getMin (ord);
	double d2 = n2.getMBR ().getMin (ord);
	return Double.compare (d1, d2);
    }
}
