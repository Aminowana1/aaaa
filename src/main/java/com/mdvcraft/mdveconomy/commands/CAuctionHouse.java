package com.mdvcraft.mdveconomy.commands;
import com.mdvcraft.commandtree.*;
import com.mdvcraft.mdveconomy.MDVEconomyPlugin;
import org.bukkit.command.*;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.entity.Player;
import java.util.*;
public final class CAuctionHouse extends MarketCommandNode {
    public CAuctionHouse(CommandSender sender, JavaPlugin plugin) { super(sender, (MDVEconomyPlugin) plugin); }
    @Override public String getName() { return "ah"; }
    @Override public List<String> getAliases() { return List.of("subastas"); }
    @Override protected void run(List<String> args) throws Exception {
        if (!(sender instanceof Player player)) { sender.sendMessage("Solo jugadores."); return; }
        if (!args.isEmpty()) { sender.sendMessage("Usá /ah y el botón de crear publicación."); return; }
        plugin.menus().main(player);
    }
}

