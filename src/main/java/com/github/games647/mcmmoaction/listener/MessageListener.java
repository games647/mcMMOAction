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

import java.util.Collection;
import java.util.logging.Level;
import java.util.regex.Pattern;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.chat.ComponentSerializer;

import org.bukkit.entity.Player;

import static com.comphenix.protocol.PacketType.Play.Server.CHAT;
import static java.util.stream.Collectors.toSet;

public class MessageListener extends PacketAdapter {

    private final mcMMOAction plugin;
    private final Pattern pluginTagPattern = Pattern.compile(Pattern.quote("[mcMMO] "));

    //compile the pattern just once - remove the comma so it also detect numbers like (10,000)
    private final Pattern numberRemover = Pattern.compile("[,0-9]");

    //create a immutable set in order to be thread-safe and faster than normal sets
    private final ImmutableSet<String> localizedMessages;

    private HoverEventCleaner cleaner;

    public MessageListener(mcMMOAction plugin, Collection<String> messages) {
        super(params().plugin(plugin).types(CHAT));

        if (!Enums.getIfPresent(HoverEvent.Action.class, "SHOW_ENTITY").isPresent()) {
            cleaner = new HoverEventCleaner();
        }

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
        if (cleaner != null) {
            json = cleaner.cleanJson(json);
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
                Object pos = FieldUtils.readField(packet.getChatTypes().getField(0), packet.getHandle(), true);
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
}
