package com.github.games647.mcmmoaction.redirect;

import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.EnumWrappers.ChatType;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import com.github.games647.mcmmoaction.MessageListener;
import com.github.games647.mcmmoaction.mcMMOAction;
import com.github.games647.mcmmoaction.refresh.RefreshManager;
import com.google.common.base.Enums;
import com.google.common.collect.ImmutableSet;

import java.util.Collection;
import java.util.regex.Pattern;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.HoverEvent.Action;
import net.md_5.bungee.chat.ComponentSerializer;

import org.bukkit.entity.Player;

import static com.comphenix.protocol.PacketType.Play.Server.CHAT;
import static java.util.stream.Collectors.toSet;

public class RedirectListener extends MessageListener {

    private final Pattern pluginTagPattern = Pattern.compile(Pattern.quote("[mcMMO] "));

    //compile the pattern just once - remove the comma so it also detect numbers like (10,000)
    private final Pattern numberRemover = Pattern.compile("[,0-9]");

    //create a immutable set in order to be thread-safe and faster than normal sets
    private final ImmutableSet<String> localizedMessages;

    private StubHoverCleaner cleaner = new StubHoverCleaner();
    private final RefreshManager refreshManager;

    public RedirectListener(mcMMOAction plugin, RefreshManager refreshManager, Collection<String> messages) {
        super(plugin, params().types(CHAT).listenerPriority(ListenerPriority.LOW));

        if (!Enums.getIfPresent(Action.class, "SHOW_ENTITY").isPresent()) {
            cleaner = new HoverEventCleaner();
        }

        this.refreshManager = refreshManager;
        this.localizedMessages = ImmutableSet.copyOf(messages
                .stream()
                .map(message -> numberRemover.matcher(message).replaceAll(""))
                .collect(toSet()));
    }

    @Override
    public void onPacketSending(PacketEvent packetEvent) {
        PacketContainer packet = packetEvent.getPacket();
        Player player = packetEvent.getPlayer();
        if (packetEvent.isCancelled() || isOurPacket(packet) || !isRedirectEnabled(player)) {
            return;
        }

        WrappedChatComponent message = packet.getChatComponents().read(0);
        ChatType chatType = readChatPosition(packet);
        if (message == null) {
            return;
        }

        String json = cleaner.cleanJson(message.getJson());

        BaseComponent chatComponent = ComponentSerializer.parse(json)[0];
        if (chatComponent != null && isMcMMOMessage(chatComponent.toPlainText())) {
            //action bar doesn't support the new chat features
            String legacyText = pluginTagPattern.matcher(chatComponent.toLegacyText()).replaceFirst("");
            refreshManager.sendActionMessage(player, legacyText);

            packetEvent.setCancelled(true);
        }
    }

    protected boolean isMcMMOMessage(CharSequence plainText) {
        //remove the numbers to match the string easier
        String cleanedMessage = numberRemover.matcher(plainText).replaceAll("");
        return localizedMessages.contains(cleanedMessage);
    }

    public boolean isRedirectEnabled(Player player) {
        return !plugin.getActionBarDisabled().contains(player.getUniqueId())
                && player.hasPermission(plugin.getName().toLowerCase() + ".display");
    }
}
