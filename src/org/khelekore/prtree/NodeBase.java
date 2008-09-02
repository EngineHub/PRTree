package org.khelekore.prtree;

import java.util.ArrayList;

abstract class NodeBase<N, T> extends ArrayList<N> implements Node<T> {
    private MBR mbr;
    private MBRConverter<T> converter;
    
    public NodeBase (int size, MBRConverter<T> converter) {
	super (size);
	this.converter = converter;
    }
    
    public MBR getMBR () {
	if (mbr == null)
	    mbr = computeMBR ();
	return mbr;
    }
    
    public MBR getMBR (T t) {
	return new SimpleMBR (converter.getMin (t, 0),
			      converter.getMin (t, 1),
			      converter.getMax (t, 0),
			      converter.getMax (t, 1));
    }
    
    public abstract MBR computeMBR ();
    
    public MBR getUnion (MBR m1, MBR m2) {
	if (m1 == null)
	    return m2;
	return m1.union (m2);
    }
}

