package org.nimboscloud.server;

import org.nimboscloud.manager.services.TaggedConnection;
import static  org.nimboscloud.manager.services.TaggedConnection.FrameSend;
import static  org.nimboscloud.manager.services.TaggedConnection.FrameReceive;
import org.nimboscloud.JobFunction.JobFunction;
import org.nimboscloud.JobFunction.JobFunctionException;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class Server {

    private static int memory;
    private static final Object lock = new Object();
    private static volatile boolean shutdownCondition = false;



    public Server(){}

    public static void main(String[] args) {
        //this.memory = Integer.parseInt(args[1]);
        try {
            Socket socket = new Socket("localhost", 1667);

            Server server = new Server();

            DataInputStream in = new DataInputStream(socket.getInputStream());
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
            TaggedConnection taggedConnection = new TaggedConnection(in, out);

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                // Perform cleanup or termination tasks
                try {
                    server.close(out, taggedConnection, socket);

                } catch (IOException | InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }));

            try {
                BufferedReader systemIn = new BufferedReader(new InputStreamReader(System.in));

                System.out.print("Enter the memory of the Server: ");
                String userInput = systemIn.readLine();

                // Parse the string input to an integer
                memory = Integer.parseInt(userInput);

            } catch (IOException | NumberFormatException e) {
                // Handle exceptions (e.g., invalid input)
                System.err.println("Error: " + e.getMessage());
            }

            out.writeInt(memory);
            out.flush();

            while (true) {
                FrameSend frameSend = taggedConnection.receiveS();

                if (frameSend.tag != -1) {

                    Thread t = new Thread(() -> {
                        try {
                            byte[] response = JobFunction.execute(frameSend.data);
                            System.out.println(Arrays.toString(response));
                            FrameReceive frameReceive = new FrameReceive(frameSend.tag, 0, response);
                            taggedConnection.sendR(frameReceive);

                        } catch (JobFunctionException e) {
                            FrameReceive frameReceive = new FrameReceive(frameSend.tag, 1, null);
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
                }else{
                    synchronized (lock) {
                        taggedConnection.sendR(new FrameReceive(-2, 0, null));
                        shutdownCondition = true;
                        lock.notify(); // Notify the waiting thread (if any)
                    }
                    break;
                    }
                }



            } catch(IOException e){
                throw new RuntimeException(e);
            }

    }

    public void close(DataOutputStream out,TaggedConnection taggedConnection,Socket socket) throws IOException, InterruptedException {
        taggedConnection.sendR(new FrameReceive(-1, 0, null));
        out.flush();

        synchronized (lock) {
            while (!shutdownCondition) {
                lock.wait();
            }
        }
        socket.shutdownOutput();
        socket.shutdownInput();
        socket.close();
    }
}

