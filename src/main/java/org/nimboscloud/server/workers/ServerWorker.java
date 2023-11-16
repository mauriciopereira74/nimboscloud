package org.nimboscloud.server.workers;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ServerWorker implements Runnable{
    private Socket socket;

    public ServerWorker( Socket s){
        this.socket = s;
    }


    public void run(){
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream());

            String line;

            while ((line = in.readLine()) != null) {
                try {
                    String command = line;


                    out.println("Command received: " + command);
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
