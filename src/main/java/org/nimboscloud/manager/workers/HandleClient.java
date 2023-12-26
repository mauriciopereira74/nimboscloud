package org.nimboscloud.manager.workers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.io.*;

import org.nimboscloud.manager.skeletons.AuthenticationManagerSkeleton;

public class HandleClient implements Runnable {
    private Socket socket;
    private AuthenticationManagerSkeleton authSkeleton;

    public HandleClient(Socket s, AuthenticationManagerSkeleton authSkeleton) {
        this.socket = s;
        this.authSkeleton = authSkeleton;
    }

    public void run() {
        try {
            DataInputStream in = new DataInputStream(socket.getInputStream());
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());

            handle_cliente(in, out);

            socket.shutdownInput();

            out.writeUTF("App closed");
            out.flush();

            socket.shutdownOutput();
            socket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void handle_cliente(DataInputStream in, DataOutputStream out) {

        try {
            int command;
            command = in.readInt();
            boolean flag;

            switch (command){
                case 0 -> { // register
                    String username = in.readUTF();
                    String password = in.readUTF();
                    flag = authSkeleton.processRegister(username,password,out);
                }
                case 1 -> { //login
                    String username = in.readUTF();
                    String password = in.readUTF();
                    flag = authSkeleton.processLogin(username,password,out);
                }
                case 2 ->{ // logout
                    String username = in.readUTF();
                    flag= authSkeleton.processLogout(username,out);
                }
                case 3->{ // exec
                    int mem = in.readInt();
                    int length = in.readInt();
                    byte[] data = new byte[length];
                    in.readFully(data);
                }
                case 4->{ // status

                }
            }

        } catch (IOException e) {
            e.printStackTrace();

        }
    }


}
