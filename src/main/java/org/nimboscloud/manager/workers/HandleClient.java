package org.nimboscloud.manager.workers;

import java.io.IOException;
import java.net.Socket;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

import org.nimboscloud.manager.services.QueueList;
import org.nimboscloud.manager.skeletons.AuthenticationManagerSkeleton;

public class HandleClient implements Runnable {
    private Socket socket;
    private AuthenticationManagerSkeleton authSkeleton;
    private int cliente;
    private QueueList queueList;

    private ReentrantLock readLock = new ReentrantLock();

    public HandleClient(Socket s, AuthenticationManagerSkeleton authSkeleton, int cliente, QueueList queueList) {
        this.socket = s;
        this.authSkeleton = authSkeleton;
        this.cliente = cliente;
        this.queueList = queueList;
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

    private int addAllJobMem(List<Object[]> list){
        int accumulator = 0;

        for (Object[] job : list) {
            accumulator += (int) job[2];
        }

        return accumulator;
    }

//    private List<Object[]> selectServer(int mem){
//        List<Object[]> lowestMemoryQueue = (List<Object[]>)this.listQueue.get(0)[1];
//        int lowestMem = addAllJobMem((List<Object[]>)((Object[])this.listQueue.get(0))[1]);
//
//        for (Object[] server : this.listQueue) {
//            if((int)server[0] < mem) {
//                continue;
//            }
//            if(((List)server[1]).isEmpty()) {
//                return (List<Object[]>) server[1];
//            }
//            int accumulator = addAllJobMem((List<Object[]>) server[1]);
//
//            //System.out.println("accumolator: " + accumulator + "lowesMem" + lowestMem);
//
//            if (accumulator < lowestMem) {
//                lowestMem = accumulator;
//                lowestMemoryQueue = (List<Object[]>) server[1];
//            }
//        }
//
//        return  lowestMemoryQueue;
//    }

    public void handle_cliente(DataInputStream in, DataOutputStream out) {
        while (true) {

            try {
                int command;
                readLock.lock();
                command = in.readInt();
                boolean flag;

                switch (command) {
                    case 0 -> { // register
                        String username = in.readUTF();
                        String password = in.readUTF();
                        authSkeleton.processRegister(username, password, out);
                    }
                    case 1 -> { //login
                        readLock.lock();
                        String username = in.readUTF();
                        String password = in.readUTF();
                        readLock.unlock();
                        authSkeleton.processLogin(username, password, out);
                    }
                    case 2 -> { // logout
                        String username = in.readUTF();
                        authSkeleton.processLogout(username, out);
                    }
                    case 3 -> { // exec
                        int tag = in.readInt();
                        int mem = in.readInt();
                        int ager = 0;
                        String data = in.readUTF();

                        Object[] objeto = new Object[]{cliente, tag, mem, data, out,ager};

                        queueList.escolheQueue(objeto);

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


    public void readRegister(DataInputStream in, DataOutputStream out) throws IOException {
        readLock.lock();
        String username = in.readUTF();
        String password = in.readUTF();
        authSkeleton.processRegister(username, password, out);
        readLock.unlock();
    }

    public void readLogin(DataInputStream in, DataOutputStream out) throws IOException {
        readLock.lock();
        String username = in.readUTF();
        String password = in.readUTF();
        readLock.unlock();
        authSkeleton.processLogin(username, password, out);
    }

    public void readLogout(DataInputStream in, DataOutputStream out) throws IOException {
        readLock.lock();
        String username = in.readUTF();
        authSkeleton.processLogout(username, out);
        readLock.unlock();
    }

    public void readExec(DataInputStream in, DataOutputStream out) throws IOException {
        readLock.lock();
        int tag = in.readInt();
        int mem = in.readInt();
        int ager = 0;
        String data = in.readUTF();
        readLock.unlock();

        Object[] objeto = new Object[]{cliente, tag, mem, data, out,ager};

        queueList.escolheQueue(objeto);


    }



}