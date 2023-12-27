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
    public BlockingQueue<Object[]> queue = new LinkedBlockingQueue<>();
    private ReentrantLock lockMemory = new ReentrantLock();
    private ReentrantLock lockThreads = new ReentrantLock();
    private ReentrantLock lockQueue =  new ReentrantLock();
    private Condition waitQueue = lockQueue.newCondition();
    private Map<Integer, DataOutputStream> clientOutMap = new HashMap<>();
    private Map<Integer, PedidoInfo> pedidoMap = new HashMap<>();

    public List<Object[]> listQueue = new ArrayList<>();
    private ReentrantLock lockList = new ReentrantLock();

    private int numPedido;
    private static int memory;
    private int threadsOnWait = 0;
    private Socket socket;

    public HandleServer(Socket socket,Map<Integer, DataOutputStream> clientOutMap, List<Object[]> listQueue, ReentrantLock lockList){
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

            Object[]  memoryServer = {memory,queue};
            lockList.lock();
            try {
                listQueue.add(memoryServer);
            } finally {
                lockList.unlock();
            }

            try {
                Thread t = new Thread(() -> {
                    try {
                        handleServerIn(taggedConnection,queue);
                    } catch (InterruptedException e) {
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
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void handleServerIn(TaggedConnection taggedConnection,BlockingQueue<Object[]> queue) throws InterruptedException {
        while (true) {
            if(!queue.isEmpty()) {
                System.out.println("a queue antes do peak: " + queue);
                Object[] firsElement = queue.peek();

                while (!this.startJob((int) firsElement[2])) {
                    lockQueue.lock();
                    this.waitQueue.await();
                    lockQueue.unlock();
                }

                Object[] element = queue.take();
                int client = (int) element[0];

                int tag = (int) element[1];

                int memPedido = (int) element[2];
                String full_string = (String) element[3];
                byte[] taskCode = StringToByteArray(full_string);

                DataOutputStream outPedido = (DataOutputStream) element[4];

                int pedido = this.numPedido;
                this.numPedido++;
                clientOutMap.put(client, outPedido);

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
                try {
                    PedidoInfo pedido = obterPedido(frame.tag);
                    addMemory(pedido.memPedido);
                    DataOutputStream outputStream = clientOutMap.get(pedido.cliente);

                    String dataString = frame.exp + "|" + pedido.pedidoCliente + "|";

                    if (frame.exp == 1) {
                        dataString += "Could not compute the job.";

                    } else {
                        dataString += Arrays.toString(frame.data);
                    }

                    outputStream.writeUTF(dataString);
                    outputStream.flush();

                    lockQueue.lock();
                    waitQueue.signalAll();
                    lockQueue.unlock();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
            t.start();
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

