package org.nimboscloud.manager.workers;

import java.io.IOException;
import java.net.Socket;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

import org.nimboscloud.manager.services.QueueConnection;
import org.nimboscloud.manager.services.QueueList;
import org.nimboscloud.manager.skeletons.AuthenticationManagerSkeleton;

public class HandleClient implements Runnable {
    private Socket socket;
    private AuthenticationManagerSkeleton authSkeleton;
    private int cliente;
    private QueueList queueList;

    private ReentrantLock readLock = new ReentrantLock();

    public HandleClient(Socket s, AuthenticationManagerSkeleton authSkeleton, int client, QueueList queueList) {
        this.socket = s;
        this.authSkeleton = authSkeleton;
        this.cliente = client;
        this.queueList = queueList;
    }

    public void run() {
        try {
            DataInputStream in = new DataInputStream(socket.getInputStream());
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());

            handle_cliente(in, out);

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
        boolean continueHandling = true;

        while (continueHandling) {
            try {
                int command = in.readInt();

                switch (command) {
                    case 0: // register
                        String username = in.readUTF();
                        String password = in.readUTF();
                        authSkeleton.processRegister(username, password, out);
                        break;
                    case 1: // login
                        String loginUsername = in.readUTF();
                        String loginPassword = in.readUTF();
                        authSkeleton.processLogin(loginUsername, loginPassword, out);
                        break;
                    case 2: // logout
                        String logoutUsername = in.readUTF();
                        authSkeleton.processLogout(logoutUsername, out);
                        break;
                    case 3: // exec
                        int tag = in.readInt();
                        int mem = in.readInt();

                        int length = in.readInt();

                        byte[] data = new byte[length];
                        in.readFully(data);

                        int ager = 0;

                        Object[] objeto = new Object[]{cliente, tag, mem, data, out, ager};
                        queueList.escolheQueue(objeto);
                        break;
                    case 4: // status
                        ReentrantLock lockList = queueList.getLockList();
                        lockList.lock();
                        StringBuilder res = new StringBuilder();
                        int count = 0;
                        if (!queueList.getList().isEmpty()) {
                            try {
                                for (QueueConnection queue : queueList.getList()) {
                                    res.append(queue.getQueueInfo(count));
                                    res.append("\n\n");
                                    count++;
                                }
                            } finally {
                                lockList.unlock();
                            }
                        } else {
                            res.append("Any Workers Available!\n");
                        }
                        out.writeUTF(res.toString());
                        out.flush();
                        break;
                    case 999:
                        socket.shutdownOutput();
                        socket.shutdownInput();
                        socket.close();

                        continueHandling = false;
                        break;
                    default:
                        System.out.println("Unknown command: " + command);
                        break;
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