package org.nimboscloud.server;

import org.nimboscloud.manager.services.TaggedConnection;
import static  org.nimboscloud.manager.services.TaggedConnection.FrameSend;
import static  org.nimboscloud.manager.services.TaggedConnection.FrameReceive;
import org.nimboscloud.JobFunction.JobFunction;
import org.nimboscloud.JobFunction.JobFunctionException;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class Server {

    public int memory;

    public Server() {
        this.memory = 1000;
    }

    public static void main(String[] args) {
        //this.memory = Integer.parseInt(args[1]);
        try {
            Socket socket = new Socket("localhost", 1667);

            DataInputStream in = new DataInputStream(socket.getInputStream());
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
            TaggedConnection taggedConnection = new TaggedConnection(in,out);

            while (true){
                FrameSend frameSend = taggedConnection.receiveS();

                Thread t = new Thread(() -> {
                    try {
                        byte[] response = JobFunction.execute(frameSend.data);
                        System.out.println(Arrays.toString(response));
                        FrameReceive frameReceive = new FrameReceive(frameSend.tag,0,response);
                        taggedConnection.sendR(frameReceive);

                    } catch (JobFunctionException e) {
                        FrameReceive frameReceive = new FrameReceive(frameSend.tag,0,null);
                        try {
                            taggedConnection.sendR(frameReceive);
                        } catch (IOException ex) {
                            throw new RuntimeException(ex);
                        }

                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });

                t.start();
            }


        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

