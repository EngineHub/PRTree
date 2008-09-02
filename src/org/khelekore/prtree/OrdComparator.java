package org.khelekore.prtree;

import java.util.Comparator;

class OrdComparator<T> implements Comparator<T> {
    private int ord;
    private MBRConverter<T> converter;
    
    public OrdComparator (int ord, MBRConverter<T> converter) {
	this.ord = ord;
	this.converter = converter;
    }

    public int compare (T t1, T t2) {
	double d1 = converter.getMin (t1, ord);
	double d2 = converter.getMin (t1, ord);
	return Double.compare (d1, d2);
    }
}
