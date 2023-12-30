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
public class Server {

    private static int memory;

    public Server(){}

    public static void main(String[] args) {
        //this.memory = Integer.parseInt(args[1]);
        try {
            Socket socket = new Socket("localhost", 1667);

            DataInputStream in = new DataInputStream(socket.getInputStream());
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
            TaggedConnection taggedConnection = new TaggedConnection(in,out);

//            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
//                // Perform cleanup or termination tasks
//                try {
//                    close(socket,out);
//                } catch (IOException e) {
//                    throw new RuntimeException(e);
//                }
//
//            }));

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

            while (true){
                FrameSend frameSend = taggedConnection.receiveS();

                Thread t = new Thread(() -> {
                    try {
                        byte[] response = JobFunction.execute(frameSend.data);
                        System.out.println(Arrays.toString(response));
                        FrameReceive frameReceive = new FrameReceive(frameSend.tag,0,response);
                        taggedConnection.sendR(frameReceive);

                    } catch (JobFunctionException e) {
                        FrameReceive frameReceive = new FrameReceive(frameSend.tag,1,null);
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

    private static void close(Socket socket, DataOutputStream out) throws IOException {
        out.writeInt(999);
        out.flush();

        socket.shutdownOutput();
        socket.shutdownInput();
        socket.close();
    }
}

