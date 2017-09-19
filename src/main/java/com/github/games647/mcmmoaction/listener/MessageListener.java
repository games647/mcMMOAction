package com.github.games647.mcmmoaction.listener;

import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.utility.MinecraftVersion;
import com.comphenix.protocol.wrappers.EnumWrappers.ChatType;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import com.github.games647.mcmmoaction.mcMMOAction;
import com.google.common.collect.ImmutableSet;

import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.chat.ComponentSerializer;

import org.bukkit.entity.Player;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import static com.comphenix.protocol.PacketType.Play.Server.CHAT;

public class MessageListener extends PacketAdapter {

    private static final byte NORMAL_CHAT_POSITION = 1;
    private static final byte ACTION_BAR_POSITION = 2;
    private static final String PLUGIN_TAG = "[mcMMO] ";

    private final mcMMOAction plugin;

    private final Pattern pluginTagPattern = Pattern.compile(PLUGIN_TAG);

    private final MinecraftVersion currentVersion = MinecraftVersion.getCurrentVersion();
    //in comparison to the ProtocolLib variant this includes the build number
    private final MinecraftVersion explorationUpdate = new MinecraftVersion(1, 11, 2);

    //compile the pattern just once - remove the comma so it also detect numbers like (10,000)
    private final Pattern numberRemover = Pattern.compile("[,0-9]");

    //create a immutable set in order to be thread-safe and faster than normal sets
    private ImmutableSet<String> localizedMessages;

    public MessageListener(mcMMOAction plugin, Set<String> messages) {
        super(params().plugin(plugin).optionAsync().types(CHAT));

        this.plugin = plugin;
        this.localizedMessages = ImmutableSet.copyOf(messages
                .stream()
                .map(message -> numberRemover.matcher(message).replaceAll(""))
                .collect(Collectors.toSet()));
    }

    @Override
    public void onPacketSending(PacketEvent packetEvent) {
        if (packetEvent.isCancelled()) {
            return;
        }

        PacketContainer packet = packetEvent.getPacket();

        byte chatType = readChatPosition(packet);
        Player player = packetEvent.getPlayer();
        if (chatType == NORMAL_CHAT_POSITION
                && !plugin.getDisabledActionBar().contains(player.getUniqueId())
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
            if (chatComponent != null && isMcmmoMessage(chatComponent.toPlainText())) {
                writeChatPosition(packet, ACTION_BAR_POSITION);

                //action bar doesn't support the new chat features
                String legacyText = pluginTagPattern.matcher(chatComponent.toLegacyText()).replaceFirst("");
                packet.getChatComponents().write(0, WrappedChatComponent.fromText(legacyText));
                plugin.playNotificationSound(player);
            }
        }
    }

    private boolean isMcmmoMessage(String plainText) {
        //remove the numbers to match the string easier
        String cleanedMessage = numberRemover.matcher(plainText).replaceAll("");
        return localizedMessages.contains(cleanedMessage);
    }

    private byte readChatPosition(PacketContainer packet) {
        if (supportsChatTypeEnum()) {
            return packet.getChatTypes().read(0).getId();
        }

        return packet.getBytes().read(0);
    }

    private void writeChatPosition(PacketContainer packet, byte positionId) {
        if (supportsChatTypeEnum()) {
            packet.getChatTypes().writeSafely(0, ChatType.values()[positionId]);
        } else {
            packet.getBytes().writeSafely(0, positionId);
        }
    }

    private static JSONObject cleanJsonFromHover(String json) {
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
        components.stream()
                .filter(JSONObject.class::isInstance)
                .forEach(component -> {
                    JSONObject jsonComponent = (JSONObject) component;

                    //due this issue: https://github.com/SpigotMC/BungeeCord/issues/1300 - there is a class missing
                    jsonComponent.remove("hoverEvent");

                    //if this object has also extra or with components use them there too
                    cleanJsonFromHover(jsonComponent);
                });
    }

    private boolean supportsChatTypeEnum() {
        return currentVersion.compareTo(explorationUpdate) > 0;
    }
}
