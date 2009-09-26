package org.khelekore.prtree;

import java.util.Comparator;

class YMaxComparator<T> implements Comparator<T> {
    private MBRConverter<T> converter;
    
    public YMaxComparator (MBRConverter<T> converter) {
	this.converter = converter;
    }

    public int compare (T t1, T t2) {
	double d1 = converter.getMaxY (t1);
	double d2 = converter.getMaxY (t2);
	return Double.compare (d2, d1);
    }
}
