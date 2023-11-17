package org.nimboscloud.client;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Client {
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

                if ("exit".equalsIgnoreCase(userInput.trim())) {
                    System.out.println("A sair da aplicação...");
                    break;
                }

                if ("help".equalsIgnoreCase(userInput.trim())) {
                    processHelp();
                    continue;
                }

                out.println(userInput);
                out.flush();

                String response = in.readLine();
                System.out.println("Server response: " + response);
            }

            // Continue with the remaining code (shutdownOutput, etc.) as needed...

            socket.shutdownOutput();
            String response = in.readLine();
            if (response != null) {
                System.out.println("Average: " + response);
            } else {
                System.out.println("Server did not provide an average.");
            }

            socket.shutdownInput();
            socket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void processHelp() {

        StringBuilder helpMenu = new StringBuilder();

        helpMenu.append("nimbouscloud.help> 'help' displays this message\n");

        helpMenu.append("nimbouscloud.help> 'register [username] [password]' register a new user with the provided parameters\n");
        helpMenu.append("nimbouscloud.help> 'login [username] [password]' login the user with the provided parameters\n");
        helpMenu.append("nimbouscloud.help> 'logout [username]' logout the user provied in the username\n");
        helpMenu.append("nimbouscloud.help> 'status' [username]' displays information about the user\n");

        helpMenu.append("nimbouscloud.help> 'exit' exit de application\n");

        System.out.println(helpMenu);
    }
}



