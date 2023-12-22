package org.nimboscloud.server.workers;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Arrays;

import org.nimboscloud.JobFunction.JobFunctionException;
import org.nimboscloud.server.services.*;
import org.nimboscloud.server.skeletons.AuthenticationManagerSkeleton;

public class ServerWorker implements Runnable{
    private Socket socket;
    private ExecuteManager executeManager;
    private AuthenticationManagerSkeleton authSkeleton;


    public ServerWorker(Socket s, ExecuteManager executeManager, AuthenticationManagerSkeleton authSkeleton){
        this.socket = s;
        this.executeManager = executeManager;
        this.authSkeleton =authSkeleton;
    }

    public void run(){
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream());

            String line;

            line = in.readLine();

            String[] parts = line.split(" ");

            authSkeleton.processCommand(parts, out);
            out.flush();

            while ((line = in.readLine()) != null) {
                try {
                    parts = line.split(" ");

                    out.println(processCommand(line));
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

    public byte[] StringToByteArray(String input){
        String[] clean_Input = input.substring(1, input.length() - 1).split(", ");

        byte[] byteArray = new byte[clean_Input.length];
        for (int i = 0; i < clean_Input.length; i++) {
            int intValue = Integer.parseInt(clean_Input[i]);
            byteArray[i] = (byte) intValue;
        }

        return byteArray;
    }

    public byte[] processCommand (String command) throws JobFunctionException, InterruptedException {
        String[] splittedCommand = command.split(" ");

        if ("exec".equals(splittedCommand[0])) {

            byte[] taskCode = StringToByteArray(splittedCommand[1]);

            byte[] response = executeManager.executeJobFunction(taskCode, Integer.parseInt(splittedCommand[2]));

            return response; // compor
        }
        if("status".equals(splittedCommand[0])) {
            String response = executeManager.checkStatus();

            //return  response;
        }

        return null;
    }
}
