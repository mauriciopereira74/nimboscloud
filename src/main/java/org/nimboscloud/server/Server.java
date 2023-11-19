package org.nimboscloud.server;

import org.nimboscloud.server.services.AuthenticationManager;
import org.nimboscloud.server.skeletons.AuthenticationManagerSkeleton;
import org.nimboscloud.server.workers.ServerWorker;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {

        public static void main(String[] args) {
            try {
                ServerSocket ss = new ServerSocket(1666);

                AuthenticationManager authManager = new AuthenticationManager();
                AuthenticationManagerSkeleton authSkeleton = new AuthenticationManagerSkeleton(authManager);

                // Crie um administrador para testes
                authManager.createAdminUser("admin", "admin");

                while (true) {
                    Socket socket = ss.accept();
                    Thread t = new Thread(new ServerWorker(socket,authSkeleton));
                    t.start();

                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

}
