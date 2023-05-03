package com.console.ticket.concurrency;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;


class ClientTest {

    private Client client;
    private static int listSize;
    private static int threadsQuantity;

    @BeforeAll
    static void setStaticData() {
        listSize = 100;
        threadsQuantity = 5;
    }

    @BeforeEach
    void initialize() {
        List<Integer> inputDataList = new Random().ints(listSize).boxed()
                .collect(Collectors.toCollection(CopyOnWriteArrayList::new));
        List<Integer> emptyList = new CopyOnWriteArrayList<>();


        Server server = new Server(emptyList);
        client = new Client(server, inputDataList, threadsQuantity);
    }

    @DisplayName("assert that client send all the data to server")
    @Test
    void checkClientListSizeIsEmpty() {
        client.start();

        int expectedListSize = 0;
        int actualListSize = client.getSendDataSize();

        assertThat(actualListSize).isEqualTo(expectedListSize);
    }
}