package org.nimboscloud.server.services;

import org.trotiletre.common.INotificationManager;
import org.trotiletre.models.utils.Location;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class NotificationManager implements INotificationManager {
    private final Map<String, Set<LocationData>> userMap = new HashMap<>();
    private final Lock lock = new ReentrantLock();

    public boolean register(String user) {
        lock.lock();
        try {
            if (this.userMap.containsKey(user))
                return false;
            this.userMap.put(user, new HashSet<>());
            return true;
        } finally {
            lock.unlock();
        }
    }

    public boolean isRegistered(String user) {
        lock.lock();
        try {
            return this.userMap.containsKey(user);
        } finally {
            lock.unlock();
        }
    }

    public boolean addLocation(String user, Location location, int radius) {
        lock.lock();
        try {
            Set<LocationData> locationDataSet = this.userMap.get(user);
            if (locationDataSet == null)
                return false;

            locationDataSet.add(new LocationData(location, radius));
            return true;
        } finally {
            lock.unlock();
        }
    }

    public boolean remove(String user) {
        lock.lock();
        try {
            if (!this.userMap.containsKey(user))
                return false;

            this.userMap.remove(user);
            return true;
        } finally {
            lock.unlock();
        }
    }

    public Set<String> getUserSet() {
        lock.lock();
        try {
            return new HashSet<>(this.userMap.keySet());
        } finally {
            lock.unlock();
        }
    }

    public Set<LocationData> getUserLocationSet(String user) {
        lock.lock();
        try {
            Set<LocationData> userLocationDataSet = this.userMap.get(user);
            if (userLocationDataSet == null)
                return new HashSet<>();

            return new HashSet<>(userLocationDataSet);
        } finally {
            lock.unlock();
        }
    }

    public record LocationData(Location location, int radius) {
    }

}
