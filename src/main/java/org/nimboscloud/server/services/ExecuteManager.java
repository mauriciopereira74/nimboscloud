package org.nimboscloud.server.services;

import org.nimboscloud.JobFunction.*;
import org.nimboscloud.server.Server;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ExecuteManager {

    private Server server;


    public ExecuteManager(Server server) {
        this.server = server;

    }

    public byte[] executeJobFunction(byte[] clientCode, int memoryOccupancy) throws JobFunctionException, InterruptedException {

        byte[] result = JobFunction.execute(clientCode);

            return result;
        }

}
