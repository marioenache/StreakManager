package ro.marioenache.streakmanager;

import org.bukkit.plugin.java.JavaPlugin;
import ro.marioenache.streakmanager.commands.AddStreakCommand;
import ro.marioenache.streakmanager.commands.StreakAdminCommand;
import ro.marioenache.streakmanager.data.ConfigManager;
import ro.marioenache.streakmanager.data.StreakDataManager;
import ro.marioenache.streakmanager.listeners.PlayerLoginListener;
import ro.marioenache.streakmanager.placeholders.StreakPlaceholder;

import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

public class StreakManager extends JavaPlugin {

    private StreakDataManager dataManager;
    private ConfigManager configManager;
    private boolean placeholderAPIEnabled = false;

    @Override
    public void onEnable() {
        // Save default config
        saveDefaultConfig();
        
        // Initialize managers
        configManager = new ConfigManager(this);
        dataManager = new StreakDataManager(this);
        
        // Load data
        configManager.loadRewards();
        dataManager.loadPlayerData();
        
        // Register commands
        getCommand("addstreak").setExecutor(new AddStreakCommand(this));
        getCommand("streakadmin").setExecutor(new StreakAdminCommand(this));
        
        // Register events
        getServer().getPluginManager().registerEvents(new PlayerLoginListener(this), this);
        
        // Setup auto-save task
        int saveInterval = getConfig().getInt("settings.save-interval", 5) * 1200; // Convert minutes to ticks
        new BukkitRunnable() {
            @Override
            public void run() {
                dataManager.savePlayerData();
                getLogger().info("Auto-saving player streak data...");
            }
        }.runTaskTimerAsynchronously(this, saveInterval, saveInterval);
        
        // Check for PlaceholderAPI
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new StreakPlaceholder(this).register();
            placeholderAPIEnabled = true;
            getLogger().info("PlaceholderAPI found! Registering placeholders.");
        } else {
            getLogger().warning("PlaceholderAPI not found! Placeholders will not work.");
        }
        
        getLogger().info("StreakManager has been enabled!");
    }

    @Override
    public void onDisable() {
        // Save player data on shutdown
        if (dataManager != null) {
            dataManager.savePlayerData();
        }
        getLogger().info("StreakManager has been disabled!");
    }
    
    public StreakDataManager getStreakDataManager() {
        return dataManager;
    }
    
    public ConfigManager getConfigManager() {
        return configManager;
    }
    
    public boolean isPlaceholderAPIEnabled() {
        return placeholderAPIEnabled;
    }
}