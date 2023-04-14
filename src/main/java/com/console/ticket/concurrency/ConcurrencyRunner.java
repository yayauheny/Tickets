package com.console.ticket.concurrency;

import java.util.List;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

public class ConcurrencyRunner {

    private static final int threadsQuantity = 3;
    private static final int listSize = 100;

    public static void main(String[] args) {
        List<Integer> dataList = new Random().ints(listSize).boxed()
                .collect(Collectors.toCollection(CopyOnWriteArrayList::new));
        List<Integer> emptyList = new CopyOnWriteArrayList<>();

        Server server = new Server(emptyList);
        Client client = new Client(server, dataList, threadsQuantity);

        client.start();
        System.out.println(client.getAccumulator());
    }
}
