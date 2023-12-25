package org.nimboscloud.server.workers;

import org.nimboscloud.server.Server;
import org.nimboscloud.server.skeletons.AuthenticationManagerSkeleton;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.nimboscloud.JobFunction.JobFunction;
import org.nimboscloud.JobFunction.JobFunctionException;
import org.nimboscloud.server.Server;

public class QueueWorker implements Runnable {
    private Server server;


    public QueueWorker (Server server){ this.server = server; }

    public void run(){
        try {
            handle_thread();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (JobFunctionException e) {
            throw new RuntimeException(e);
        }
    }

    public void handle_thread() throws InterruptedException, JobFunctionException {
        while (true) {
            ThreadInfo t = server.getFromQueue();

            while (true) {
                if (t.getMemory() < server.getMemory()) {
                    t.getThread().start();
                    break;
                }
            }

        }
    }

}
