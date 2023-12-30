package org.nimboscloud.manager.services;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class QueueConnection implements AutoCloseable {

    private ReentrantLock geralQueue = new ReentrantLock();
    private List<Object[]> listQueue = new ArrayList<>();
    private ReentrantLock lockThreadsonWait = new ReentrantLock();
    private ReentrantLock lockThreadsExcuting = new ReentrantLock();
    private ReentrantLock lockQueue = new ReentrantLock();
    private Condition waitQueue = lockQueue.newCondition();
    private int threadsonWait=0;
    private int threadsExcuting=0;
    private int memory=0;
    private int maxMemory=0;
    private int memoryOnWait=0;
    private int control=0;
    private ReentrantLock lockMemory = new ReentrantLock();

    public QueueConnection(){
    }

    public boolean isEmpty(){
        return listQueue.isEmpty();
    }

    public Object[] getOne(){
        Object[] objeto=null;
        if(!listQueue.isEmpty()){
            objeto = listQueue.get(0);
            listQueue.remove(0  );
        }
        return objeto;
    }

    public ReentrantLock getLock(){
        return lockQueue;
    }
    public Condition getCondition(){
        return waitQueue;
    }

    public void add(Object[] element){
        lockQueue.lock();
        listQueue.add(element);
        lockQueue.unlock();
    }
    public void rem(Object[] element){
        lockQueue.lock();
        listQueue.remove(element);
        lockQueue.unlock();
    }
    public void addThreadsonWait(){
        lockThreadsonWait.lock();
        threadsonWait++;
        lockThreadsonWait.unlock();
    }

    public void rmThreadsonWait(){
        lockThreadsonWait.lock();
        threadsonWait--;
        lockThreadsonWait.unlock();
    }

    public void addThreadsExcuting(){
        lockThreadsExcuting.lock();
        threadsExcuting++;
        lockThreadsExcuting.unlock();
    }

    public void rmThreadsExcuting(){
        lockThreadsExcuting.lock();
        threadsExcuting--;
        lockThreadsExcuting.unlock();
    }

    public int getThreadsonWait(){
        return threadsonWait;
    }

    public int getThreadsExcuting(){
        return threadsExcuting;
    }

    public void addMemory(int mem){
        lockMemory.lock();
        memory = memory + mem;
        lockMemory.unlock();
    }

    public void addMemoryonWait(int mem){
        lockMemory.lock();
        memoryOnWait = memoryOnWait + mem;
        lockMemory.unlock();
    }

    public void rmMemory(int mem){
        lockMemory.lock();
        memory = memory - mem;
        lockMemory.unlock();
    }

    public void rmMemoryonWait(int mem){
        lockMemory.lock();
        memoryOnWait = memoryOnWait - mem;
        lockMemory.unlock();
    }

    public int getMemory(){
        try {
            lockMemory.lock();
            return memory;
        }finally {
            lockMemory.unlock();
        }
    }
    public int getMemoryOnWait(){
        try {
            lockMemory.lock();
            return memoryOnWait;
        }finally {
            lockMemory.unlock();
        }
    }

    public void setMemory(int mem){
        memory = mem;
        maxMemory = mem;
    }

    public int getControl(){
        return control;
    }
    public void setControl(int c){
        control = c;
    }

    public int getMaxMemory(){
        return maxMemory;
    }

    public Object[] getNextJob() throws InterruptedException {
        try {
            lockQueue.lock();
            Object[] result = (Object[]) listQueue.get(0);
            int lowestMem = (int) listQueue.get(0)[2];

            for (Object[] element : listQueue) {
                if ((int) element[5] == 8) {
                    result = element;
                    break;
                }
                if ((int) element[2] < lowestMem) {
                    lowestMem = (int) element[2];
                    result = element;
                }

            }

            return result;

        }finally {
            lockQueue.unlock();
        }

    }

    public void increaseAgerJobs(){
        for (Object[] element : listQueue) {
            element[5] = (int) element[5] + 1;
        }
    }



    public boolean startJob(int mem, ReentrantLock lock){
        try{
            lockMemory.lock();
            if(mem <= this.getMemory()){
                this.rmMemory(mem);
                this.rmMemoryonWait(mem);
                lock.lock();
                return true;
            }else{
                return false;
            }
        }finally {
            lockMemory.unlock();
        }
    }

    public void addExec(Object[] element){
        lockMemory.lock();
        listQueue.add(element);
        this.addThreadsonWait();
        this.addMemoryonWait((int) element[2]);
        lockMemory.unlock();

    }

    public String getQueueInfo(int count) {
        StringBuilder info = new StringBuilder();

        lockThreadsonWait.lock();
        lockThreadsExcuting.lock();
        lockMemory.lock();

        try {
            info.append("Worker #").append(count).append("\n");
            info.append("  Threads Waiting: ").append(getThreadsonWait()).append("\n");
            info.append("  Threads Excuting: ").append(getThreadsExcuting()).append("\n");
            info.append("  Available Memory: ").append(getMemory()).append("\n");
            info.append("  Memory Waiting: ").append(getMemoryOnWait()).append("\n");
            info.append("  Max Memory: ").append(getMaxMemory()).append("\n");
        } finally {
            lockThreadsonWait.unlock();
            lockThreadsExcuting.unlock();
            lockMemory.unlock();
        }

        return info.toString();
    }



    @Override
    public void close() throws Exception {

    }
}
