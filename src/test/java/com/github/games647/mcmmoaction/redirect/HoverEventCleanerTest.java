package com.github.games647.mcmmoaction.redirect;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.google.common.truth.Truth.assertThat;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class HoverEventCleanerTest {

    private HoverEventCleaner cleaner;

    @BeforeEach
    public void setUp() throws Exception {
        this.cleaner = new HoverEventCleaner();
    }

    @Test
    public void testUnmodified() throws Exception {
        String expected = readFile("/normal.json").replace(" ", "");
        assertEquals(expected, cleaner.cleanJson(expected));
    }

    @Test
    public void shouldRemoveHover() throws Exception {
        String hover = readFile("/hover-event.json");
        assertThat(cleaner.cleanJson(hover)).doesNotContain("hoverEvent");
    }

    private String readFile(String name) throws IOException, URISyntaxException {
        Path file = Paths.get(getClass().getResource(name).toURI());
        return Files.readAllLines(file).stream().collect(Collectors.joining());
    }
}
