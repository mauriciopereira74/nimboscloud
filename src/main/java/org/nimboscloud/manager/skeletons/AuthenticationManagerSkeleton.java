package org.nimboscloud.manager.skeletons;

import org.nimboscloud.manager.services.AuthenticationManager;
import java.io.*;
import java.io.PrintWriter;

public class AuthenticationManagerSkeleton {

    private AuthenticationManager authManager;

    public AuthenticationManagerSkeleton(AuthenticationManager authManager) {
        this.authManager = authManager;
    }

    public void processCommand(String[] parts, DataOutputStream out) {
        try{
            String command = parts[0];
            String user = parts[1];
            String pass = parts[2];

            switch (command) {
                case "register":
                    processRegister(user, pass, out);
                case "login":
                    processLogin(user, pass, out);
                case "logout":
                    processLogout(user, out);
                default:
                    out.writeUTF("Unknown command. Type 'help' for a list of commands.");
                    out.flush();
            }
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    public void processRegister(String username, String password, DataOutputStream out) {
        try{

            boolean registrationSuccess = authManager.registerUser(username, password);

            if (registrationSuccess) {
                out.writeBoolean(true);
                out.flush();
            } else {
                out.writeBoolean(false);
                out.flush();
            }

        }catch (IOException e){
            e.printStackTrace();
        }
    }

    public void processLogin(String username, String password, DataOutputStream out) {
        try{

            boolean loginSuccess = authManager.loginUser(username, password);

            if (loginSuccess) {
                out.writeBoolean(true);
                out.flush();
            } else {
                out.writeBoolean(false);
                out.flush();
            }

        } catch (IOException e){
            e.printStackTrace();
        }
    }

    public void processLogout(String username, DataOutputStream out) {
        try{
            boolean logoutSuccess = authManager.logoutUser(username);
            if (logoutSuccess) {
                out.writeBoolean(true);
                out.flush();
            } else {
                out.writeBoolean(false);
                out.flush();
            }

        }catch (IOException e){
                    e.printStackTrace();
        }
    }


    public boolean checkStatus(String username){
        return authManager.isUserOnline(username);
    }
}
