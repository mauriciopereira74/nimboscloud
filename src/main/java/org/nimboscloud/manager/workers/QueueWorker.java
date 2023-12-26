package org.nimboscloud.manager.workers;

import java.io.PrintWriter;

import java.io.*;
import org.nimboscloud.JobFunction.JobFunction;
import org.nimboscloud.JobFunction.JobFunctionException;
//import org.nimboscloud.manager.Server;

public class QueueWorker implements Runnable {
   // private Server server;


   // public QueueWorker (Server server){
     //   this.server = server;}


    public void run(){
        try {
            handle_thread();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (JobFunctionException e) {
            throw new RuntimeException(e);
        }
    }

    public void handle_thread() throws InterruptedException, JobFunctionException {
//        while (true) {
//
//            Object[] element = server.queue.take();
//            String full_string = (String) element[0];
//            String[] splittedString = full_string.split(" ");
//            byte[] taskCode = StringToByteArray(splittedString[0]);
//            PrintWriter out = (PrintWriter) element[1];
//            int mem = Integer.parseInt(splittedString[1]);
//            server.lockQueue.lock();
//            try {
//                while (!server.startJob(mem)) {
//                    server.waitQueue.await();
//                }
//                Thread t = new Thread(() -> {
//                    try {
//                        byte[] response = JobFunction.execute(taskCode);
//                        server.addMemory(mem);
//                        out.println(response);
//                        out.flush();
//
//
//                    } catch (JobFunctionException e) {
//                        server.addMemory(mem);
//                        out.println("JobFunctionException caught: " + e.getMessage());
//                        out.flush();
//                    }
//                    server.lockQueue.lock();
//                    server.waitQueue.signalAll();
//                    server.lockQueue.unlock();
//                });
//
//                t.start();
//                System.out.println(server.getMemory());
//
//
//            } finally {
//                server.lockQueue.unlock();
//            }
//
//        }
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

}
