package org.nimboscloud.manager.services;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class QueueList implements AutoCloseable{

    private List<QueueConnection> list = new ArrayList<>();

    private ReentrantLock lockList = new ReentrantLock();

    public QueueList(){

    }

    public void addQueue(QueueConnection queue){
        lockList.lock();
        list.add(queue);
        lockList.unlock();
    }
    public void remQueue(QueueConnection queue){
        lockList.lock();
        list.remove(queue);
        lockList.unlock();
    }

    public void escolheQueue(Object[] pedido){
        lockList.lock();
        this.list.get(0).add(pedido);
        ReentrantLock lock = this.list.get(0).getLock();
        Condition condition = this.list.get(0).getCondition();
        lock.lock();
        condition.signalAll();
        lock.unlock();
        lockList.unlock();
    }


    @Override
    public void close() throws Exception {

    }
}
