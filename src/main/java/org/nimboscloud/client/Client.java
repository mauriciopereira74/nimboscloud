package org.nimboscloud.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Client {
    private static String username;

    public static void main(String[] args) {
        try {
            Socket socket = new Socket("localhost", 1666);

            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream());

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
                else if (parts[0].equals("logout") || parts[0].equals("status")){
                    if (username != null) {
                        userInput = userInput + " " + username;
                    }
                }

                out.println(userInput);
                out.flush();

                String response = in.readLine();
                // login success
                if(response.equals("1")){
                    username = parts[1];
                    System.out.println("Login successful for user: " + username);
                    processAuthenticatedMenu(systemIn, in, out);
                } else if ((response.equals("0"))){
                    System.out.println("Register successful for user: " + parts[1]);
                } else if ((response.equals("1.1"))) {
                    System.out.println("Login failed. Invalid credentials or user already logged in.\n");
                } else {
                    System.out.println(response);
                }
            }

            // Continue with the remaining code (shutdownOutput, etc.) as needed...

            socket.shutdownOutput();
            String response = in.readLine();
            if (response != null) {
                System.out.println("Server: " + response);
            } else {
                System.out.println("Nothing from server");
            }

            socket.shutdownInput();
            socket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void processAuthenticatedMenu(BufferedReader systemIn, BufferedReader in, PrintWriter out) throws IOException, IOException {

        authMenu(username);

        String userInput;
        label:
        while ((userInput = systemIn.readLine()) != null) {
            String[] parts = userInput.split(" ");

            switch (parts[0]) {
                case "logout" -> {
                    userInput = userInput + " " + username;
                    out.println(userInput);
                    out.flush();
                    String response = in.readLine();
                    if (response.equals("2")) {
                        System.out.println("Logout successful for user: " + username);
                        for (int i = 0; i < 5; ++i) System.out.println();
                        processHelp();
                        break label;
                    } else {
                        System.out.println(response);
                    }
                }
                case "status" -> {
                    userInput = userInput + " " + username;
                    out.println(userInput);
                    out.flush();
                    System.out.println(in.readLine());
                }
                case "help" -> processHelp();
                case "execute-job" -> {
                }
                // Lógica para executar um Job

                case "view-jobs" -> {
                }
                // Lógica para visualizar Jobs já executados

                default -> System.out.println("Comando desconhecido. Digite 'help' para obter a lista de comandos.");
            }
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



