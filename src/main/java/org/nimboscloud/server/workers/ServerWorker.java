package org.nimboscloud.server.workers;

import org.nimboscloud.server.services.AuthenticationManager;
import org.nimboscloud.server.skeletons.AuthenticationManagerSkeleton;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ServerWorker implements Runnable{
    private Socket socket;
    private AuthenticationManagerSkeleton authSkeleton;

    public ServerWorker(Socket s, AuthenticationManagerSkeleton authSkeleton){
        this.socket = s;
        this.authSkeleton =authSkeleton;
    }


    public void run(){
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream());

            String line;

            while ((line = in.readLine()) != null) {
                try {
                    String[] parts = line.split(" ");
                    authSkeleton.processCommand(parts, out);
                    out.flush();
                } catch (Exception e) {
                    out.println("Invalid input.");
                    out.flush();
                }
            }
            socket.shutdownInput();

            out.println("App closed");

            socket.shutdownOutput();
            socket.close();
        }catch(Exception e){
            e.printStackTrace();
        }
    }


}
