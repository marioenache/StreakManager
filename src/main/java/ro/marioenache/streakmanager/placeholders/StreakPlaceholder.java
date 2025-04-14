package ro.marioenache.streakmanager.placeholders;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;

import ro.marioenache.streakmanager.StreakManager;

public class StreakPlaceholder extends PlaceholderExpansion {

    private final StreakManager plugin;

    public StreakPlaceholder(StreakManager plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getIdentifier() {
        return "streak";
    }

    @Override
    public String getAuthor() {
        return plugin.getDescription().getAuthors().toString();
    }

    @Override
    public String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true; // This is required or placeholders will stop working after a reload
    }

    @Override
    public String onPlaceholderRequest(Player player, String identifier) {
        if (player == null) {
            return "";
        }

        // %streak_count% - returns the player's current streak
        if (identifier.equals("count")) {
            return String.valueOf(plugin.getStreakDataManager().getPlayerStreak(player.getUniqueId()));
        }

        return null;
    }
}