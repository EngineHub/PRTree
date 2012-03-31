package org.khelekore.prtree.junit;

import org.junit.runner.JUnitCore;

public class RunTests {
    public static void main (String args[]) {
	JUnitCore.main (TestSimpleMBRND.class.getName (),
			TestPRTreeND.class.getName (),
			TestPRTree.class.getName ());
    }
}
