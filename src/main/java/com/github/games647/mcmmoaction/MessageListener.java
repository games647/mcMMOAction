package com.github.games647.mcmmoaction;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.WrappedChatComponent;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.chat.ComponentSerializer;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

public class MessageListener extends PacketAdapter {

    private static final byte NORMAL_CHAT_POSTION = 1;
    private static final byte ACTIONBAR_POSITION = 2;

    private final mcMMOAction plugin;

    public MessageListener(mcMMOAction plugin) {
        super(params().plugin(plugin).optionAsync().types(PacketType.Play.Server.CHAT));

        this.plugin = plugin;
    }

    @Override
    public void onPacketSending(PacketEvent packetEvent) {
        if (packetEvent.isCancelled()) {
            return;
        }

        PacketContainer packet = packetEvent.getPacket();
        byte chatPosition = packet.getBytes().read(0);
        if (chatPosition == NORMAL_CHAT_POSTION) {
            WrappedChatComponent message = packet.getChatComponents().read(0);
            String json = message.getJson();
            String cleanedJson = JSONValue.toJSONString(cleanJsonFromHover(json));
            if (cleanedJson == null) {
                return;
            }

            BaseComponent chatComponent = ComponentSerializer.parse(cleanedJson)[0];
            if (chatComponent != null && plugin.isMcmmoMessage(chatComponent.toPlainText())) {
                packet.getBytes().write(0, ACTIONBAR_POSITION);
                //action bar doesn't support the new chat features
                packet.getChatComponents().write(0, WrappedChatComponent.fromText(chatComponent.toLegacyText()));
            }
        }
    }

    private JSONObject cleanJsonFromHover(String json) {
        Object parseComponent = JSONValue.parse(json);
        if (parseComponent instanceof JSONObject) {
            JSONObject jsonComponent = (JSONObject) parseComponent;
            return cleanJsonFromHover(jsonComponent);
        }

        return null;
    }

    private JSONObject cleanJsonFromHover(JSONObject jsonComponent) {
        JSONArray withComponents = (JSONArray) jsonComponent.get("with");
        JSONArray extraComponents = (JSONArray) jsonComponent.get("extra");
        if (withComponents != null) {
            removeHoverEvent(withComponents);
        }

        if (extraComponents != null) {
            removeHoverEvent(extraComponents);
        }

        return jsonComponent;
    }

    private void removeHoverEvent(JSONArray components) {
        for (Object component : components) {
            if (component instanceof JSONObject) {
                JSONObject jsonComponent = (JSONObject) component;
                //if this object has also extra or with components use them there too
                cleanJsonFromHover(jsonComponent);

                //due this issue: https://github.com/SpigotMC/BungeeCord/issues/1300 - there is a class missing
                jsonComponent.remove("hoverEvent");
            }
        }
    }
}
