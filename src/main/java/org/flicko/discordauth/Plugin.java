package org.flicko.discordauth;

import github.scarsz.discordsrv.DiscordSRV;
import github.scarsz.discordsrv.dependencies.jda.api.entities.User;
import github.scarsz.discordsrv.dependencies.jda.api.interactions.components.Button;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public final class Plugin extends JavaPlugin implements Listener{

    HashMap<UUID, ArrayList<String>> trusted = new HashMap<UUID, ArrayList<String >>();
    //HashMap<UUID, String> to_be_trusted = new HashMap<UUID, String>();

    private final DiscordSRVListener discordsrvListener = new DiscordSRVListener(this);

    @Override
    public void onEnable() {
        // Plugin startup logic
        getServer().getPluginManager().registerEvents(this, this);
        DiscordSRV.api.subscribe(discordsrvListener);

    }

    //command
    public void addTrusted(UUID uuid, String ip) {
        if (trusted.get(uuid) == null) {
            ArrayList<String> ips = new ArrayList<String>();
            ips.add(ip);
            trusted.put(uuid, ips);
        } else {
            trusted.get(uuid).add(ip);
        }
    }

    public void removeTrusted(UUID uuid, String ip){
        if (trusted.get(uuid) == null) {
            return;
        } else {
            trusted.get(uuid).remove(ip);
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event){
        // event.setKickMessage("nop");
        String d_id = DiscordSRV.getPlugin().getAccountLinkManager().getDiscordId(event.getPlayer().getUniqueId());
        UUID player_id = event.getPlayer().getUniqueId();
        Player player = event.getPlayer();

        if ( d_id != null) {

            //get player ip

            InetSocketAddress addr = player.getAddress();
            if (addr == null) {
                player.sendMessage("addr is null");
                return;
            }
            String ip = addr.getAddress().toString().replace("/", "");

            if (trusted.get(player_id) != null && trusted.get(player_id).contains(ip)) {
                player.sendMessage("nice");
                return;
            } else {
                User discord = DiscordSRV.getPlugin().getJda().getUserById(d_id);
                if (discord != null){
                    discord.openPrivateChannel().queue((channel) -> {
                        channel.sendMessage("click button to set ip as trusted").setActionRow(Button.success("yes", ip)).queue();
                    });
                } else {
                    getLogger().warning("discord is null");
                }

                player.kickPlayer("You are not trusted, please check your discord for your ip");
            }

        } else {
            player.sendMessage("You are not linked to a discord account :: " + d_id);
        }
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        DiscordSRV.api.unsubscribe(discordsrvListener);
    }
}
