package ro.marioenache.streakmanager.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import ro.marioenache.streakmanager.StreakManager;
import ro.marioenache.streakmanager.models.PlayerData;

import java.util.UUID;

public class PlayerLoginListener implements Listener {

    private final StreakManager plugin;

    public PlayerLoginListener(StreakManager plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UUID playerUUID = player.getUniqueId();
        String playerName = player.getName();
        
        // Update player name if changed and initialize data if needed
        plugin.getStreakDataManager().updatePlayerName(playerUUID, playerName);
        
        // Create default player data if none exists
        if (plugin.getStreakDataManager().getPlayerStreak(playerUUID) == 0) {
            PlayerData data = new PlayerData(playerUUID);
            data.setName(playerName);
            plugin.getStreakDataManager().setPlayerData(playerUUID, data);
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        if (plugin.getConfig().getBoolean("settings.save-on-quit", true)) {
            Player player = event.getPlayer();
            UUID playerUUID = player.getUniqueId();
            plugin.getStreakDataManager().savePlayerData();
        }
    }
}