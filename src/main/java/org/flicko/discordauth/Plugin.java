package org.flicko.discordauth;

import github.scarsz.discordsrv.DiscordSRV;
import github.scarsz.discordsrv.dependencies.jda.api.entities.User;
import github.scarsz.discordsrv.dependencies.jda.api.interactions.components.Button;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.net.InetAddress;
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
        if (trusted.get(uuid) != null) {
            trusted.get(uuid).remove(ip);
        }
    }

    @EventHandler
    public void onPlayerJoin(AsyncPlayerPreLoginEvent event){
        // event.setKickMessage("nop");
        UUID player_id = event.getUniqueId();
        String d_id = DiscordSRV.getPlugin().getAccountLinkManager().getDiscordId(player_id);

        if ( d_id != null) {

            //get player ip

            InetAddress ip_address = event.getAddress();
            String ip = ip_address.getHostAddress();

            if (trusted.get(player_id) != null && trusted.get(player_id).contains(ip)) {
                getLogger().log(java.util.logging.Level.INFO, player_id + " trusted");
            } else {
                User discord = DiscordSRV.getPlugin().getJda().getUserById(d_id);
                if (discord != null){
                    discord.openPrivateChannel().queue((channel) -> {
                        channel.sendMessage("click button to set ip as trusted").setActionRow(Button.success("yes", ip)).queue();
                    });
                } else {
                    getLogger().warning("discord is null");
                }

                event.setKickMessage("You are not trusted, check your discord DMs");
                event.setLoginResult(AsyncPlayerPreLoginEvent.Result.KICK_OTHER);
            }

        }
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        DiscordSRV.api.unsubscribe(discordsrvListener);
    }
}
