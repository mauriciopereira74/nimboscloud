package org.nimboscloud.manager;
import org.nimboscloud.manager.workers.HandleClient;
import org.nimboscloud.manager.services.AuthenticationManager;
import org.nimboscloud.manager.skeletons.AuthenticationManagerSkeleton;
import org.nimboscloud.manager.workers.HandleServer;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.BlockingQueue;



public class Manager {
    public int memory = 1000;

    public int threadsOnWait = 0;

    private ReentrantLock lockMemory = new ReentrantLock();
    private ReentrantLock lockThreads = new ReentrantLock();
    public ReentrantLock lockQueue =  new ReentrantLock();
    public Condition waitQueue = lockQueue.newCondition();
    public BlockingQueue<Object[]> queue = new LinkedBlockingQueue<>();



    public static void main(String[] args) {
        Manager manager = new Manager();
        try {
            Thread t1 = new Thread(() -> {
                try {
                    manager.acceptClient();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });

            Thread t2 = new Thread(() -> {
                try {
                    manager.acceptServer();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });

            t1.start();
            t2.start();

        } catch (Exception e) {
        e.printStackTrace();
        }

    }

    public void acceptClient() throws IOException {

        ServerSocket ss = new ServerSocket(1666);

        AuthenticationManager authManager = new AuthenticationManager();
        AuthenticationManagerSkeleton authSkeleton = new AuthenticationManagerSkeleton(authManager);
        authManager.createAdminUser("a", "a");

        while (true) {

            Socket socket = ss.accept();
            Thread t = new Thread(new HandleClient(socket,authSkeleton));

            t.start();

        }
    }
    public void acceptServer() throws IOException {

        ServerSocket ss = new ServerSocket(1667);

        while (true) {

            Socket socket = ss.accept();
            BlockingQueue<Object[]> queue = new LinkedBlockingQueue<>();
            Thread t = new Thread(new HandleServer(queue,socket));

            t.start();

        }
    }
}
