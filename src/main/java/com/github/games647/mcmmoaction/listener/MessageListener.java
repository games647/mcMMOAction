package com.github.games647.mcmmoaction.listener;

import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.reflect.FieldUtils;
import com.comphenix.protocol.wrappers.EnumWrappers;
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
import java.util.logging.Level;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.HoverEvent.Action;
import net.md_5.bungee.chat.ComponentSerializer;

import org.bukkit.entity.Player;

import static com.comphenix.protocol.PacketType.Play.Server.CHAT;
import static java.util.stream.Collectors.toSet;

public class MessageListener extends PacketAdapter {

    private static final String[] childrenClean = {"extra", "with", "style"};

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

        shouldRemoveHover = !Enums.getIfPresent(Action.class, "SHOW_ENTITY").isPresent();

        this.plugin = plugin;
        this.localizedMessages = ImmutableSet.copyOf(messages
                .stream()
                .map(message -> numberRemover.matcher(message).replaceAll(""))
                .collect(toSet()));
    }

    @Override
    public void onPacketSending(PacketEvent packetEvent) {
        PacketContainer packet = packetEvent.getPacket();
        if (packetEvent.isCancelled() || packet.hasMetadata(plugin.getName())) {
            return;
        }

        Player player = packetEvent.getPlayer();
        WrappedChatComponent message = packet.getChatComponents().read(0);
        ChatType chatType = readChatPosition(packet);
        if (message == null || chatType != ChatType.SYSTEM) {
            return;
        }

        String json = message.getJson();
        plugin.getLogger().info(json);
        if (shouldRemoveHover) {
            json = gson.toJson(cleanJsonFromHover(json));
        }

        BaseComponent chatComponent = ComponentSerializer.parse(json)[0];
        if (chatComponent != null && isMcMMOMessage(chatComponent.toPlainText()) && plugin.isActionBarEnabled(player)) {
            writeChatPosition(packet);

            //action bar doesn't support the new chat features
            String legacyText = pluginTagPattern.matcher(chatComponent.toLegacyText()).replaceFirst("");
            packet.getChatComponents().write(0, WrappedChatComponent.fromText(legacyText));
            plugin.playNotificationSound(player);
        }
    }

    private boolean isMcMMOMessage(CharSequence plainText) {
        //remove the numbers to match the string easier
        String cleanedMessage = numberRemover.matcher(plainText).replaceAll("");
        return localizedMessages.contains(cleanedMessage);
    }

    private ChatType readChatPosition(PacketContainer packet) {
        if (plugin.supportsChatTypeEnum()) {
            try {
                Object pos = FieldUtils.readField(packet.getChatTypes().getField(0), packet.getHandle());
                if (pos == null) {
                    //check for null types (invalid packets)
                    return null;
                }

                return EnumWrappers.getChatTypeConverter().getSpecific(pos);
            } catch (IllegalAccessException accessEx) {
                plugin.getLogger().log(Level.WARNING, "Cannot read chat position from packet", accessEx);
            }

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
        Stream.of(components)
                .filter(JsonElement::isJsonObject)
                .map(JsonElement::getAsJsonObject)
                .peek(object -> object.remove("hoverEvent"))
                //if this object has also extra or with components use them there too
                .forEach(this::cleanJsonFromHover);
    }
}
