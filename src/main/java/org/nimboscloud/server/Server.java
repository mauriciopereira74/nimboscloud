package org.nimboscloud.server;
import org.nimboscloud.server.services.ExecuteManager;
import org.nimboscloud.server.workers.ServerWorker;
import org.nimboscloud.server.services.AuthenticationManager;
import org.nimboscloud.server.skeletons.AuthenticationManagerSkeleton;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    public int memory = 1000;

    public int threadsOnWait = 0;

    public static void main(String[] args) {
        try {
            ServerSocket ss = new ServerSocket(1666);

            AuthenticationManager authManager = new AuthenticationManager();
            AuthenticationManagerSkeleton authSkeleton = new AuthenticationManagerSkeleton(authManager);

            // Crie um administrador para testes
            authManager.createAdminUser("admin", "admin");

            while (true) {

                Socket socket = ss.accept();
                Thread t = new Thread(new ServerWorker(socket, new ExecuteManager(new Server()),authSkeleton));
                t.start();

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public  int getMemory(){
            return this.memory;
        }
        public void addMemory(int addValue){
        this.memory += addValue;
    }

    public void removeMemory(int removValue){
        this.memory -= removValue;
    }

    public int getThreadsOnWait() {
        return threadsOnWait;
    }

    public void addThreadsOnWait() {
        this.threadsOnWait += 1;
    }

    public void removeThreadsOnWait() {
        this.threadsOnWait -= 1;
    }

}
