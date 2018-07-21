package com.github.games647.mcmmoaction.redirect;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;

public class HoverEventCleanerTest {

    private HoverEventCleaner cleaner;

    @Before
    public void setUp() throws Exception {
        this.cleaner = new HoverEventCleaner();
    }

    @Test
    public void testUnmodified() throws Exception {
        String expected = readFile("/normal.json").replace(" ", "");
        assertThat(expected, is(cleaner.cleanJson(expected)));
    }

    @Test
    public void shouldRemoveHover() throws Exception {
        String hover = readFile("/hover-event.json");
        assertThat(cleaner.cleanJson(hover), not(containsString("hoverEvent")));
    }

    private String readFile(String name) throws IOException, URISyntaxException {
        Path file = Paths.get(getClass().getResource(name).toURI());
        return Files.readAllLines(file).stream().collect(Collectors.joining());
    }
}
