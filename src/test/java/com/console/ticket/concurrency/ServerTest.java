package com.console.ticket.concurrency;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

class ServerTest {
    private Client client;
    private Server server;
    private List<Integer> inputDataList;
    private List<Integer> emptyList;
    private static int listSize;
    private static int threadsQuantity;

    @BeforeAll
    static void setStaticData() {
        listSize = 100;
        threadsQuantity = 5;
    }

    @BeforeEach
    void initialize() {
        inputDataList = IntStream.range(0, listSize)
                .boxed()
                .collect(Collectors.toCollection(CopyOnWriteArrayList::new));
        emptyList = new CopyOnWriteArrayList<>();


        server = new Server(emptyList);
        client = new Client(server, inputDataList, threadsQuantity);
    }

    @DisplayName("assert that server received all the data from client")
    @Test
    void checkServerListSizeIsEqualTo100() {
        client.start();

        int expectedListSize = listSize;
        int actualListSize = server.getListSize();

        assertThat(actualListSize).isEqualTo(expectedListSize);
    }
}