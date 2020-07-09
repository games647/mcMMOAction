package com.github.games647.mcmmoaction;

import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.EnumWrappers.ChatType;

public class MessageListener extends PacketAdapter {

    protected final mcMMOAction plugin;

    public MessageListener(mcMMOAction plugin, AdapterParameteters params) {
        super(params.plugin(plugin));

        this.plugin = plugin;
    }

    protected boolean isOurPacket(PacketContainer container) {
        return container.getMeta(plugin.getName()).isPresent();
    }

    protected ChatType readChatPosition(PacketContainer packet) {
        if (plugin.supportsChatTypeEnum()) {
            return packet.getChatTypes().read(0);
        }

        byte positionId = packet.getBytes().read(0);
        return ChatType.values()[positionId];
    }
}
