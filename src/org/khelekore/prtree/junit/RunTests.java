package org.khelekore.prtree.junit;

import org.junit.runner.JUnitCore;

public class RunTests {
    public static void main (String args[]) {
	JUnitCore.main (TestSimpleMBR.class.getName (),
			TestPRTree.class.getName ());
    }
}
