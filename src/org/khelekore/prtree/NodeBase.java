package org.khelekore.prtree;

abstract class NodeBase<N, T> implements Node<T> {
    private MBR mbr;
    private MBRConverter<T> converter;
    private Object[] data;
    private int usage;

    public NodeBase (int size, MBRConverter<T> converter) {
	data = new Object[size];
	this.converter = converter;
    }

    public void add (N n) {
	data[usage++] = n;
    }

    public int size () {
	return data.length;
    }

    @SuppressWarnings("unchecked")
    public N get (int i) {
	return (N)data[i];
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

