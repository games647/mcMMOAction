package com.github.games647.mcmmoaction.listener;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.stream.IntStream;

public class HoverEventCleaner {

    private static final String[] childrenClean = {"extra", "with"};

    private final Gson gson = new Gson();

    public String cleanJson(String json) {
        return gson.toJson(cleanJsonFromHover(json));
    }

    private JsonElement cleanJsonFromHover(String json) {
        JsonElement jsonComponent = gson.fromJson(json, JsonElement.class);
        if (jsonComponent.isJsonObject()) {
            return cleanJsonFromHover((JsonObject) jsonComponent);
        }

        return jsonComponent;
    }

    private JsonObject cleanJsonFromHover(JsonObject jsonComponent) {
        for (String child : childrenClean) {
            if (jsonComponent.has(child)) {
                removeHoverEvent(jsonComponent.getAsJsonArray(child));
            }
        }

        return jsonComponent;
    }

    private void removeHoverEvent(JsonArray components) {
        // due this issue: https://github.com/SpigotMC/BungeeCord/issues/1300 -
        // there is a class missing for the SHOW_ENTITY event
        IntStream.range(0, components.size())
                .mapToObj(components::get)
                .filter(JsonElement::isJsonObject)
                .map(JsonElement::getAsJsonObject)
                .peek(object -> object.remove("hoverEvent"))
                //if this object has also extra or with components use them there too
                .forEach(this::cleanJsonFromHover);
    }
}
