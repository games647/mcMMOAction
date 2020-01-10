package com.github.games647.mcmmoaction;

import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TimeoutManagerTest {

    private TimeoutManager timeoutManager;

    @BeforeEach
    public void setUp() throws Exception {
        timeoutManager = new TimeoutManager(Duration.ofSeconds(2));
    }

    @Test
    public void testWaiting() throws Exception {
        UUID uuid = UUID.randomUUID();

        assertTrue(timeoutManager.isAllowed(uuid));
        TimeUnit.SECONDS.sleep(1);
        assertFalse(timeoutManager.isAllowed(uuid));

        TimeUnit.SECONDS.sleep(1);
        assertTrue(timeoutManager.isAllowed(uuid));
    }

    @Test
    public void testMultipleUsers() throws Exception {
        UUID first = UUID.randomUUID();
        UUID second = UUID.randomUUID();

        assertTrue(timeoutManager.isAllowed(first));
        TimeUnit.SECONDS.sleep(1);
        assertFalse(timeoutManager.isAllowed(first));
        assertTrue(timeoutManager.isAllowed(second));
    }
}
