package org.nimboscloud.server.skeletons;

import org.nimboscloud.server.services.AuthenticationManager;

import java.io.PrintWriter;

public class AuthenticationManagerSkeleton {

    private AuthenticationManager authManager;

    public AuthenticationManagerSkeleton(AuthenticationManager authManager) {
        this.authManager = authManager;
    }

    public void processCommand(String[] parts, PrintWriter out) {
        String command = parts[0];

        switch (command) {
            case "register":
                processRegister(parts, out);
                break;
            case "login":
                processLogin(parts, out);
                break;
            case "logout":
                processLogout(parts, out);
                break;
            case "status":
                processStatus(parts, out);
                break;
            default:
                out.println("Unknown command. Type 'help' for a list of commands.");
        }
    }

    private void processRegister(String[] parts, PrintWriter out) {
        if (parts.length == 3) {
            String username = parts[1];
            String password = parts[2];

            boolean registrationSuccess = authManager.registerUser(username, password);

            if (registrationSuccess) {
                out.println("0");
            } else {
                out.println("User " + username + " already exists. Registration failed.");
            }
        } else {
            out.println("Invalid format for 'register' command. Usage: register [username] [password]");
        }
    }

    private void processLogin(String[] parts, PrintWriter out) {
        if (parts.length == 3) {
            String username = parts[1];
            String password = parts[2];
            boolean loginSuccess = authManager.loginUser(username, password);

            if (loginSuccess) {
                out.println("1");
            } else {
                out.println("1.1");
            }
        } else {
            out.println("Invalid format for 'login' command. Usage: login [username] [password]");
        }
    }

    private void processLogout(String[] parts, PrintWriter out) {
        if (parts.length == 2) {
            String username = parts[1];
            boolean logoutSuccess = authManager.logoutUser(username);

            if (logoutSuccess) {
                out.println("2");
            } else {
                out.println("Logout failed. User " + username + " is not logged in.");
            }
        } else {
            out.println("Invalid format for 'logout' command. Usage: logout [username]");
        }
    }

    private void processStatus(String[] parts, PrintWriter out) {
        if (parts.length == 2) {
            String username = parts[1];
            String userStatus = authManager.isUserOnline(username) ? "Online" : "Offline";
            out.println("User " + username + " is " + userStatus);
        } else {
            out.println("Invalid format for 'status' command. Usage: status [username]");
        }
    }
}
