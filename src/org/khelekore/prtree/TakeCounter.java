package org.khelekore.prtree;

class TakeCounter {
    private final int maxTaken;
    private int taken = 0;

    public TakeCounter (int maxTaken) {
	this.maxTaken = maxTaken;
    }

    public void take () {
	if (taken == maxTaken)
	    throw new IllegalStateException ("Too many taken");
	taken++;
    }

    public int getTaken () {
	return taken;
    }

    public int getNumLeft () {
	return maxTaken - taken;
    }

    public boolean canTakeMore () {
	return taken < maxTaken;
    }

    public int getSize () {
	return maxTaken;
    }
}