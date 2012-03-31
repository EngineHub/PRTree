package org.khelekore.prtree.nd;

import java.util.List;

class LeafNodeND<T> extends NodeBaseND<T, T> {

    public LeafNodeND (Object[] data) {
	super (data);
    }

    public MBRND getMBR (T t, MBRConverterND<T> converter) {
	return new SimpleMBRND (t, converter);
    }

    @Override public MBRND computeMBR (MBRConverterND<T> converter) {
	MBRND ret = null;
	for (int i = 0, s = size (); i < s; i++)
	    ret = getUnion (ret, getMBR (get (i), converter));
	return ret;
    }

    public void expand (MBRND mbr, MBRConverterND<T> converter,
			List<T> found, List<NodeND<T>> nodesToExpand) {
	find (mbr, converter, found);
    }

    public void find (MBRND mbr, MBRConverterND<T> converter, List<T> result) {
	for (int i = 0, s = size (); i < s; i++) {
	    T  t = get (i);
	    if (mbr.intersects (t, converter))
		result.add (t);
	}
    }
}
