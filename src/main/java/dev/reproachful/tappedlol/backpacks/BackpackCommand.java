package dev.reproachful.tappedlol.backpacks;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class BackpackCommand implements CommandExecutor {

    private final Backpacks core;

    public BackpackCommand(Backpacks core) {
        this.core = core;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length > 1) {
            // bp [target] [id]
            Player target = Bukkit.getPlayer(args[0]);
            if (target != null) {
                if (core.getBackpacks().containsKey(args[1])) {
                    boolean success = target.getInventory().addItem(core.getBackpack(args[1])).isEmpty();
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7Successfully gave " + target.getName() + " a " + args[1] + " backpack!"));
                    target.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7You have received a " + args[1] + " backpack!"));
                    if (!success) {
                        target.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cYour inventory is full! Dropping backpack on the ground!"));
                    }
                } else {
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cThat is not a valid backpack!"));
                }
            } else {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cThat player is not online!"));
            }
        } else {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cInvalid usage. Usage /backpack [player] [size]"));
        }
        return false;
    }
}
