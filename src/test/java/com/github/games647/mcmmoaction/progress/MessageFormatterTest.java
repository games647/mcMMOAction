package com.github.games647.mcmmoaction.progress;

import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.core.AllOf.allOf;
import static org.hamcrest.core.Is.is;

import static org.junit.Assert.assertThat;

public class MessageFormatterTest {

    private MessageFormatter<Integer, String> formatter;

    @Before
    public void setUp() throws Exception {
        formatter = new MessageFormatter<>();
        formatter.addReplacer("var", (f, s) -> s);
    }

    @Test
    public void testUnmodified() {
        assertThat(formatter.replace(42, "123", ""), is(""));
        assertThat(formatter.replace(1, "123", "abc"), is("abc"));
    }

    @Test
    public void testReplacing() {
        String replaced = formatter.replace(42, "replaced", "{var} hello");
        assertThat(replaced, allOf(containsString("replaced"), containsString("hello")));
    }
}
