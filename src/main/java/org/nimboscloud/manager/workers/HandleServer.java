package org.nimboscloud.manager.workers;

import org.nimboscloud.manager.services.QueueConnection;
import org.nimboscloud.manager.services.QueueList;
import org.nimboscloud.manager.services.TaggedConnection;
import static  org.nimboscloud.manager.services.TaggedConnection.FrameSend;
import static  org.nimboscloud.manager.services.TaggedConnection.FrameReceive;

import java.io.*;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class HandleServer implements Runnable{

    private ReentrantLock lockJob = new ReentrantLock();
    private Condition conditionlockJob = lockJob.newCondition();
    private Object controlock = new Object();
    private ReentrantLock lockElement = new ReentrantLock();
    public Object[] element;
    private ReentrantLock lockQueue;
    private Condition waitQueue;
    private Map<Integer, Object[]> clientOutMap = new HashMap<>();
    private Map<Integer, PedidoInfo> pedidoMap = new HashMap<>();
    private QueueList queueList;

    private int exit=0;
    private int numPedido;
    private Socket socket;

    public QueueConnection queueConnection = new QueueConnection();

    public HandleServer(Socket socket, Map<Integer, Object[]> clientOutMap, QueueList queueList){
        this.numPedido=1;
        this.socket=socket;
        this.clientOutMap = clientOutMap;
        this.queueList = queueList;
    }
    public static class PedidoInfo {
        private final int cliente;
        private final int pedidoCliente;
        private final int memPedido;


        public PedidoInfo( int cliente, int pedidoCliente, int memPedido) {
            this.cliente = cliente;
            this.pedidoCliente = pedidoCliente;
            this.memPedido = memPedido;
        }
        public int getCliente() {
            return cliente;
        }

        public int getPedidoCliente() {
            return pedidoCliente;
        }

        public int getMemPedido() {
            return memPedido;
        }
    }

    public void run() {
        try {
            DataInputStream in = new DataInputStream(socket.getInputStream());
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
            TaggedConnection taggedConnection = new TaggedConnection(in,out);

            int memory = in.readInt();
            queueConnection.setMemory(memory);

            queueList.addQueue(queueConnection);

            try {
                Thread t1 = new Thread(() -> {
                    try {
                        handleServerIn(taggedConnection);
                    } catch (IOException | InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                });

                t1.start();

                Thread t2 = new Thread(() -> {


                    try {
                        handleServerOut(taggedConnection);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }


                });

                t2.start();

                t1.join();
                t2.join();

                socket.shutdownOutput();
                socket.shutdownInput();
                socket.close();

//                int end = in.readInt();
//                t1.stop();
//                t2.stop();
//                socket.shutdownOutput();
//                socket.shutdownInput();
//                socket.close();

            } catch (Exception e) {
                e.printStackTrace();
            }



        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void handleServerIn(TaggedConnection taggedConnection) throws IOException, InterruptedException {
        lockQueue = queueConnection.getLock();
        waitQueue = queueConnection.getCondition();
        while (true) {

            while (queueConnection.isEmpty()) {
                if(exit==0) {
                    lockQueue.lock();
                    waitQueue.await();
                    lockQueue.unlock();
                }
                if(exit==1){
                    while(queueConnection.getThreadsExcuting()>0){
                        System.out.println(queueConnection.getThreadsExcuting());
                        synchronized (controlock) {
                            controlock.wait();
                        }
                    }
                    FrameSend frame = new FrameSend(-1, null);
                    try {
                        taggedConnection.sendS(frame);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    return;
                }
            }
            lockElement.lock();
            element = queueConnection.getNextJob();
            lockElement.unlock();

            //System.out.println("tag -> " + element[1] + "mem -> " + element[2] + "ager -> " + element[5]);
            Thread h = new Thread(() -> {
                while(queueConnection.getControl()==0) {
                    int x=0;
                    lockQueue.lock();
                    try {
                        waitQueue.await();

                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    lockElement.lock();
                    try {

                        if (!queueConnection.isEmpty()){

                            Object [] aux = queueConnection.getNextJob();
                            if (aux!= element){
                                x=1;
                                element=aux;
                            }
                        }
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    if(x==1) {
                        lockJob.lock();
                        conditionlockJob.signalAll();
                        lockJob.unlock();
                        if ((int) element[5] == 5) {
                            lockElement.unlock();
                            lockQueue.unlock();
                            break;
                        }
                    }
                    lockElement.unlock();
                    lockQueue.unlock();
                }
            });

            int r=0;
            while (!this.queueConnection.startJob((int)element[2],lockElement)) {
                try {
                    lockJob.lock();
                    if(r==0) h.start();
                    r=r+1;
                    conditionlockJob.await();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                lockJob.unlock();
            }
            queueConnection.rem(element);
            Object[] elementAux = element;
            queueConnection.setControl(1);
            lockQueue.lock();
            waitQueue.signalAll();
            lockQueue.unlock();
            lockElement.unlock();

            h.join();
            queueConnection.setControl(0);
            queueConnection.increaseAgerJobs();
            //System.out.println(elementAux[1]);

            Thread t = new Thread(() -> {


                int client = (int) elementAux[0];

                int tag = (int) elementAux[1];

                int memPedido = (int) elementAux[2];

                byte[] taskCode = (byte[]) elementAux[3];

                DataOutputStream outPedido = (DataOutputStream) elementAux[4];

                int pedido = this.numPedido;
                this.numPedido++;




                adicionarPedido(pedido, client, tag, memPedido);
                FrameSend frame = new FrameSend(pedido, taskCode);
                try {
                    taggedConnection.sendS(frame);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });

            t.start();
        }

    }
    
    
    public void handleServerOut(TaggedConnection taggedConnection) throws IOException {

        while (true) {

            FrameReceive frame = taggedConnection.receiveR();
            if (frame.tag > -1) {

                Thread t = new Thread(() -> {

                    PedidoInfo pedido = obterPedido(frame.tag);
                    queueConnection.addMemory(pedido.memPedido);
                    queueConnection.rmThreadsExcuting();
                    Object[] aux = clientOutMap.get(pedido.cliente);
                    TaggedConnection taggedConnection1 = (TaggedConnection) aux[1];
                    TaggedConnection.FrameReceiveClient frameClient;
                    if (frame.exp == 0) {
                        frameClient = new TaggedConnection.FrameReceiveClient(frame.exp, pedido.pedidoCliente, frame.data, null);
                    } else if (frame.exp == 1) {
                        frameClient = new TaggedConnection.FrameReceiveClient(frame.exp, pedido.pedidoCliente, null, "Could not compute the job.");
                    }else{
                        frameClient = new TaggedConnection.FrameReceiveClient(frame.exp, pedido.pedidoCliente, null, "Job computation failed due to runtime error.");
                    }
                    try {
                        taggedConnection1.sendC(frameClient);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    synchronized (controlock) {
                        controlock.notifyAll();
                    }
                    lockJob.lock();
                    conditionlockJob.signalAll();
                    lockJob.unlock();
                });
                t.start();
            } else if(frame.tag == -1){
                exit=1;
                queueList.remQueue(queueConnection);
                lockQueue.lock();
                waitQueue.signalAll();
                lockQueue.unlock();
            } else{
                break;
            }
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



    public void adicionarPedido(int pedido, int client, int tag, int memPedido) {

        PedidoInfo info = new PedidoInfo(client, tag, memPedido);
        pedidoMap.put(pedido, info);
    }

    public PedidoInfo obterPedido(int pedido) {
        return pedidoMap.get(pedido);
    }

}

