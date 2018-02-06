package com.github.games647.mcmmoaction.progress;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MessageFormatter<F, S> {

    private static final char VARIABLE_START_SYMBOL = '{';
    private static final char VARIABLE_END_SYMBOL = '}';

    private final Map<String, BiFunction<F, S, String>> replacers = new HashMap<>();
    private Pattern variablePattern;

    public void addReplacer(String variable, BiFunction<F, S, String> replacer) {
        replacers.put(VARIABLE_START_SYMBOL + variable + VARIABLE_END_SYMBOL, replacer);
        buildPattern();
    }

    public String replace(F first, S second, String template) {
        //StringBuilder is only compatible with Java 9+
        StringBuffer buffer = new StringBuffer();

        Matcher matcher = variablePattern.matcher(template);
        while (matcher.find()) {
            matcher.appendReplacement(buffer, replacers.get(matcher.group()).apply(first, second));
        }

        matcher.appendTail(buffer);
        return buffer.toString();
    }

    private void buildPattern() {
        StringBuilder builder = new StringBuilder();
        Iterator<String> iterator = replacers.keySet().iterator();
        while (iterator.hasNext()) {
            String var = iterator.next();
            builder.append('(').append(Pattern.quote(var)).append(')');
            if (iterator.hasNext()) {
                builder.append('|');
            }
        }

        variablePattern = Pattern.compile(builder.toString());
    }
}
