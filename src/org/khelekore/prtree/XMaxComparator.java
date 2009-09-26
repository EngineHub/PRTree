package org.khelekore.prtree;

import java.util.Comparator;

class XMaxComparator<T> implements Comparator<T> {
    private MBRConverter<T> converter;
    
    public XMaxComparator (MBRConverter<T> converter) {
	this.converter = converter;
    }

    public int compare (T t1, T t2) {
	double d1 = converter.getMaxX (t1);
	double d2 = converter.getMaxX (t2);
	return Double.compare (d2, d1);
    }
}
