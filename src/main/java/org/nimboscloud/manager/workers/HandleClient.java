package org.nimboscloud.manager.workers;

import java.io.IOException;
import java.net.Socket;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.BlockingQueue;

import org.nimboscloud.manager.skeletons.AuthenticationManagerSkeleton;

public class HandleClient implements Runnable {
    private Socket socket;
    private AuthenticationManagerSkeleton authSkeleton;
    private int cliente;
    public List<Object[]> listQueue = new ArrayList<>();
    public ReentrantLock lockList = new ReentrantLock();

    public HandleClient(Socket s, AuthenticationManagerSkeleton authSkeleton, int cliente, List<Object[]> listQueue, ReentrantLock lockList) {
        this.socket = s;
        this.authSkeleton = authSkeleton;
        this.cliente = cliente;
        this.listQueue = listQueue;
        this.lockList = lockList;
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

    private BlockingQueue selectServer(int mem){
        BlockingQueue lowestMemoryQueue = null;
        int lowestMem = 0;

        for (Object[] server : this.listQueue) {
            if((int)server[0] < mem) {
                continue;
            }

            if(((BlockingQueue)server[1]).size() == 0) {
                return (BlockingQueue) server[1];
            }

            int accumulator = 0;
            for (Object[] job : (BlockingQueue<Object[]>) server[1]) {
                accumulator += (int) job[2];
            }

            if (lowestMem == 0 || accumulator < lowestMem) {
                lowestMem = accumulator;
                lowestMemoryQueue = (BlockingQueue<Object[]>) server[1];
            }
        }
        return  lowestMemoryQueue;
    }

    public void handle_cliente(DataInputStream in, DataOutputStream out) {
        while (true) {

            try {
                int command;
                command = in.readInt();
                boolean flag;

                switch (command) {
                    case 0 -> { // register
                        String username = in.readUTF();
                        String password = in.readUTF();
                        flag = authSkeleton.processRegister(username, password, out);
                    }
                    case 1 -> { //login
                        String username = in.readUTF();
                        String password = in.readUTF();
                        flag = authSkeleton.processLogin(username, password, out);
                    }
                    case 2 -> { // logout
                        String username = in.readUTF();
                        flag = authSkeleton.processLogout(username, out);
                    }
                    case 3 -> { // exec
                        int tag = in.readInt();
                        int mem = in.readInt();
                        String data = in.readUTF();
                        lockList.lock();   // o Lock está a funcionar ????

                        BlockingQueue aux = selectServer(mem);
                        System.out.println("mem = " + mem + "tag" + tag);
                        aux.add(new Object[]{cliente, tag, mem, data, out});
                        lockList.unlock();
                    }
                    case 4 -> { // status

                    }
                }

            } catch (IOException e) {
                e.printStackTrace();

            }
        }
    }

    public int getCliente(){
        return this.cliente;
    }


}
