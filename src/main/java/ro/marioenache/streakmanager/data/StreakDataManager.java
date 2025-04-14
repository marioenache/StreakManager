package ro.marioenache.streakmanager.data;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import ro.marioenache.streakmanager.StreakManager;
import ro.marioenache.streakmanager.models.PlayerData;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class StreakDataManager {

    private final StreakManager plugin;
    private File streakFile;
    private FileConfiguration streakData;
    private ConcurrentHashMap<UUID, PlayerData> playerDataMap;
    private final SimpleDateFormat dateFormat;
    private boolean isSaving;

    public StreakDataManager(StreakManager plugin) {
        this.plugin = plugin;
        this.playerDataMap = new ConcurrentHashMap<>();
        this.dateFormat = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
        this.isSaving = false;
        setupStreakData();
    }

    private void setupStreakData() {
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }

        streakFile = new File(plugin.getDataFolder(), "data.yml");
        if (!streakFile.exists()) {
            try {
                streakFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Could not create data.yml file!");
                e.printStackTrace();
            }
        }
        
        // Create backup directory
        File backupDir = new File(plugin.getDataFolder(), "backups");
        if (!backupDir.exists()) {
            backupDir.mkdirs();
        }
        
        streakData = YamlConfiguration.loadConfiguration(streakFile);
    }

    private void createBackup() {
        if (!plugin.getConfig().getBoolean("settings.backup-before-save", true)) {
            return;
        }

        File backupDir = new File(plugin.getDataFolder(), "backups");
        String backupFileName = "data-" + dateFormat.format(new Date()) + ".yml";
        File backupFile = new File(backupDir, backupFileName);

        try {
            Files.copy(streakFile.toPath(), backupFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            
            // Clean old backups
            File[] backups = backupDir.listFiles((dir, name) -> name.startsWith("data-") && name.endsWith(".yml"));
            if (backups != null) {
                int maxBackups = plugin.getConfig().getInt("settings.max-backups", 5);
                if (backups.length > maxBackups) {
                    // Sort by last modified
                    java.util.Arrays.sort(backups, (f1, f2) -> Long.compare(f2.lastModified(), f1.lastModified()));
                    
                    // Delete oldest backups
                    for (int i = maxBackups; i < backups.length; i++) {
                        backups[i].delete();
                    }
                }
            }
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to create backup: " + e.getMessage());
        }
    }

    public synchronized void loadPlayerData() {
        if (streakData.contains("players")) {
            for (String uuidString : streakData.getConfigurationSection("players").getKeys(false)) {
                try {
                    UUID uuid = UUID.fromString(uuidString);
                    int streak = streakData.getInt("players." + uuidString + ".streak");
                    String name = streakData.getString("players." + uuidString + ".name", "");
                    
                    PlayerData playerData = new PlayerData(uuid);
                    playerData.setStreak(streak);
                    playerData.setName(name);
                    
                    playerDataMap.put(uuid, playerData);
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("Invalid UUID in data file: " + uuidString);
                }
            }
        }
    }

    public synchronized void savePlayerData() {
        if (isSaving) {
            plugin.getLogger().warning("Attempted to save while another save operation is in progress!");
            return;
        }

        isSaving = true;

        // Run asynchronously to prevent server lag
        new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    createBackup();

                    for (Map.Entry<UUID, PlayerData> entry : playerDataMap.entrySet()) {
                        UUID uuid = entry.getKey();
                        PlayerData data = entry.getValue();
                        
                        streakData.set("players." + uuid.toString() + ".streak", data.getStreak());
                        streakData.set("players." + uuid.toString() + ".name", data.getName());
                    }
                    
                    try {
                        streakData.save(streakFile);
                    } catch (IOException e) {
                        plugin.getLogger().severe("Could not save player data!");
                        e.printStackTrace();
                    }
                } finally {
                    isSaving = false;
                }
            }
        }.runTaskAsynchronously(plugin);
    }

    public PlayerData getPlayerData(UUID uuid) {
        return playerDataMap.getOrDefault(uuid, new PlayerData(uuid));
    }

    public void setPlayerData(UUID uuid, PlayerData data) {
        playerDataMap.put(uuid, data);
        if (plugin.getConfig().getBoolean("settings.save-on-quit", true)) {
            savePlayerData();
        }
    }

    public int getPlayerStreak(UUID uuid) {
        return getPlayerData(uuid).getStreak();
    }

    public void setPlayerStreak(UUID uuid, int value) {
        PlayerData data = getPlayerData(uuid);
        data.setStreak(value);
        playerDataMap.put(uuid, data);
        
        // Check for rewards
        checkForRewards(uuid, value);
    }

    public void updatePlayerName(UUID uuid, String name) {
        PlayerData data = getPlayerData(uuid);
        String oldName = data.getName();
        
        if (!name.equals(oldName)) {
            plugin.getLogger().info("Player " + (oldName.isEmpty() ? uuid.toString() : oldName) + 
                                   " is now known as " + name + ", updating records.");
            data.setName(name);
            playerDataMap.put(uuid, data);
        }
    }

    public void addPlayerStreak(UUID uuid, int amount) {
        PlayerData data = getPlayerData(uuid);
        int currentStreak = data.getStreak();
        int newStreak = currentStreak + amount;
        
        data.setStreak(newStreak);
        playerDataMap.put(uuid, data);
        
        // Check for rewards
        checkForRewards(uuid, newStreak);
    }

    private void checkForRewards(UUID uuid, int streakValue) {
        Map<Integer, List<String>> streakCommands = plugin.getConfigManager().getStreakCommands();
        
        if (streakCommands.containsKey(streakValue)) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null && player.isOnline()) {
                List<String> commands = streakCommands.get(streakValue);
                
                // Execute commands on the main thread
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        for (String cmd : commands) {
                            String parsedCommand = cmd.replace("%player%", player.getName());
                            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), parsedCommand);
                        }
                    }
                }.runTask(plugin);
                
                String message = plugin.getConfig().getString("messages.streak.milestone", "&aYou've reached a streak of %amount%!")
                    .replace("%amount%", String.valueOf(streakValue));
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
            }
        }
    }
}