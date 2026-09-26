package com.mdvcraft.mdveconomy.commands;
import com.mdvcraft.commandtree.*;
import com.mdvcraft.mdveconomy.MDVEconomyPlugin;
import org.bukkit.command.*;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.entity.Player;
import java.util.*;
public final class CommandRegistration {
    private CommandRegistration() {}
    public static void register(MDVEconomyPlugin plugin) {
        bind(plugin, "ah", CAuctionHouse.class);
        bind(plugin, "mdveconomy", CMDVEconomy.class);
    }
    private static void bind(MDVEconomyPlugin plugin, String name, Class<? extends CommandNode<MDVEconomyPlugin>> root) {
        var command = Objects.requireNonNull(plugin.getCommand(name), "Comando ausente en plugin.yml: " + name);
        var tree = new CommandTreeManager<MDVEconomyPlugin>(plugin, root);
        command.setExecutor(tree); command.setTabCompleter(tree);
    }
}
