package org.nimboscloud.server.services;

import org.nimboscloud.JobFunction.*;
import org.nimboscloud.server.Server;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ExecuteManager {

    private Server server;

    private Lock lock = new ReentrantLock();
    private Condition memoryAccess = lock.newCondition();


    public ExecuteManager(Server server) {
        this.server = server;
    }

    public byte[] executeJobFunction(byte[] clientCode, int memoryOccupancy) throws JobFunctionException, InterruptedException {


        while(memoryOccupancy > server.getMemory()){
            memoryAccess.await();
        }

        lock.lock();
        try {
            server.removeMemory(memoryOccupancy);
            System.out.println(server.getMemory());
        }
        finally {
            lock.unlock();
        }

        byte[] result = JobFunction.execute(clientCode);

        lock.lock();
        try {
            server.addMemory(memoryOccupancy);
            System.out.println(server.getMemory());
        }
        finally {
            lock.unlock();
        }
            return result;
        }

}
