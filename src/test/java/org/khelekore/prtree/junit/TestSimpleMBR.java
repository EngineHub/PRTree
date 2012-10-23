package org.khelekore.prtree.junit;

import org.junit.Test;
import org.khelekore.prtree.MBR;
import org.khelekore.prtree.SimpleMBR;

import static org.junit.Assert.*;

/** Tests for SimpleMBRND
 */
public class TestSimpleMBR {
    @Test
    public void testCreate () {
	MBR mbr = new SimpleMBR (0, 1, 0, 1);
	assertEquals (0.0, mbr.getMin (0), 0.05);
	assertEquals (0.0, mbr.getMin (1), 0.05);
	assertEquals (1.0, mbr.getMax (0), 0.05);
	assertEquals (1.0, mbr.getMax (1), 0.05);
	assertEquals (2, mbr.getDimensions ());
    }

    @Test
    public void testUnion () {
	MBR mbr1 = new SimpleMBR (0, 1, 0, 1);
	MBR mbr2 = new SimpleMBR (2, 3, 2, 3);
	MBR mbr = mbr1.union (mbr2);
	assertEquals (0.0, mbr.getMin (0), 0.05);
	assertEquals (0.0, mbr.getMin (1), 0.05);
	assertEquals (3.0, mbr.getMax (0), 0.05);
	assertEquals (3.0, mbr.getMax (1), 0.05);
    }
}
