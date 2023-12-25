package org.nimboscloud.server.workers;

public class ThreadInfo {
    private final Thread thread;
    private final int mem;

    public ThreadInfo(Thread thread, int mem) {
        this.thread = thread;
        this.mem = mem;
    }

    public Thread getThread() {
        return thread;
    }

    public int getMemory() {
        return mem;
    }
}
