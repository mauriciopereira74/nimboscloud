package org.nimboscloud.manager;
import org.nimboscloud.manager.services.QueueConnection;
import org.nimboscloud.manager.services.QueueList;
import org.nimboscloud.manager.workers.HandleClient;
import org.nimboscloud.manager.services.AuthenticationManager;
import org.nimboscloud.manager.skeletons.AuthenticationManagerSkeleton;
import org.nimboscloud.manager.workers.HandleServer;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;



public class Manager {
    public Map<Integer, Object[]> clientOutMap = new HashMap<>();
    public QueueList queueList = new QueueList();
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

            Thread t = new Thread(new HandleClient(socket,authSkeleton,numCliente,queueList));
            t.start();
            numCliente++;

        }
    }
    public void acceptServer() throws IOException {

        ServerSocket ss = new ServerSocket(1667);

        while (true) {

            Socket socket = ss.accept();
            Thread t = new Thread(new HandleServer(socket,clientOutMap,queueList));

            t.start();

        }
    }
}
