package ro.marioenache.streakmanager.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import ro.marioenache.streakmanager.StreakManager;

public class StreakAdminCommand implements CommandExecutor {

    private final StreakManager plugin;

    public StreakAdminCommand(StreakManager plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!sender.hasPermission("streakmanager.admin")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to use this command!");
            return true;
        }
        
        if (args.length < 1) {
            sender.sendMessage(ChatColor.RED + "Usage: /streakadmin reload|reset <player>|set <player> <amount>");
            return true;
        }
        
        if (args[0].equalsIgnoreCase("reload")) {
            plugin.reloadConfig();
            plugin.getConfigManager().loadRewards();
            sender.sendMessage(ChatColor.GREEN + "Configuration reloaded!");
            return true;
        } else if (args[0].equalsIgnoreCase("reset")) {
            if (args.length < 2) {
                sender.sendMessage(ChatColor.RED + "Usage: /streakadmin reset <player>");
                return true;
            }
            
            Player target = Bukkit.getPlayer(args[1]);
            if (target == null) {
                sender.sendMessage(ChatColor.RED + "Player not found!");
                return true;
            }
            
            // Reset streak asynchronously
            new BukkitRunnable() {
                @Override
                public void run() {
                    plugin.getStreakDataManager().setPlayerStreak(target.getUniqueId(), 0);
                    
                    // Send confirmation on the main thread
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            sender.sendMessage(ChatColor.GREEN + "Reset streak for " + target.getName() + "!");
                            target.sendMessage(ChatColor.RED + "Your streak has been reset!");
                        }
                    }.runTask(plugin);
                }
            }.runTaskAsynchronously(plugin);
            
            return true;
        } else if (args[0].equalsIgnoreCase("set")) {
            if (args.length < 3) {
                sender.sendMessage(ChatColor.RED + "Usage: /streakadmin set <player> <amount>");
                return true;
            }
            
            Player target = Bukkit.getPlayer(args[1]);
            if (target == null) {
                sender.sendMessage(ChatColor.RED + "Player not found!");
                return true;
            }
            
            try {
                int amount = Integer.parseInt(args[2]);
                
                // Set streak asynchronously
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        plugin.getStreakDataManager().setPlayerStreak(target.getUniqueId(), amount);
                        
                        // Send confirmation on the main thread
                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                sender.sendMessage(ChatColor.GREEN + "Set streak for " + target.getName() + " to " + amount + "!");
                                target.sendMessage(ChatColor.GREEN + "Your streak has been set to " + amount + "!");
                            }
                        }.runTask(plugin);
                    }
                }.runTaskAsynchronously(plugin);
                
                return true;
            } catch (NumberFormatException e) {
                sender.sendMessage(ChatColor.RED + "Invalid amount! Must be a number.");
                return true;
            }
        }
        
        return false;
    }
}