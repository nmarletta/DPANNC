package dpannc.EXP;

public class Timer {
    private long start, spent = 0;
    public Timer() { start(); }
    public double check() { return (System.nanoTime()-start+spent)/1e9; }
    public void pause() { spent += System.nanoTime()-start; }
    public void start() { start = System.nanoTime(); }
}