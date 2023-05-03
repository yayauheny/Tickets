package com.console.ticket.concurrency;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

public class Server {

    private static final int MS_RANGE_FROM = 1;
    private static final int MS_RANGE_TO = 10;
    private final List<Integer> receivedData;
    private final ReentrantLock receivedDataLock = new ReentrantLock(true);

    public Server(List<Integer> receivedData) {
        this.receivedData = receivedData;
    }

    private void addReceivedElement(Request req) {
        try {
            Integer elementFromRequest = req.value();
            long delayInMs = ThreadLocalRandom.current().nextInt(MS_RANGE_FROM, MS_RANGE_TO);

            TimeUnit.MILLISECONDS.sleep(delayInMs);
            receivedData.add(elementFromRequest);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private Response getReceivedDataListSize() {
        int currentSize;
        currentSize = receivedData.size();
        return new Response(currentSize);
    }

    public Response handleRequestAndReturnDataSize(Request req) {
        try {
            receivedDataLock.lock();

            addReceivedElement(req);
            return getReceivedDataListSize();
        } finally {
            receivedDataLock.unlock();
        }
    }

    public int getListSize(){
        try {
            receivedDataLock.lock();

            return receivedData.size();
        } finally {
            receivedDataLock.unlock();
        }
    }
}
