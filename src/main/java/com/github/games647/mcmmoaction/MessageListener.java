package com.github.games647.mcmmoaction;

import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.reflect.FieldUtils;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.comphenix.protocol.wrappers.EnumWrappers.ChatType;

import java.util.logging.Level;

public class MessageListener extends PacketAdapter {

    protected final mcMMOAction plugin;

    public MessageListener(mcMMOAction plugin, AdapterParameteters params) {
        super(params.plugin(plugin));

        this.plugin = plugin;
    }

    protected boolean isOurPacket(PacketContainer container) {
        return container.hasMetadata(plugin.getName());
    }

    protected ChatType readChatPosition(PacketContainer packet) {
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
}
