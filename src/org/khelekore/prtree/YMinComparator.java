package org.khelekore.prtree;

import java.util.Comparator;

class YMinComparator<T> implements Comparator<T> {
    private MBRConverter<T> converter;
    
    public YMinComparator (MBRConverter<T> converter) {
	this.converter = converter;
    }

    public int compare (T t1, T t2) {
	double d1 = converter.getMinY (t1);
	double d2 = converter.getMinY (t2);
	return Double.compare (d1, d2);
    }
}
