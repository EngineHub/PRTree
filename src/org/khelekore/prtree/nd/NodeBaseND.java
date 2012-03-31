package org.khelekore.prtree.nd;

/**
 * @param <N> the type of the child entries
 * @param <T> the type of the data entries
 */
abstract class NodeBaseND<N, T> implements NodeND<T> {
    private MBRND mbr;
    private Object[] data;

    public NodeBaseND (Object[] data) {
	this.data = data;
    }

    public int size () {
	return data.length;
    }

    @SuppressWarnings("unchecked")
    public N get (int i) {
	return (N)data[i];
    }
    
    public MBRND getMBR (MBRConverterND<T> converter) {
	if (mbr == null)
	    mbr = computeMBR (converter);
	return mbr;
    }
    
    public abstract MBRND computeMBR (MBRConverterND<T> converter);
    
    public MBRND getUnion (MBRND m1, MBRND m2) {
	if (m1 == null)
	    return m2;
	return m1.union (m2);
    }
}
