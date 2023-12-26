package org.nimboscloud.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.io.*;
import java.util.Arrays;

public class Client {
    private static String username;
    private static int jobs;

    public static void main(String[] args) {
        try {
            Socket socket = new Socket("localhost", 1666);

            DataInputStream in = new DataInputStream(socket.getInputStream());
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());

            // Display help menu as soon as the client connects
            processHelp();

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
                handle_command(parts, in, out);

            }

            // Continue with the remaining code (shutdownOutput, etc.) as needed...

            socket.shutdownOutput();
            socket.shutdownInput();
            socket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void handle_command(String[] parts, DataInputStream in, DataOutputStream out){
        try{
            if(parts[0].equals("register")){
                out.writeInt(0);
                out.writeUTF(parts[1]);
                out.writeUTF(parts[2]);
                out.flush();

                boolean flag = in.readBoolean();
                if (flag) System.out.println("Register successful for user: " + parts[1]);
            }
            else if (parts[0].equals("login")){
                out.writeInt(1);
                out.writeUTF(parts[1]);
                out.writeUTF(parts[2]);
                out.flush();

                boolean flag = in.readBoolean();
                if (flag) {
                    username = parts[1];
                    System.out.println("Login successful for user: " + username);
                    processAuthenticatedMenu(in, out);
                }
                else{
                    System.out.println("Login failed. Invalid credentials or user already logged in.\n");
                }
            }
            else{
                System.out.println("Command doesnt exist!");
                return;
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
    }


    private static void processAuthenticatedMenu(DataInputStream in, DataOutputStream out) throws IOException, IOException {

        authMenu(username);
        jobs = 0;

        BufferedReader systemIn = new BufferedReader(new InputStreamReader(System.in));

        String userInput;
        label:
        while ((userInput = systemIn.readLine()) != null) {
            String[] parts = userInput.split(" ");

            switch (parts[0]) {
                case "logout" -> {
                    out.writeInt(2);
                    out.writeUTF(username);
                    out.flush();
                    int response = in.readInt();
                    if (response == 1) {
                        System.out.println("Logout successful for user: " + username);
                        for (int i = 0; i < 3; ++i) System.out.println();
                        processHelp();
                        break label;
                    } else {
                        System.out.println("Logout error!");
                    }
                }
                case "status" -> {
                    out.writeInt(4);
                    out.flush();
                    System.out.println(in.readUTF());
                }
                case "help" -> processHelp();
                case "exec" -> {

                    Thread t = new Thread(() -> {
                        try {
                            initExec(in, out, parts);
                            waitExec(in,out);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });

                    t.start();
                }
                case "view-jobs" -> {
                    // Lógica para visualizar Jobs já executados
                }
                default -> System.out.println("Comando desconhecido. Digite 'help' para obter a lista de comandos.");
            }
        }
    }

    private static void initExec(DataInputStream in, DataOutputStream out, String[] parts) throws IOException {

        out.writeInt(3);
        out.writeInt(jobs);
        out.writeInt(Integer.parseInt(parts[2]));
        out.writeUTF(parts[1]);
        out.flush();

        System.out.println(" | Pedido Com Tag: "+ jobs + " |");
        jobs = jobs +1;

    }

    private static void waitExec(DataInputStream in, DataOutputStream out){

        try {
            String full_string = in.readUTF();
            String[] parts = full_string.split("\\|");
            // Extrair os valores
            int exp = Integer.parseInt(parts[0]);
            int pedidoCliente = Integer.parseInt(parts[1]);

            if (exp == 1) {

                String errorMsg = parts[2];
                System.out.println("\nOutput Pedido com a Tag: " + pedidoCliente + "\nError: " + errorMsg + '\n');
            } else {

                String data = parts[2];
                System.out.println("\nOutput Pedido com a Tag: " + pedidoCliente + "\n->" + data + '\n');
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private static void authMenu(String username) {
        StringBuilder helpMenu = new StringBuilder();

        helpMenu.append("\n");
        helpMenu.append("========================================== " + "Bem-vindo, ").append(username).append("!").append(" ==============================================\n");
        helpMenu.append("nimbouscloud.help> 'exec'                     - Execute a Job\n");
        helpMenu.append("nimbouscloud.help> 'view-jobs'                - View the last executed jobs\n");
        helpMenu.append("nimbouscloud.help> 'logout'                   - Logout the user provided in the username\n");
        helpMenu.append("nimbouscloud.help> 'status'                   - Displays information about the user\n");
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
}



