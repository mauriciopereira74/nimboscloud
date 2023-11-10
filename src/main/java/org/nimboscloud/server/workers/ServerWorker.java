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

            int sum = 0;
            int count = 0;
            String line;

            while ((line = in.readLine()) != null) {
                try {
                    int number = Integer.parseInt(line);
                    sum += number;
                    count++;
                    out.println("Sum so far: " + sum);
                    out.flush();
                } catch (NumberFormatException e) {
                    out.println("Invalid input. Please send numbers only.");
                    out.flush();
                }
            }
            socket.shutdownInput();

            if (count > 0) {
                double average = (double) sum / count;
                out.println("Average: " + average);
                out.flush();
            } else {
                out.println("No numbers received.");
                out.flush();
            }


            socket.shutdownOutput();
            socket.close();
        }catch(Exception e){
            e.printStackTrace();
        }
    }


}
