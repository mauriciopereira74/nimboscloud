package org.nimboscloud.server.services;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

import org.nimboscloud.models.User;


public class AuthenticationManager {

    private Map<String, Boolean> onlineAccounts = new HashMap<>();
    private Map<String, User> accounts = new HashMap<>();

    private ReentrantLock authLock = new ReentrantLock();

    public AuthenticationManager() {
    }

    public boolean isUserOnline(String username) {

        try {

            authLock.lock();
            return onlineAccounts.getOrDefault(username, false);

        } finally {
            authLock.unlock();
        }
    }

    public boolean registerUser(String username, String password) {

        try {

            authLock.lock();
            User user = new User(username);
            user.setPassword(password);
            if (!accounts.containsKey(username)) {
                accounts.put(username, user);
                return true;
            }

            return false;

        } finally {
            authLock.unlock();
        }
    }

    public boolean loginUser(String username, String password) {

        try {

            authLock.lock();

            if (onlineAccounts.containsKey(username)) {
                if (onlineAccounts.get(username)) return false;
            }

            User user = accounts.get(username);
            if (user != null && user.matchPassword(password)) {
                onlineAccounts.put(username, true);
                return true;
            } else return false;

        } finally {
            authLock.unlock();
        }
    }

    public boolean logoutUser(String username) {

        try {

            authLock.lock();

            if (onlineAccounts.containsKey(username)) {

                if (onlineAccounts.get(username)) {
                    onlineAccounts.put(username, false);
                    return true;
                }
            }
            return false;

        } finally {
            authLock.unlock();
        }
    }

    public User getUser(String username) {

        try {

            authLock.lock();
            return accounts.get(username);

        } finally {
            authLock.unlock();
        }
    }

    public void createAdminUser(String username, String password) {
        try {
            authLock.lock();
            User adminUser = new User(username);
            adminUser.setPassword(password);
            adminUser.setAdmin(true);
            accounts.put(username, adminUser);
        } finally {
            authLock.unlock();
        }
    }

}
