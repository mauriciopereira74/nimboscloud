package org.nimboscloud.client;

import java.io.BufferedReader;
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
                }
                else if ((response.equals("0"))){
                    System.out.println("Register successful for user: " + parts[1]);
                }
                else {
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

    private static void processHelp() {
        StringBuilder helpMenu = new StringBuilder();

        helpMenu.append("\n");
        helpMenu.append("===================================== NIMBOUS CLOUD HELP =====================================\n");
        helpMenu.append("nimbouscloud.help> 'help'                           - Displays this message\n");
        helpMenu.append("nimbouscloud.help> 'register [username] [password]' - Register a new user with the provided parameters\n");
        helpMenu.append("nimbouscloud.help> 'login [username] [password]'    - Login the user with the provided parameters\n");
        helpMenu.append("nimbouscloud.help> 'logout                          - Logout the user provided in the username\n");
        helpMenu.append("nimbouscloud.help> 'status                          - Displays information about the user\n");
        helpMenu.append("nimbouscloud.help> 'exit'                           - Exit the application\n");
        helpMenu.append("=================================================================================================\n");

        System.out.println(helpMenu);
    }

}



