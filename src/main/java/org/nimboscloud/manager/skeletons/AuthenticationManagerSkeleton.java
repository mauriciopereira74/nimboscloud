package org.nimboscloud.manager.skeletons;

import org.nimboscloud.manager.services.AuthenticationManager;
import java.io.*;
import java.io.PrintWriter;

public class AuthenticationManagerSkeleton {

    private AuthenticationManager authManager;

    public AuthenticationManagerSkeleton(AuthenticationManager authManager) {
        this.authManager = authManager;
    }

    public boolean processCommand(String[] parts, DataOutputStream out) {
        try{
            String command = parts[0];
            String user = parts[1];
            String pass = parts[2];

            switch (command) {
                case "register":
                    return processRegister(user, pass, out);
                case "login":
                    return processLogin(user, pass, out);
                case "logout":
                    return processLogout(user, out);
                default:
                    out.writeUTF("Unknown command. Type 'help' for a list of commands.");
                    out.flush();
            }
        } catch (IOException e){
            e.printStackTrace();
        }
        return false;
    }

    public boolean processRegister(String username, String password, DataOutputStream out) {
        try{

            boolean registrationSuccess = authManager.registerUser(username, password);

            if (registrationSuccess) {
                out.writeBoolean(true);
                out.flush();
                return true;
            } else {
                out.writeBoolean(false);
                out.flush();
            }

        }catch (IOException e){
            e.printStackTrace();
        }
        return false;
    }

    public boolean processLogin(String username, String password, DataOutputStream out) {
        try{

            boolean loginSuccess = authManager.loginUser(username, password);

            if (loginSuccess) {
                out.writeBoolean(true);
                out.flush();
                return true;
            } else {
                out.writeBoolean(false);
                out.flush();
            }

        } catch (IOException e){
            e.printStackTrace();
        }
        return false;
    }

    public boolean processLogout(String username, DataOutputStream out) {
        try{

            boolean logoutSuccess = authManager.logoutUser(username);

            if (logoutSuccess) {
                out.writeBoolean(true);
                out.flush();
                return true;
            } else {
                out.writeBoolean(false);
                out.flush();
            }

        }catch (IOException e){
                    e.printStackTrace();
        }
        return false;
    }


    public boolean checkStatus(String username){
        return authManager.isUserOnline(username);
    }
}
