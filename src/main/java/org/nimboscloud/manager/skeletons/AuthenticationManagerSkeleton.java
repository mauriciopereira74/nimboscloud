package org.nimboscloud.manager.skeletons;

import org.nimboscloud.manager.services.AuthenticationManager;
import java.io.*;
import java.io.PrintWriter;

public class AuthenticationManagerSkeleton {

    private AuthenticationManager authManager;

    public AuthenticationManagerSkeleton(AuthenticationManager authManager) {
        this.authManager = authManager;
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

    public void processLogoutExit(String username) {

        boolean logoutSuccess = authManager.logoutUser(username);

    }


    public boolean checkStatus(String username){
        return authManager.isUserOnline(username);
    }
}
