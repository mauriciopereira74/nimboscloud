package org.nimboscloud.server;
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
                ServerSocket ss = new ServerSocket(12345);

                while (true) {
                    Socket socket = ss.accept();
                    Thread t = new Thread(new ServerWorker(socket));
                    t.start();

                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

}
