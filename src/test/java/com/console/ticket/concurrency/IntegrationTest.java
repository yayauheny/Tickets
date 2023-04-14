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

public class IntegrationTest {
    private Client client;
    private Server server;
    private List<Integer> inputDataList;
    private List<Integer> emptyList;
    private static int listSize;
    private static int threadsQuantity;

    @BeforeAll
    static void setStaticData() {
        listSize = 100;
        threadsQuantity = 10;
    }

    @BeforeEach
    void initialize() {
        inputDataList = new Random().ints(listSize).boxed()
                .collect(Collectors.toCollection(CopyOnWriteArrayList::new));
        emptyList = new CopyOnWriteArrayList<>();


        server = new Server(emptyList);
        client = new Client(server, inputDataList, threadsQuantity);
    }

    @DisplayName("assert that result of server responses accumulated in client correctly")
    @Test
    void checkAccumulatorIsCorrect() {
        client.start();

        int expectedAccumulatorValue = (1 + listSize) * (listSize / 2);
        int actualAccumulatorValue = client.getAccumulator();

        assertThat(actualAccumulatorValue).isEqualTo(expectedAccumulatorValue);
    }
}
