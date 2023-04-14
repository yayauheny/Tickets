package com.console.ticket.concurrency;

import java.util.List;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class Client implements Callable<Request> {

    private final ReentrantLock sendDataLock = new ReentrantLock(true);
    private final AtomicInteger accumulator = new AtomicInteger(0);
    private final List<Integer> sendData;
    private final Server server;
    private final ExecutorService clientExecutor;
    private final Queue<Future<Request>> requestsQueue;
    private final Queue<Future<Response>> responsesQueue;

    public Client(Server server, List<Integer> sendData, int threadsQuantity) {
        this.sendData = sendData;
        this.requestsQueue = new LinkedBlockingQueue<>(getSendDataSize());
        this.responsesQueue = new LinkedBlockingQueue<>(getSendDataSize());
        this.clientExecutor = Executors.newFixedThreadPool(threadsQuantity);
        this.server = server;
    }

    public int getAccumulator() {
        return accumulator.get();
    }

    public void start() {
        putFutureRequestsToRequestsQueue(requestsQueue, clientExecutor);
        executeRequests(requestsQueue, clientExecutor, server);
        executeResponses(responsesQueue, clientExecutor);

        clientExecutor.shutdown();
        try {
            clientExecutor.awaitTermination(1, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void executeRequests(Queue<Future<Request>> requests, ExecutorService executor, Server server) {
        Stream.generate(requests::poll)
                .takeWhile(Objects::nonNull)
                .forEach(futureReq -> {
                    getExecutedReqAndSaveResponseOrPutBack(requests, futureReq, executor);
                });
    }

    private void executeResponses(Queue<Future<Response>> responses, ExecutorService executor) {
        Stream.generate(responses::poll)
                .takeWhile(Objects::nonNull)
                .forEach(futureResp -> {
                    getExecutedRespAndCalculateAccumulatorOrPutBack(responses, futureResp, executor);
                });
    }

    private void putFutureRequestsToRequestsQueue(Queue<Future<Request>> requests, ExecutorService executor) {
        IntStream.range(0, getSendDataSize())
                .forEach(index -> {
                    Future<Request> requestFromClient = executor.submit(this);
                    requests.add(requestFromClient);
                });
    }

    private void getExecutedRespAndCalculateAccumulatorOrPutBack(Queue<Future<Response>> responses, Future<Response> futureResp, ExecutorService executor) {
        try {
            if (futureResp.isDone()) {
                Response resp = futureResp.get();
                executor.submit(() -> calculateAccumulator(resp));
            } else {
                responses.add(futureResp);
            }
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    private void getExecutedReqAndSaveResponseOrPutBack(Queue<Future<Request>> requests, Future<Request> futureReq, ExecutorService executor) {
        try {
            if (futureReq.isDone()) {
                Request request = futureReq.get();

                Future<Response> responseFromServer = executor.submit(() ->
                        server.handleRequestAndReturnDataSize(request));

                responsesQueue.add(responseFromServer);
            } else {
                requests.add(futureReq);
            }
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    private Integer remove() {
        try {
            sendDataLock.lock();

            int deletableIndex = ThreadLocalRandom.current().nextInt(sendData.size());
            return sendData.remove(deletableIndex);
        } finally {
            sendDataLock.unlock();
        }
    }

    private void calculateAccumulator(Response response) {
        int receivedListSize = response.listSize();
        accumulator.addAndGet(receivedListSize);
    }

    @Override
    public Request call() throws Exception {
        Integer deletedElement = remove();

        return new Request(deletedElement);
    }

    public int getSendDataSize() {
        try {
            sendDataLock.lock();
            return sendData.size();
        } finally {
            sendDataLock.unlock();
        }
    }
}
