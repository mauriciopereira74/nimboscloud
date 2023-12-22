package org.nimboscloud.server.skeletons;

import org.nimboscloud.server.services.AuthenticationManager;

import java.io.PrintWriter;

public class AuthenticationManagerSkeleton {

    private AuthenticationManager authManager;

    public AuthenticationManagerSkeleton(AuthenticationManager authManager) {
        this.authManager = authManager;
    }

    public int processCommand(String[] parts, PrintWriter out) {
        String command = parts[0];

        switch (command) {
            case "register":
                return processRegister(parts, out);
            case "login":
                return processLogin(parts, out);
            case "logout":
                return processLogout(parts, out);
            case "status":
                return processStatus(parts, out);
            default:
                out.println("Unknown command. Type 'help' for a list of commands.");
        }
        return 0;
    }

    private int processRegister(String[] parts, PrintWriter out) {
        if (parts.length == 3) {
            String username = parts[1];
            String password = parts[2];

            boolean registrationSuccess = authManager.registerUser(username, password);

            if (registrationSuccess) {
                out.println("0");
                return 1;
            } else {
                out.println("User " + username + " already exists. Registration failed.");
            }
        } else {
            out.println("Invalid format for 'register' command. Usage: register [username] [password]");
        }
        return 0;
    }

    private int processLogin(String[] parts, PrintWriter out) {
        if (parts.length == 3) {
            String username = parts[1];
            String password = parts[2];
            boolean loginSuccess = authManager.loginUser(username, password);

            if (loginSuccess) {
                out.println("1");
                return 2;
            } else {
                out.println("1.1");
            }
        } else {
            out.println("Invalid format for 'login' command. Usage: login [username] [password]");

        }
        return 0;
    }

    private int processLogout(String[] parts, PrintWriter out) {
        if (parts.length == 2) {
            String username = parts[1];
            boolean logoutSuccess = authManager.logoutUser(username);

            if (logoutSuccess) {
                out.println("2");
                return 3;
            } else {
                out.println("Logout failed. User " + username + " is not logged in.");
            }
        } else {
            out.println("Invalid format for 'logout' command. Usage: logout [username]");
        }
        return 0;
    }

    private int processStatus(String[] parts, PrintWriter out) {
        if (parts.length == 2) {
            String username = parts[1];
            String userStatus = authManager.isUserOnline(username) ? "Online" : "Offline";
            out.println("User " + username + " is " + userStatus);
            return 4;
        } else {
            out.println("Invalid format for 'status' command. Usage: status [username]");
        }
        return 0;
    }

    public boolean checkStatus(String username){
        return authManager.isUserOnline(username);
    }
}
