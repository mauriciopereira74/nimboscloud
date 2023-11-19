package org.nimboscloud.models;

import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;

import java.util.HashMap;
import java.util.Map;

/**
 * A class that represents a user in the application.
 * <p>
 * This class stores information about the user's username, password hash, balance,
 * distance traveled, and number of rides taken.
 */
public class User {

    private String username; // 'username' acts as the identification of a user.
    private String passwordHash; // Hashed password with the algoritm Argon2.
    private Map<String, String> jobs = new HashMap<>();
    private boolean isAdmin;


    public User(String username, String hashedPassword) {
        this.username = username;
        this.passwordHash = hashedPassword;
    }


    public User(String username) {
        this.username = username;
    }

    /**
     * Sets the password for the user.
     * <p>
     * The password is encoded using the Argon2 algorithm before being stored.
     *
     * @param password The plaintext password to be set.
     */
    public void setPassword(String password) {
        Argon2PasswordEncoder encoder = new Argon2PasswordEncoder(32, 64, 1, 15 * 1024, 2);
        this.passwordHash = encoder.encode(password);
    }

    /**
     * Checks if the provided password is correct or not.
     *
     * @param password The hashed password to test.
     * @return {@code true} if they match, {@code false} otherwise.
     */
    public boolean matchPassword(String password) {
        Argon2PasswordEncoder encoder = new Argon2PasswordEncoder(32, 64, 1, 15 * 1024, 2);
        return encoder.matches(password, passwordHash);
    }

    /**
     * Returns the username of the user.
     *
     * @return The username of the user.
     */
    public String getUsername() {
        return username;
    }

    /**
     * Sets the username of the user.
     *
     * @param username The new username for the user.
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Returns the password hash of the user.
     *
     * @return The password hash of the user.
     */
    public String getPasswordHash() {
        return passwordHash;
    }

    /**
     * Sets the password hash of the user.
     *
     * @param passwordHash The new password hash for the user.
     */
    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    /**
     * Returns the jobs list of the user.
     *
     * @return The jobs list of the user.
     */
    public Map<String, String> getJobs() {return jobs;}

    /**
     * Sets the jobs list of the user.
     *
     * @param jobs The new jobs list of the user.
     */
    public void setJobs(Map<String, String> jobs) {this.jobs = jobs;}

    public boolean isAdmin() {
        return isAdmin;
    }

    public void setAdmin(boolean admin) {
        isAdmin = admin;
    }
}

