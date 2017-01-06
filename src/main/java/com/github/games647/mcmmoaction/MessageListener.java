package com.github.games647.mcmmoaction;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.WrappedChatComponent;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.chat.ComponentSerializer;

import org.bukkit.entity.Player;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

public class MessageListener extends PacketAdapter {

    private static final byte NORMAL_CHAT_POSTION = 1;
    private static final byte ACTIONBAR_POSITION = 2;

    private static final String PLUGIN_TAG = "[mcMMO] ";

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
        Player player = packetEvent.getPlayer();
        if (chatPosition == NORMAL_CHAT_POSTION && !plugin.getDisabledActionBar().contains(player.getUniqueId())
                && player.hasPermission(plugin.getName().toLowerCase() + ".display")) {
            WrappedChatComponent message = packet.getChatComponents().read(0);
            if (message == null) {
                return;
            }

            String json = message.getJson();
            String cleanedJson = JSONValue.toJSONString(cleanJsonFromHover(json));
            if (cleanedJson == null) {
                return;
            }

            BaseComponent chatComponent = ComponentSerializer.parse(cleanedJson)[0];
            if (chatComponent != null && plugin.isMcmmoMessage(chatComponent.toPlainText())) {
                packet.getBytes().write(0, ACTIONBAR_POSITION);
                //action bar doesn't support the new chat features
                String legacyText = chatComponent.toLegacyText().replace(PLUGIN_TAG, "");
                packet.getChatComponents().write(0, WrappedChatComponent.fromText(legacyText));
                plugin.playNotificationSound(packetEvent.getPlayer());
            }
        }
    }

    public static JSONObject cleanJsonFromHover(String json) {
        Object parseComponent = JSONValue.parse(json);
        if (parseComponent instanceof JSONObject) {
            JSONObject jsonComponent = (JSONObject) parseComponent;
            return cleanJsonFromHover(jsonComponent);
        }

        return null;
    }

    private static JSONObject cleanJsonFromHover(JSONObject jsonComponent) {
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

    private static void removeHoverEvent(JSONArray components) {
        //due this issue: https://github.com/SpigotMC/BungeeCord/issues/1300 - there is a class missing
//if this object has also extra or with components use them there too
        components.stream().filter(component -> component instanceof JSONObject).forEach(component -> {
            JSONObject jsonComponent = (JSONObject) component;

            //due this issue: https://github.com/SpigotMC/BungeeCord/issues/1300 - there is a class missing
            jsonComponent.remove("hoverEvent");

            //if this object has also extra or with components use them there too
            cleanJsonFromHover(jsonComponent);
        });
    }
}
