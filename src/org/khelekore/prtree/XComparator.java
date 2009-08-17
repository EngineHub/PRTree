package org.khelekore.prtree;

import java.util.Comparator;

class XComparator<T> implements Comparator<T> {
    private MBRConverter<T> converter;
    
    public XComparator (MBRConverter<T> converter) {
	this.converter = converter;
    }

    public int compare (T t1, T t2) {
	double d1 = converter.getMinX (t1);
	double d2 = converter.getMinX (t2);
	return Double.compare (d1, d2);
    }
}
