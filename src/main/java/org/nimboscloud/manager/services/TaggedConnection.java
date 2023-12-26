package org.nimboscloud.manager.services;
import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.locks.ReentrantLock;

public class TaggedConnection implements AutoCloseable {
    private Socket socket ;
    private final DataInputStream input;
    private final DataOutputStream output;

    private ReentrantLock sendlock = new ReentrantLock();

    private ReentrantLock receivelock = new ReentrantLock();
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
    public TaggedConnection(DataInputStream in, DataOutputStream out) throws IOException {
        this.input= in;
        this.output= out;
    }
    public void sendS(FrameSend frame) throws IOException {
        sendlock.lock();
        try{
            output.writeInt(frame.tag);
            output.writeInt(frame.data.length);
            output.write(frame.data);
            output.flush();

        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            sendlock.unlock();
        }
    }

    public void sendR (FrameReceive frame) throws IOException {
        sendlock.lock();
        try{
            output.writeInt(frame.tag);
            output.writeInt(frame.exp);
            if (frame.exp==0) {
                output.writeInt(frame.data.length);
                output.write(frame.data);
            }
            output.flush();

        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            sendlock.unlock();
        }
    }

    public FrameSend receiveS() throws IOException {
        receivelock.lock();
        try {
            int tag = input.readInt();
            int length = input.readInt();
            byte[] data = new byte[length];
            input.readFully(data);
            return  new FrameSend(tag,data);
        } finally {
            receivelock.unlock();
        }
    }

    public FrameReceive receiveR() throws IOException {
        receivelock.lock();
        try {
            int tag = input.readInt();
            int exp = input.readInt();
            byte[] data;
            if (exp==0) {
                int length = input.readInt();
                data = new byte[length];
                input.readFully(data);
            }
            else {
                data = null;
            }

            return  new FrameReceive(tag,exp,data);
        } finally {
            receivelock.unlock();
        }
    }
    public void close() throws IOException {
        socket.close();
    }
}

