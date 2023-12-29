package org.nimboscloud.manager.workers;

import org.nimboscloud.manager.services.TaggedConnection;
import static  org.nimboscloud.manager.services.TaggedConnection.FrameSend;
import static  org.nimboscloud.manager.services.TaggedConnection.FrameReceive;

import java.io.*;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class HandleServer implements Runnable{
    public List<Object[]> waitList = new ArrayList<>();
    private ReentrantLock lockMemory = new ReentrantLock();
    private ReentrantLock lockThreads = new ReentrantLock();
    private ReentrantLock lockQueue =  new ReentrantLock();
    private Condition waitQueue = lockQueue.newCondition();
    private Map<Integer, Object[]> clientOutMap = new HashMap<>();
    private Map<Integer, PedidoInfo> pedidoMap = new HashMap<>();

    private List<Object[]> listQueue = new ArrayList<>();
    private ReentrantLock lockList = new ReentrantLock();

    private int numPedido;
    private int memory = 0;
    private int id;
    private int threadsOnWait = 0;
    private Socket socket;

    public HandleServer(Socket socket,Map<Integer, Object[]> clientOutMap, List<Object[]> listQueue, ReentrantLock lockList){
        this.numPedido=1;
        this.socket=socket;
        this.clientOutMap = clientOutMap;
        this.listQueue = listQueue;
        this.lockList=lockList;
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
            memory = in.readInt();

            Object[] memoryServer = {memory,waitList};
            lockList.lock();
            try {
                listQueue.add(memoryServer);
            } finally {
                lockList.unlock();
            }
            id = listQueue.indexOf(memoryServer);

            try {
                Thread t = new Thread(() -> {
                    try {
                        handleServerIn(in,taggedConnection);
                    } catch (InterruptedException | IOException e) {
                        throw new RuntimeException(e);
                    }
                });

                t.start();

            } catch (Exception e) {
                e.printStackTrace();
            }

            handleServerOut(taggedConnection);


            socket.shutdownInput();

            out.writeUTF("App closed");
            out.flush();

            socket.shutdownOutput();
            socket.close();
        } catch (IOException e) {
            listQueue.remove(id);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Object[] getNextJob() throws InterruptedException {
        Object[] result = (Object[]) waitList.get(0);
        int lowestMem = (int)waitList.get(0)[5];

        for (Object[] element : waitList) {
            if ((int) element[5] == 3) {
                result = element;
                break;
            }
            if ((int) element[2] < lowestMem) {
                lowestMem =(int) element[3];
                result = element;
            }
        }

        waitList.remove(result);
        return result;
    }


    public void handleServerIn(DataInputStream in,TaggedConnection taggedConnection) throws InterruptedException, IOException {
        while (true) {

            System.out.println("wait list" + waitList.isEmpty());
            if (!waitList.isEmpty()) {
                Object[] element = getNextJob();

                //System.out.println("tag" + element[2] + "mem"  + element[3]);

                lockQueue.lock();
                while (!this.startJob((int) element[2])) {
                    this.waitQueue.await();
                }
                lockQueue.unlock();

                int client = (int) element[0];

                int tag = (int) element[1];

                int memPedido = (int) element[2];
                String full_string = (String) element[3];
                byte[] taskCode = StringToByteArray(full_string);

                DataOutputStream outPedido = (DataOutputStream) element[4];

                int pedido = this.numPedido;
                this.numPedido++;

                TaggedConnection taggedConnection1 = new TaggedConnection(in, outPedido);

                clientOutMap.putIfAbsent(client, new Object[]{outPedido, taggedConnection1});

                Thread t = new Thread(() -> {
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
    }
    


    public void handleServerOut(TaggedConnection taggedConnection) throws IOException {

        while (true){

            FrameReceive frame = taggedConnection.receiveR();

            Thread t = new Thread(() -> {
                PedidoInfo pedido = obterPedido(frame.tag);
                addMemory(pedido.memPedido);
                Object[] aux = clientOutMap.get(pedido.cliente);
                TaggedConnection taggedConnection1 = (TaggedConnection) aux[1];
                TaggedConnection.FrameReceiveClient frameClient;
                if (frame.exp==0) {
                    frameClient = new TaggedConnection.FrameReceiveClient(frame.exp, pedido.pedidoCliente, frame.data, null);
                } else{
                    frameClient = new TaggedConnection.FrameReceiveClient(frame.exp, pedido.pedidoCliente, null, "Could not compute the job.");
                }
                try {
                    taggedConnection1.sendC(frameClient);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                lockQueue.lock();
                waitQueue.signalAll();
                lockQueue.unlock();
            });
            t.start();
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
    public  int getMemory(){
        try{
            lockMemory.lock();
            return this.memory;
        } finally {
            lockMemory.unlock();
        }
    }
    public void addMemory(int addValue){
        try{
            lockMemory.lock();
            this.memory += addValue;
        }finally {
            lockMemory.unlock();
        }
    }

    public void removeMemory(int removValue){
        try{
            lockMemory.lock();
            this.memory -= removValue;
        }finally {
            lockMemory.unlock();
        }
    }

    public boolean startJob(int mem){
        try{
            lockMemory.lock();
            if(mem <= this.getMemory()){
                this.removeMemory(mem);
                return true;
            }else{
                return false;
            }
        }finally {
            lockMemory.unlock();
        }
    }

    public int getThreadsOnWait() {
        try{
            lockThreads.lock();
            return threadsOnWait;
        }finally {
            lockThreads.unlock();
        }
    }

    public void addThreadsOnWait() {
        try{
            lockThreads.lock();
            this.threadsOnWait += 1;
        }finally {
            lockThreads.unlock();
        }

    }

    public void removeThreadsOnWait() {
        try{
            lockThreads.lock();
            this.threadsOnWait -= 1;
        }
        finally {
            lockThreads.unlock();
        }
    }

    public void adicionarPedido(int pedido, int client, int tag, int memPedido) {

        PedidoInfo info = new PedidoInfo(client, tag, memPedido);
        pedidoMap.put(pedido, info);
    }

    public PedidoInfo obterPedido(int pedido) {
        return pedidoMap.get(pedido);
    }

}

