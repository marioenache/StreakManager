package ro.marioenache.streakmanager.data;

import org.bukkit.configuration.file.FileConfiguration;
import ro.marioenache.streakmanager.StreakManager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConfigManager {

    private final StreakManager plugin;
    private Map<Integer, List<String>> streakCommands;

    public ConfigManager(StreakManager plugin) {
        this.plugin = plugin;
        this.streakCommands = new HashMap<>();
    }

    public void loadRewards() {
        streakCommands.clear();
        
        FileConfiguration config = plugin.getConfig();
        if (config.contains("rewards")) {
            for (String key : config.getConfigurationSection("rewards").getKeys(false)) {
                try {
                    int streakValue = Integer.parseInt(key);
                    List<String> commands = config.getStringList("rewards." + key);
                    streakCommands.put(streakValue, commands);
                } catch (NumberFormatException e) {
                    plugin.getLogger().warning("Invalid streak value in config: " + key);
                }
            }
        }
    }

    public Map<Integer, List<String>> getStreakCommands() {
        return streakCommands;
    }
}