package org.nimboscloud.server.workers;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import org.nimboscloud.JobFunction.JobFunctionException;
import org.nimboscloud.server.services.*;

public class ServerWorker implements Runnable{
    private Socket socket;
    private ExecuteManager executeManager;

    public ServerWorker(Socket s, ExecuteManager executeManager){
        this.socket = s;
        this.executeManager = executeManager;
    }


    public void run(){
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream());

            String line;

            while ((line = in.readLine()) != null) {
                try {
                    String command = line;

                    out.println(processCommand(command));

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

    public byte[] processCommand (String command) throws JobFunctionException, InterruptedException {
        String[] splittedCommand = command.split(" ");

        if ("execute".equals(splittedCommand[0])) {

            byte[] response = executeManager.executeJobFunction(splittedCommand[1].getBytes());

            return response;
        }

        return null;
    }
}
