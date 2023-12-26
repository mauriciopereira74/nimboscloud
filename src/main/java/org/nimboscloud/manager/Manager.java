package org.nimboscloud.manager;
import org.nimboscloud.manager.workers.HandleClient;
import org.nimboscloud.manager.services.AuthenticationManager;
import org.nimboscloud.manager.skeletons.AuthenticationManagerSkeleton;
import org.nimboscloud.manager.workers.HandleServer;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.BlockingQueue;



public class Manager {
    public Map<Integer, DataOutputStream> clientOutMap = new HashMap<>();
    public Map<Integer,BlockingQueue> listQueue = new HashMap<>();
    public ReentrantLock lockList = new ReentrantLock();

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
        int numCliente=0;

        while (true) {

            Socket socket = ss.accept();
            Thread t = new Thread(new HandleClient(socket,authSkeleton,numCliente,listQueue,lockList));
            t.start();
            numCliente++;

        }
    }
    public void acceptServer() throws IOException {

        ServerSocket ss = new ServerSocket(1667);

        while (true) {

            Socket socket = ss.accept();
            Thread t = new Thread(new HandleServer(socket,clientOutMap,listQueue,lockList));

            t.start();

        }
    }
}
