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
            String noPermMsg = plugin.getConfig().getString("messages.no-permission", "&cYou don't have permission to use this command!");
            if (noPermMsg != null && !noPermMsg.isEmpty()) {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', noPermMsg));
            }
            return true;
        }

        if (args.length < 1) {
            sender.sendMessage(ChatColor.RED + "Usage: /streakadmin reload|reset <player>|set <player> <amount>");
            return true;
        }

        if (args[0].equalsIgnoreCase("reload")) {
            plugin.reloadConfig();
            plugin.getConfigManager().loadRewards();
            String reloadMsg = plugin.getConfig().getString("messages.config-reloaded", "&aConfiguration reloaded!");
            if (reloadMsg != null && !reloadMsg.isEmpty()) {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', reloadMsg));
            }
            return true;
        } else if (args[0].equalsIgnoreCase("reset")) {
            if (args.length < 2) {
                sender.sendMessage(ChatColor.RED + "Usage: /streakadmin reset <player>");
                return true;
            }

            Player target = Bukkit.getPlayer(args[1]);
            if (target == null) {
                String playerNotFoundMsg = plugin.getConfig().getString("messages.player-not-found", "&cPlayer not found!");
                if (playerNotFoundMsg != null && !playerNotFoundMsg.isEmpty()) {
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', playerNotFoundMsg));
                }
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
                            String resetAdminMsg = plugin.getConfig().getString("messages.streak.reset-admin", "&aReset streak for %player%!")
                                    .replace("%player%", target.getName());
                            if (resetAdminMsg != null && !resetAdminMsg.isEmpty()) {
                                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', resetAdminMsg));
                            }

                            String resetMsg = plugin.getConfig().getString("messages.streak.reset", "&cYour streak has been reset!");
                            if (resetMsg != null && !resetMsg.isEmpty()) {
                                target.sendMessage(ChatColor.translateAlternateColorCodes('&', resetMsg));
                            }
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
                String playerNotFoundMsg = plugin.getConfig().getString("messages.player-not-found", "&cPlayer not found!");
                if (playerNotFoundMsg != null && !playerNotFoundMsg.isEmpty()) {
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', playerNotFoundMsg));
                }
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
                                String setMsg = plugin.getConfig().getString("messages.streak.set", "&aSet streak for %player% to %amount%!")
                                        .replace("%player%", target.getName())
                                        .replace("%amount%", String.valueOf(amount));
                                if (setMsg != null && !setMsg.isEmpty()) {
                                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', setMsg));
                                }

                                String setReceivedMsg = plugin.getConfig().getString("messages.streak.set-received", "&aYour streak has been set to %amount%!")
                                        .replace("%amount%", String.valueOf(amount));
                                if (setReceivedMsg != null && !setReceivedMsg.isEmpty()) {
                                    target.sendMessage(ChatColor.translateAlternateColorCodes('&', setReceivedMsg));
                                }
                            }
                        }.runTask(plugin);
                    }
                }.runTaskAsynchronously(plugin);

                return true;
            } catch (NumberFormatException e) {
                String invalidAmountMsg = plugin.getConfig().getString("messages.invalid-amount", "&cInvalid amount! Must be a number.");
                if (invalidAmountMsg != null && !invalidAmountMsg.isEmpty()) {
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', invalidAmountMsg));
                }
                return true;
            }
        }

        return false;
    }
}