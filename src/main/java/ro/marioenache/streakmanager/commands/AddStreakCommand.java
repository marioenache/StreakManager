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
            sender.sendMessage(ChatColor.RED + "You don't have permission to use this command!");
            return true;
        }

        if (args.length < 1) {
            sender.sendMessage(ChatColor.RED + "Usage: /addstreak <player> [amount]");
            return true;
        }

        final Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            sender.sendMessage(ChatColor.RED + "Player not found!");
            return true;
        }

        final int amount;
        if (args.length > 1) {
            try {
                amount = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                sender.sendMessage(ChatColor.RED + "Invalid amount! Must be a number.");
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
                        sender.sendMessage(ChatColor.GREEN + "Added " + amount + " streak to " + target.getName() + "!");
                        target.sendMessage(ChatColor.GREEN + "Your streak has been increased by " + amount + "!");
                    }
                }.runTask(plugin);
            }
        }.runTaskAsynchronously(plugin);

        return true;
    }
}