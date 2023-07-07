package org.flicko.discordauth;

import github.scarsz.discordsrv.DiscordSRV;
import github.scarsz.discordsrv.dependencies.jda.api.entities.Guild;
import github.scarsz.discordsrv.dependencies.jda.api.events.ReadyEvent;
import github.scarsz.discordsrv.dependencies.jda.api.events.interaction.ButtonClickEvent;
import github.scarsz.discordsrv.dependencies.jda.api.events.interaction.SlashCommandEvent;
import github.scarsz.discordsrv.dependencies.jda.api.events.message.MessageReceivedEvent;
import github.scarsz.discordsrv.dependencies.jda.api.hooks.ListenerAdapter;
import github.scarsz.discordsrv.dependencies.jda.api.interactions.components.Button;
import org.jetbrains.annotations.NotNull;


import java.util.ArrayList;
import java.util.Objects;
import java.util.UUID;
import java.util.logging.Logger;

public class JDAListener extends ListenerAdapter {

    private final Plugin plugin;

    public JDAListener(Plugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onButtonClick(ButtonClickEvent event) {
        if (event.getComponentId().equals("yes")) {
            UUID uuid = DiscordSRV.getPlugin().getAccountLinkManager().getUuid(Objects.requireNonNull(event.getUser()).getId());
            Button button = event.getButton();
            if (button != null) {
                String ip = event.getButton().getLabel();
                plugin.addTrusted(uuid, ip);
                event.editButton(Button.secondary("yes", ip).asDisabled()).queue();
            } else {
                plugin.getLogger().warning("Button is null");
            }
        }
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event){
        if (event.getAuthor().isBot()) return;

        if (event.getMessage().getContentRaw().startsWith("!")) {
            String[] command = event.getMessage().getContentRaw().substring(1).split(" ");
            if (command[0].equals("trusted")) {
                if (command.length != 1) {
                    event.getChannel().sendMessage("Usage: !trusted").queue();
                    return;
                }

                ArrayList<String> ips = plugin.trusted.get(DiscordSRV.getPlugin().getAccountLinkManager().getUuid(event.getAuthor().getId()));

                if (ips == null) {
                    event.getChannel().sendMessage("no trusted ips").queue();
                    return;
                }

                StringBuilder sb = new StringBuilder();
                for (String ip : ips) {
                    sb.append(ip).append("\n");
                }

                event.getAuthor().openPrivateChannel().queue((channel)->{
                    channel.sendMessage("trusted ips:\n"+sb).queue();
                });
            }
            if (command[0].equals("untrust")) {
                if (command.length != 2) {
                    event.getChannel().sendMessage("Usage: !untrust <ip>").queue();
                    return;
                }

                String ip = command[1];
                UUID uuid = DiscordSRV.getPlugin().getAccountLinkManager().getUuid(event.getAuthor().getId());

                plugin.removeTrusted(uuid, ip);
                event.getAuthor().openPrivateChannel().queue((channel)->{
                    channel.sendMessage("untrusted: "+ip).queue();
                });

            }


        }
    }

    @Override
    public void onReady(@NotNull ReadyEvent event) {
        Logger logger = plugin.getLogger();
        logger.info("\n\n\n");
        logger.info("[ DiscordAuth is connected to discord ]");
        logger.info("\n\n\n");
    }

}