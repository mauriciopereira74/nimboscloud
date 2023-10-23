package org.nimboscloud.server.services;

import org.trotiletre.common.IAuthenticationManager;
import org.trotiletre.models.User;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

public class AuthenticationManager implements IAuthenticationManager {

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

    public boolean registerUser(String username, String passwordHash) {

        try {

            authLock.lock();

            User newUser = new User(username, passwordHash);
            if (!accounts.containsKey(username)) {
                accounts.put(username, newUser);
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

    public boolean changeNotificationStatus(String username, boolean state) throws IOException, InterruptedException {

        try {

            authLock.lock();

            if (accounts.containsKey(username) && onlineAccounts.get(username)) {

                User user = accounts.get(username);
                user.setNotificationsAllowed(state);
                return true;
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

}
