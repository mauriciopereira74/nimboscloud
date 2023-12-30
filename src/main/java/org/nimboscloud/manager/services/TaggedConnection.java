package org.nimboscloud.manager.services;
import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Arrays;
import java.util.concurrent.locks.ReentrantLock;

public class TaggedConnection implements AutoCloseable {
    private Socket socket ;
    private final DataInputStream input;
    private final DataOutputStream output;

    private ReentrantLock sendlockS = new ReentrantLock();

    private ReentrantLock sendlockC = new ReentrantLock();

    private ReentrantLock sendlockR = new ReentrantLock();

    private ReentrantLock receivelockR = new ReentrantLock();

    private ReentrantLock receivelockS = new ReentrantLock();
    public static class FrameSend {
        public final int tag;

        public final byte[] data;

        public FrameSend(int tag, byte[] data) { this.tag = tag; this.data = data;}

    }

    public static class FrameReceive {
        public final int tag;
        public final int exp;

        public final byte[] data;
        public FrameReceive(int tag, int exp, byte[] data) { this.tag = tag; this.data = data; this.exp = exp;}

    }

    public static class FrameReceiveClient {
        public final int exp;
        public final int pedidoCliente;

        public final byte[] data;

        public final String messageException;
        public FrameReceiveClient(int exp, int pedidoCliente, byte[] data, String messageException) { this.exp = exp; this.data = data; this.pedidoCliente = pedidoCliente; this.messageException = messageException;}

    }
    public TaggedConnection(DataInputStream in, DataOutputStream out) throws IOException {
        this.input= in;
        this.output= out;
    }

    public void sendS(FrameSend frame) throws IOException {
        sendlockS.lock();
        try{
            if(frame.tag!=-1) {
                output.writeInt(frame.tag);
                output.writeInt(frame.data.length);
                output.write(frame.data);
                output.flush();
            }else{
                output.writeInt(frame.tag);
                output.flush();
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            sendlockS.unlock();
        }
    }

    public void sendR (FrameReceive frame) throws IOException {
        sendlockR.lock();
        try{
            if(frame.tag>-1) {
                output.writeInt(frame.tag);

                output.writeInt(frame.exp);
                if (frame.exp == 0) {
                    output.writeInt(frame.data.length);
                    output.write(frame.data);
                }
                output.flush();
            } else{
                output.writeInt(frame.tag);
                output.flush();
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            sendlockR.unlock();
        }
    }

    public void sendC (FrameReceiveClient frame) throws IOException {
        sendlockC.lock();
        try{
            output.writeInt(0);
            output.writeInt(frame.exp);
            output.writeInt(frame.pedidoCliente);
            if (frame.exp==0) {
                output.writeInt(frame.data.length);
                output.write(frame.data);
            } else{
                output.writeUTF(frame.messageException);
            }
            output.flush();

        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            sendlockC.unlock();
        }
    }

    public void SendStatus (String res){
        sendlockC.lock();
        try{
            output.writeInt(1);
            output.writeUTF(res);
            output.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            sendlockC.unlock();
        }

    }

    public FrameSend receiveS() throws IOException {
        receivelockS.lock();
        try {
            int tag = input.readInt();
            if(tag!=-1) {
                int length = input.readInt();
                byte[] data = new byte[length];
                input.readFully(data);
                return new FrameSend(tag, data);
            } else{
                return new FrameSend(tag, null);
            }
        } finally {
            receivelockS.unlock();
        }
    }

    public FrameReceive receiveR() throws IOException {
        receivelockR.lock();
        try {
            int tag = input.readInt();
            if(tag>-1) {
                int exp = input.readInt();
                byte[] data;
                if (exp == 0) {
                    int length = input.readInt();
                    data = new byte[length];
                    input.readFully(data);
                } else {
                    data = null;
                }

                return new FrameReceive(tag, exp, data);
            } else{
                return new FrameReceive(tag, 0, null);
            }
        } finally {
            receivelockR.unlock();
        }
    }

    public FrameReceiveClient receiveC() throws IOException {
        sendlockC.lock();
        try {
            int status = input.readInt();
            if(status==0) {
                int exp = input.readInt();
                int pedidoCliente = input.readInt();
                byte[] data;
                String exception;
                if (exp == 0) {
                    int length = input.readInt();
                    data = new byte[length];
                    input.readFully(data);
                    exception = null;
                } else {
                    data = null;
                    exception = input.readUTF();
                }

                return new FrameReceiveClient(exp, pedidoCliente, data, exception);

            }else{
                try{
                    return(new FrameReceiveClient(-1,0,null,input.readUTF()));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        } finally {
            sendlockC.unlock();
        }
    }
    public void close() throws IOException {
        socket.close();
    }
}

