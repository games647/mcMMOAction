package com.github.games647.mcmmoaction.progress;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MessageFormatterTest {

    private MessageFormatter<Integer, String> formatter;

    @BeforeEach
    public void setUp() throws Exception {
        formatter = new MessageFormatter<>();
        formatter.addReplacer("var", (f, s) -> s);
    }

    @Test
    public void testUnmodified() {
        assertEquals("", formatter.replace(42, "123", ""));
        assertEquals("", formatter.replace(42, "123", ""));
        assertEquals("abc", formatter.replace(1, "123", "abc"));
    }

    @Test
    public void testReplacing() {
        String replaced = formatter.replace(42, "replaced", "{var} hello");
        assertEquals("replaced hello", replaced);
    }
}
