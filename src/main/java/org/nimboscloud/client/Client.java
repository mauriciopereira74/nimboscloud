package org.nimboscloud.client;

import org.nimboscloud.manager.services.TaggedConnection;

import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

public class Client {
    private String username;
    private int jobs=0;
    private Map<Integer, String> files = new HashMap<>();
    private Set<Integer> jobsWait = new HashSet<>();
    private static ReentrantLock sendlock = new ReentrantLock();

    public static void main(String[] args) {
        try {
            Socket socket = new Socket("localhost", 1666);

            DataInputStream in = new DataInputStream(socket.getInputStream());
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
            TaggedConnection taggedConnection = new TaggedConnection(in,null);
            // Display help menu as soon as the client connects
            processHelp();

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                // Perform cleanup or termination tasks
                try {
                    close(socket,out);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

            }));

            BufferedReader systemIn = new BufferedReader(new InputStreamReader(System.in));

            String userInput;
            while ((userInput = systemIn.readLine()) != null) {

                String[] parts = userInput.split(" ");

                if (parts[0].equals("exit")) {
                    System.out.println("A sair da aplicação...");
                    break;
                }
                else if (parts[0].equals("help")) {
                    processHelp();
                    continue;
                }
                Client client=new Client();
                client.handle_command(parts, in, out,taggedConnection,socket);

            }

            // Continue with the remaining code (shutdownOutput, etc.) as needed...

            socket.shutdownOutput();
            socket.shutdownInput();
            socket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private  void handle_command(String[] parts, DataInputStream in, DataOutputStream out,TaggedConnection taggedConnection,Socket socket){

        try{
            switch (parts[0]) {
                case "register":
                    out.writeInt(0);
                    out.writeUTF(parts[1]);
                    out.writeUTF(parts[2]);
                    out.flush();

                    boolean flag = in.readBoolean();
                    if (flag) {
                        System.out.println("Register successful for user: " + parts[1]);
                    }
                    break;
                case "login":
                    out.writeInt(1);
                    out.writeUTF(parts[1]);
                    out.writeUTF(parts[2]);
                    out.flush();

                    boolean loginFlag = in.readBoolean();
                    if (loginFlag) {
                        username = parts[1];
                        System.out.println("Login successful for user: " + username);
                        processAuthenticatedMenu(in, out, taggedConnection);
                    } else {
                        System.out.println("Login failed. Invalid credentials or user already logged in.\n");
                    }
                    break;
                case "exit":
                    System.out.println("Exiting the application...");
                    close(socket, out);
                    break;
                default:
                    System.out.println("Command doesn't exist!");
                    break;
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void processAuthenticatedMenu(DataInputStream in, DataOutputStream out,TaggedConnection taggedConnection) throws IOException, IOException {

        authMenu(username);
        jobs = 0;

        BufferedReader systemIn = new BufferedReader(new InputStreamReader(System.in));

        String userInput;
        label:
        while ((userInput = systemIn.readLine()) != null) {
            String[] parts = userInput.split(" ");

            switch (parts[0]) {
                case "logout" -> {
                    if(jobsWait.isEmpty()){
                        out.writeInt(2);
                        out.writeUTF(username);
                        out.flush();
                        boolean response = in.readBoolean();
                        if (response) {
                            System.out.println("Logout successful for user: " + username);
                            for (int i = 0; i < 3; ++i) System.out.println();
                            processHelp();
                            break label;
                        } else {
                            System.out.println("Logout error!");
                        }
                    }
                    else{
                        System.out.println("Wait until the end of your jobs!\n");
                    }
                }
                case "exec" -> {
                    int job = jobs;
                    jobs = jobs +1;
                    Thread t = new Thread(() -> {
                        try {
                            initExec(in, out, parts,job);
                            waitExec(taggedConnection);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });

                    t.start();
                }
                case "status" -> {
                    out.writeInt(4);
                    out.flush();

                    System.out.println(in.readUTF());
                }
                case "help" -> processHelp();
                default -> System.out.println("Comando desconhecido. Digite 'help' para obter a lista de comandos.");
            }
        }
    }

    private void initExec(DataInputStream in, DataOutputStream out, String[] parts, int job) throws IOException {

        sendlock.lock();

        jobsWait.add(job);

        byte[] byteArray = new byte[0];

        if(!parts[1].contains(".")){
            byteArray = parts[1].getBytes();
        }
        else{
            try {
                files.put(job,parts[1]);
                Path caminhoAbsoluto = Paths.get(parts[1]).toAbsolutePath();

                // Ler todos os bytes do arquivo
                byteArray = Files.readAllBytes(caminhoAbsoluto);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        out.writeInt(3);
        out.writeInt(job);
        out.writeInt(Integer.parseInt(parts[2]));
        out.writeInt(byteArray.length);
        out.write(byteArray);
        out.flush();

        sendlock.unlock();

        System.out.println(" | Pedido Com Tag: "+ job + " |");

    }

    private void waitExec(TaggedConnection taggedConnection) throws IOException {

        TaggedConnection.FrameReceiveClient frame = taggedConnection.receiveC();
        String fileName;
        jobsWait.remove(frame.pedidoCliente);
        if(files.containsKey(frame.pedidoCliente)){
           fileName = "Out" + frame.pedidoCliente + "_" + files.get(frame.pedidoCliente);
        }
        else{
            fileName = "Out" + frame.pedidoCliente + ".txt";
        }

        Path caminhoAbsoluto = Paths.get(fileName).toAbsolutePath();

        if (frame.exp == 1) {

            System.out.println("\nOutput Pedido com a Tag: " + frame.pedidoCliente + "\nError: " + frame.messageException + '\n');
            try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(caminhoAbsoluto.toFile()))) {
                bos.write(frame.messageException.getBytes());
            }
        } else {

            System.out.println("\nOutput Pedido com a Tag: " + frame.pedidoCliente + "\n->" + Arrays.toString(frame.data) + '\n');
            try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(caminhoAbsoluto.toFile()))) {
                bos.write(frame.data);
            }
        }

    }

    private static void authMenu(String username) {
        StringBuilder helpMenu = new StringBuilder();

        helpMenu.append("\n");
        helpMenu.append("========================================== " + "Bem-vindo, ").append(username).append("!").append(" ==============================================\n");
        helpMenu.append("nimbouscloud.help> 'exec'                     - Execute a Job\n");
        helpMenu.append("nimbouscloud.help> 'status'                   - Displays information about the user\n");
        helpMenu.append("nimbouscloud.help> 'logout'                   - Logout the user provided in the username\n");
        helpMenu.append("============================================================================================================\n");

        System.out.println(helpMenu);
    }


    private static void processHelp() {
        StringBuilder helpMenu = new StringBuilder();

        helpMenu.append("\n");
        helpMenu.append("========================================= NIMBOUS CLOUD HELP =============================================\n");
        helpMenu.append("nimbouscloud.help> 'help'                           - Displays this message\n");
        helpMenu.append("nimbouscloud.help> 'register [username] [password]' - Register a new user with the provided parameters\n");
        helpMenu.append("nimbouscloud.help> 'login [username] [password]'    - Login the user with the provided parameters\n");
        helpMenu.append("nimbouscloud.help> 'exit'                           - Exit the application\n");
        helpMenu.append("=========================================================================================================\n");

        System.out.println(helpMenu);
    }

    private static void close(Socket socket, DataOutputStream out) throws IOException {
        out.writeInt(999);
        out.flush();

        socket.shutdownOutput();
        socket.shutdownInput();
        socket.close();
    }
}



