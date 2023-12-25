package org.nimboscloud.server.workers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.nimboscloud.JobFunction.JobFunction;
import org.nimboscloud.JobFunction.JobFunctionException;
import org.nimboscloud.server.Server;
import org.nimboscloud.server.services.*;
import org.nimboscloud.server.skeletons.AuthenticationManagerSkeleton;

public class ServerWorker implements Runnable{
    private Socket socket;
    private AuthenticationManagerSkeleton authSkeleton;

    private Server server;
    public ServerWorker(Socket s, AuthenticationManagerSkeleton authSkeleton, Server server){
        this.socket = s;
        this.authSkeleton = authSkeleton;
        this.server = server;
    }

    public void run(){
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream());

            List<Thread> threadList = new ArrayList<>();

            //Thread t = new Thread(() -> {
            //    handleExec(threadList);
            //});
            //t.start();


            handle_cliente(in, out);




            socket.shutdownInput();

            out.println("App closed");

            socket.shutdownOutput();
            socket.close();
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public void handle_cliente(BufferedReader in, PrintWriter out) {

            try {
                String line;
                String[] parts;
                boolean login_flag = false;

                while ((line = in.readLine()) != null) {

                    try {
                        parts = line.split(" ");

                        int flag = 0;

                        if (!login_flag) {
                            flag = authSkeleton.processCommand(parts, out);
                        } else if (parts[0].equals("logout")) {
                            flag = authSkeleton.processCommand(parts, out);
                            if (flag == 1) {
                                login_flag = false;
                            }
                        }

                        if (flag == 2) {
                            login_flag = true;
                        }

                        if (login_flag) {
                            processCommand(line, out);
                        }


                    } catch (Exception e) {
                        System.out.println(e.getMessage());
                        out.println("Invalid input.");
                        out.flush();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();

            }
        }



    public byte[] StringToByteArray(String input){

        String[] clean_Input = input.substring(1, input.length() - 1).split(",");

        byte[] byteArray = new byte[clean_Input.length];

        for (int i = 0; i < clean_Input.length; i++) {
            int intValue = Integer.parseInt(clean_Input[i]);
            byteArray[i] = (byte) intValue;
        }

        return byteArray;
    }

    //função para o escalonamento (por fazer)
    public void handleExec(List<Thread> threadList) {
        while (true) {

        }
    }

    public void processCommand (String command, PrintWriter out) throws JobFunctionException, InterruptedException {

        String[] splittedCommand = command.split(" ");

        switch (splittedCommand[0]) {

            case "exec" -> {


                byte[] taskCode = StringToByteArray(splittedCommand[1]);


                //Thread t = new Thread(() -> {executeManager.executeJobFunction(taskCode, Integer.parseInt(splittedCommand[2]));});
                int mem = Integer.parseInt(splittedCommand[2]);
                Thread t = new Thread(() -> {
                    try {

                        boolean flag= server.startJob(mem);

                        if (!server.queue.isEmpty()){
                            String command2 = splittedCommand[1] + " " + splittedCommand[2];
                            server.queue.add(new Object[]{command2,out});
//
                        }
                        else if(flag){

                            byte[] response = JobFunction.execute(taskCode);
                            server.addMemory(mem);
                            out.println(response);
                            out.flush();
                            server.lockQueue.lock();
                            server.waitQueue.signalAll();
                            server.lockQueue.unlock();

                        }else{
                            String command2 = splittedCommand[1] + " " + splittedCommand[2];
                            server.queue.add(new Object[]{command2,out});
                        }

                    } catch (JobFunctionException e) {
                        server.addMemory(mem);
                        out.println("JobFunctionException caught: " + e.getMessage());
                        out.flush();
                        server.lockQueue.lock();
                        server.waitQueue.signalAll();
                        server.lockQueue.unlock();
                    }
                });
                t.start();

                System.out.println(server.getMemory());


            }

            case "status" -> {
                int memoryAvailable = server.getMemory();
                int waitList = server.getThreadsOnWait();

                String response = "Memory Available: " + memoryAvailable + " | Threads on Wait: " + waitList;

                out.println(response);
                out.flush();
            }

        }
    }
}
