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

    public byte[] executeJobFunction(byte[] clientCode) throws JobFunctionException, InterruptedException {

        /*


            while(clientCode[0] >= server.getMemory())
                sleep

            server.memory.lock();

            server.removeMemory(clientCode[0]);

            server.memory.unlock();

            byte[] result = JobFunction.execute(clientCode);

            server.memory.lock();
            server.addMemory(clientCode[0]);
            server.memory.unlock();


            signal.all()

            return result;
         */

        while(clientCode[0] > server.getMemory()){
            memoryAccess.await();
        }

        lock.lock();
        try {
            server.removeMemory(clientCode[0]);
            System.out.println(server.getMemory());
        }
        finally {
            lock.unlock();
        }

        byte[] result = JobFunction.execute(clientCode);

        lock.lock();
        try {
            server.addMemory(clientCode[0]);
            System.out.println(server.getMemory());
        }
        finally {
            lock.unlock();
        }
            return result;
        }

}
