package org.nimboscloud.manager.workers;

import java.io.IOException;
import java.net.Socket;
import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.BlockingQueue;

import org.nimboscloud.manager.skeletons.AuthenticationManagerSkeleton;

public class HandleClient implements Runnable {
    private Socket socket;
    private AuthenticationManagerSkeleton authSkeleton;
    private int cliente;
    public Map<Integer,BlockingQueue> listQueue = new HashMap<>();
    public ReentrantLock lockList = new ReentrantLock();

    public HandleClient(Socket s, AuthenticationManagerSkeleton authSkeleton, int cliente, Map<Integer, BlockingQueue> listQueue, ReentrantLock lockList) {
        this.socket = s;
        this.authSkeleton = authSkeleton;
        this.cliente = cliente;
        this.listQueue.putAll(listQueue);
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
                        lockList.lock();
                        BlockingQueue aux = listQueue.get(1000);
                        if(aux!=null) {
                                aux.add(new Object[]{cliente, tag, mem, data, out});
                        }
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
