package org.nimboscloud;

import org.nimboscloud.JobFunction.JobFunction;
import org.nimboscloud.JobFunction.JobFunctionException;

import java.io.PrintWriter;

public class Trash {


//    public byte[] StringToByteArray(String input){
//
//        String[] clean_Input = input.substring(1, input.length() - 1).split(",");
//
//        byte[] byteArray = new byte[clean_Input.length];
//
//        for (int i = 0; i < clean_Input.length; i++) {
//            int intValue = Integer.parseInt(clean_Input[i]);
//            byteArray[i] = (byte) intValue;
//        }
//
//        return byteArray;
//    }
//
//    public void processCommand (String command, PrintWriter out) throws JobFunctionException, InterruptedException {
//
//        String[] splittedCommand = command.split(" ");
//
//        switch (splittedCommand[0]) {
//
//            case "exec" -> {
//
//
//                byte[] taskCode = StringToByteArray(splittedCommand[1]);
//
//                int mem = Integer.parseInt(splittedCommand[2]);
//                Thread t = new Thread(() -> {
//                    try {
//
//                        boolean flag= server.startJob(mem);
//
//                        if (!server.queue.isEmpty()){
//                            String command2 = splittedCommand[1] + " " + splittedCommand[2];
//                            server.queue.add(new Object[]{command2,out});
////
//                        }
//                        else if(flag){
//
//                            byte[] response = JobFunction.execute(taskCode);
//                            server.addMemory(mem);
//                            out.println(response);
//                            out.flush();
//                            server.lockQueue.lock();
//                            server.waitQueue.signalAll();
//                            server.lockQueue.unlock();
//
//                        }else{
//                            String command2 = splittedCommand[1] + " " + splittedCommand[2];
//                            server.queue.add(new Object[]{command2,out});
//                        }
//
//                    } catch (JobFunctionException e) {
//                        server.addMemory(mem);
//                        out.println("JobFunctionException caught: " + e.getMessage());
//                        out.flush();
//                        server.lockQueue.lock();
//                        server.waitQueue.signalAll();
//                        server.lockQueue.unlock();
//                    }
//                });
//                t.start();
//
//                System.out.println(server.getMemory());
//            }
//
//            case "status" -> {
//                int memoryAvailable = server.getMemory();
//                int waitList = server.getThreadsOnWait();
//
//                String response = "Memory Available: " + memoryAvailable + " | Threads on Wait: " + waitList;
//
//                out.println(response);
//                out.flush();
//            }
//
//        }
//    }
}

