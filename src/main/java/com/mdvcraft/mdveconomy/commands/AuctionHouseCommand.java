package com.mdvcraft.mdveconomy.commands;

import com.mdvcraft.mdveconomy.MDVEconomyPlugin;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import java.util.*;

/** Player entry point for the auction menus, including the /subastas alias. */
public final class AuctionHouseCommand implements CommandExecutor, TabCompleter {
    private final MDVEconomyPlugin plugin;
    public AuctionHouseCommand(MDVEconomyPlugin plugin) { this.plugin = plugin; }
    @Override public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        try {
            if (!(sender instanceof Player player)) { sender.sendMessage("Solo jugadores."); return true; }
            if (args.length > 0) { sender.sendMessage("Usá /ah y el botón de crear publicación."); return true; }
            plugin.menus().main(player);
        } catch (Exception e) { plugin.failure(sender, e); }
        return true;
    }
    @Override public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return List.of();
    }
}
