package org.nimboscloud.manager.services;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class QueueList implements AutoCloseable {

    private List<QueueConnection> list = new ArrayList<>();

    private ReentrantLock lockList = new ReentrantLock();

    public QueueList() {

    }
    public List<QueueConnection> getList(){
        return list;
    }
    public void addQueue(QueueConnection queue) {
        lockList.lock();
        list.add(queue);
        lockList.unlock();
    }

    public void remQueue(QueueConnection queue) {
        lockList.lock();
        list.remove(queue);
        lockList.unlock();
    }

    public void escolheQueue(Object[] pedido) {

        List<Integer> listAux = new ArrayList<>();
        boolean flag = false;
        int mem = (int) pedido[2];

        lockList.lock();

        for (QueueConnection queue : list) {
            if (mem <= queue.getMemory()) {
                flag = true;
                ReentrantLock lock = queue.getLock();
                Condition condition = queue.getCondition();
                queue.addExec(pedido);
                lock.lock();
                condition.signalAll();
                lock.unlock();
                break;
            }
            if (mem <= queue.getMaxMemory()) {
                listAux.add(queue.getMemoryOnWait());
            } else{
                listAux.add(null);
            }
        }

        if (!flag) {

            int minIndex = 0; // Assumindo que o primeiro elemento é o menor inicialmente

            for (int i = 1; i < listAux.size(); i++) {
                if (listAux.get(i) != null && listAux.get(minIndex) != null) {

                    if (listAux.get(i).compareTo(listAux.get(minIndex)) < 0) {

                        minIndex = i;
                    }
                } else if (listAux.get(i) != null && listAux.get(minIndex) == null) {

                    minIndex = i;
                }
            }
            ReentrantLock lock = list.get(minIndex).getLock();
            Condition condition = list.get(minIndex).getCondition();
            list.get(minIndex).addExec(pedido);
            lock.lock();
            condition.signalAll();
            lock.unlock();


        }

        lockList.unlock();
        
    }

    public ReentrantLock getLockList(){
        return lockList;
    }

    @Override
    public void close() throws Exception {

    }
}


