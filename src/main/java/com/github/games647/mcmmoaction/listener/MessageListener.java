package com.github.games647.mcmmoaction.listener;

import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.EnumWrappers.ChatType;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import com.github.games647.mcmmoaction.mcMMOAction;
import com.google.common.base.Enums;
import com.google.common.collect.ImmutableSet;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.Collection;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.chat.ComponentSerializer;

import org.bukkit.entity.Player;

import static com.comphenix.protocol.PacketType.Play.Server.CHAT;

public class MessageListener extends PacketAdapter {

    private final mcMMOAction plugin;
    private final Pattern pluginTagPattern = Pattern.compile(Pattern.quote("[mcMMO] "));
    private final Gson gson = new Gson();

    private final boolean shouldRemoveHover;

    //compile the pattern just once - remove the comma so it also detect numbers like (10,000)
    private final Pattern numberRemover = Pattern.compile("[,0-9]");

    //create a immutable set in order to be thread-safe and faster than normal sets
    private final ImmutableSet<String> localizedMessages;

    public MessageListener(mcMMOAction plugin, Collection<String> messages) {
        super(params().plugin(plugin).types(CHAT));

        shouldRemoveHover = !Enums.getIfPresent(HoverEvent.Action.class, "SHOW_ENTITY").isPresent();

        this.plugin = plugin;
        this.localizedMessages = ImmutableSet.copyOf(messages
                .stream()
                .map(message -> numberRemover.matcher(message).replaceAll(""))
                .collect(Collectors.toSet()));
    }

    @Override
    public void onPacketSending(PacketEvent packetEvent) {
        PacketContainer packet = packetEvent.getPacket();
        if (packetEvent.isCancelled() || packet.hasMetadata(plugin.getName())) {
            return;
        }

        ChatType chatType = readChatPosition(packet);
        Player player = packetEvent.getPlayer();
        if (chatType != ChatType.SYSTEM
                || plugin.getActionBarDisabled().contains(player.getUniqueId())
                || !player.hasPermission(plugin.getName().toLowerCase() + ".display")) {
            return;
        }

        WrappedChatComponent message = packet.getChatComponents().read(0);
        if (message == null) {
            return;
        }

        String json = message.getJson();
        if (shouldRemoveHover) {
            json = gson.toJson(cleanJsonFromHover(json));
        }

        if (json == null) {
            return;
        }

        BaseComponent chatComponent = ComponentSerializer.parse(json)[0];
        if (chatComponent != null && isMcMMOMessage(chatComponent.toPlainText())) {
            writeChatPosition(packet);

            //action bar doesn't support the new chat features
            String legacyText = pluginTagPattern.matcher(chatComponent.toLegacyText()).replaceFirst("");
            packet.getChatComponents().write(0, WrappedChatComponent.fromText(legacyText));
            plugin.playNotificationSound(player);
        }
    }

    private boolean isMcMMOMessage(String plainText) {
        //remove the numbers to match the string easier
        String cleanedMessage = numberRemover.matcher(plainText).replaceAll("");
        return localizedMessages.contains(cleanedMessage);
    }

    private ChatType readChatPosition(PacketContainer packet) {
        if (plugin.supportsChatTypeEnum()) {
            return packet.getChatTypes().read(0);
        }

        byte positionId = packet.getBytes().read(0);
        return ChatType.values()[positionId];
    }

    private void writeChatPosition(PacketContainer packet) {
        if (plugin.supportsChatTypeEnum()) {
            packet.getChatTypes().writeSafely(0, ChatType.GAME_INFO);
        } else {
            packet.getBytes().writeSafely(0, ChatType.GAME_INFO.getId());
        }
    }

    private JsonElement cleanJsonFromHover(String json) {
        JsonElement jsonComponent = gson.fromJson(json, JsonElement.class);
        if (jsonComponent.isJsonObject()) {
            return cleanJsonFromHover((JsonObject) jsonComponent);
        }

        return jsonComponent;
    }

    private JsonObject cleanJsonFromHover(JsonObject jsonComponent) {
        if (jsonComponent.has("extra")) {
            removeHoverEvent(jsonComponent.getAsJsonArray("extra"));
        }

        if (jsonComponent.has("with")) {
            removeHoverEvent(jsonComponent.getAsJsonArray("with"));
        }

        return jsonComponent;
    }

    private void removeHoverEvent(JsonArray components) {
        //due this issue: https://github.com/SpigotMC/BungeeCord/issues/1300 - there is a class missing
        components.forEach(jsonElement -> {
            if (jsonElement.isJsonObject()) {
                JsonObject jsonObject = jsonElement.getAsJsonObject();
                jsonObject.remove("hoverEvent");

                //if this object has also extra or with components use them there too
                cleanJsonFromHover(jsonObject);
            }
        });
    }
}
