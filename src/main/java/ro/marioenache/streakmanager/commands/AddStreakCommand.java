package ro.marioenache.streakmanager.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import ro.marioenache.streakmanager.StreakManager;

public class AddStreakCommand implements CommandExecutor {

    private final StreakManager plugin;

    public AddStreakCommand(StreakManager plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!sender.hasPermission("streakmanager.add")) {
            String noPermMsg = plugin.getConfig().getString("messages.no-permission", "&cYou don't have permission to use this command!");
            if (noPermMsg != null && !noPermMsg.isEmpty()) {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', noPermMsg));
            }
            return true;
        }

        if (args.length < 1) {
            sender.sendMessage(ChatColor.RED + "Usage: /addstreak <player> [amount]");
            return true;
        }

        final Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            String playerNotFoundMsg = plugin.getConfig().getString("messages.player-not-found", "&cPlayer not found!");
            if (playerNotFoundMsg != null && !playerNotFoundMsg.isEmpty()) {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', playerNotFoundMsg));
            }
            return true;
        }

        final int amount;
        if (args.length > 1) {
            try {
                amount = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                String invalidAmountMsg = plugin.getConfig().getString("messages.invalid-amount", "&cInvalid amount! Must be a number.");
                if (invalidAmountMsg != null && !invalidAmountMsg.isEmpty()) {
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', invalidAmountMsg));
                }
                return true;
            }
        } else {
            amount = 1;
        }

        // Add streak asynchronously
        new BukkitRunnable() {
            @Override
            public void run() {
                plugin.getStreakDataManager().addPlayerStreak(target.getUniqueId(), amount);

                // Send confirmation on the main thread
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        String addedMsg = plugin.getConfig().getString("messages.streak.added", "&aAdded %amount% streak to %player%!")
                                .replace("%amount%", String.valueOf(amount))
                                .replace("%player%", target.getName());
                        if (addedMsg != null && !addedMsg.isEmpty()) {
                            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', addedMsg));
                        }

                        String receivedMsg = plugin.getConfig().getString("messages.streak.received", "&aYour streak has been increased by %amount%!")
                                .replace("%amount%", String.valueOf(amount));
                        if (receivedMsg != null && !receivedMsg.isEmpty()) {
                            target.sendMessage(ChatColor.translateAlternateColorCodes('&', receivedMsg));
                        }
                    }
                }.runTask(plugin);
            }
        }.runTaskAsynchronously(plugin);

        return true;
    }
}