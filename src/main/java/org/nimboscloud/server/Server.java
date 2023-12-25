package org.nimboscloud.server;
import org.nimboscloud.JobFunction.JobFunction;
import org.nimboscloud.JobFunction.JobFunctionException;
import org.nimboscloud.server.workers.ServerWorker;
import org.nimboscloud.server.services.AuthenticationManager;
import org.nimboscloud.server.skeletons.AuthenticationManagerSkeleton;
import org.nimboscloud.server.workers.QueueWorker;
import org.nimboscloud.server.workers.ThreadInfo;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.BlockingQueue;


public class Server {
    public int memory = 1000;

    public int threadsOnWait = 0;

    private ReentrantLock lockMemory = new ReentrantLock();
    private ReentrantLock lockThreads = new ReentrantLock();
    public ReentrantLock lockQueue =  new ReentrantLock();
    public Condition waitQueue = lockQueue.newCondition();
    public BlockingQueue<ThreadInfo> queue = new LinkedBlockingQueue<>();



    public static void main(String[] args) {
        try {
            ServerSocket ss = new ServerSocket(1666);

            AuthenticationManager authManager = new AuthenticationManager();
            AuthenticationManagerSkeleton authSkeleton = new AuthenticationManagerSkeleton(authManager);
            Server server = new Server();

            // Crie um administrador para testes

            authManager.createAdminUser("a", "a");
            QueueWorker queueWorker = new QueueWorker(server);

            Thread h = new Thread(() -> {
                queueWorker.run();
            });
            h.start();
            while (true) {

                Socket socket = ss.accept();
                Thread t = new Thread(new ServerWorker(socket ,authSkeleton ,server));

                t.start();

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public  int getMemory(){
        try{
            lockMemory.lock();
            return this.memory;
        } finally {
            lockMemory.unlock();
        }
    }
    public void addMemory(int addValue){
        try{
            lockMemory.lock();
            this.memory += addValue;
        }finally {
            lockMemory.unlock();
        }
    }

    public void removeMemory(int removValue){
        try{
            lockMemory.lock();
            this.memory -= removValue;
        }finally {
            lockMemory.unlock();
        }
    }


    public void addToQueue(Thread t, int mem) throws InterruptedException {
        ThreadInfo threadInfo = new ThreadInfo(t, mem);
        queue.put(threadInfo);
    }

    public ThreadInfo getFromQueue() {
        try {
            return queue.take();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean startJob(int mem){
        try{
            lockMemory.lock();
            if(mem < this.getMemory()){
                this.removeMemory(mem);
                return true;
            }else{
                return false;
            }
        }finally {
            lockMemory.unlock();
        }
    }

    public int getThreadsOnWait() {
        try{
            lockThreads.lock();
            return threadsOnWait;
        }finally {
            lockThreads.unlock();
        }
    }

    public void addThreadsOnWait() {
        try{
            lockThreads.lock();
            this.threadsOnWait += 1;
        }finally {
            lockThreads.unlock();
        }

    }

    public void removeThreadsOnWait() {
        try{
            lockThreads.lock();
            this.threadsOnWait -= 1;
        }
        finally {
            lockThreads.unlock();
        }
    }



}
