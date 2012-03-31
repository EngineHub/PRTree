package org.khelekore.prtree.junit;

import org.junit.Test;
import org.khelekore.prtree.nd.MBRND;
import org.khelekore.prtree.nd.SimpleMBRND;

import static org.junit.Assert.*;

/** Tests for SimpleMBRND
 */
public class TestSimpleMBRND {
    @Test
    public void testCreate () {
	MBRND mbr = new SimpleMBRND (0, 1, 0, 1);
	assertEquals (0.0, mbr.getMin (0), 0.05);
	assertEquals (0.0, mbr.getMin (1), 0.05);
	assertEquals (1.0, mbr.getMax (0), 0.05);
	assertEquals (1.0, mbr.getMax (1), 0.05);
	assertEquals (2, mbr.getDimensions ());
    }

    @Test
    public void testUnion () {
	MBRND mbr1 = new SimpleMBRND (0, 1, 0, 1);
	MBRND mbr2 = new SimpleMBRND (2, 3, 2, 3);
	MBRND mbr = mbr1.union (mbr2);
	assertEquals (0.0, mbr.getMin (0), 0.05);
	assertEquals (0.0, mbr.getMin (1), 0.05);
	assertEquals (3.0, mbr.getMax (0), 0.05);
	assertEquals (3.0, mbr.getMax (1), 0.05);
    }
}
