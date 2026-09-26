package com.mdvcraft.mdveconomy.commands;

import com.mdvcraft.mdveconomy.MDVEconomyPlugin;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import java.util.*;

/** Connects plugin.yml declarations to their independent handlers. */
public final class CommandRegistration {
    private CommandRegistration() {}
    public static void register(MDVEconomyPlugin plugin) {
        var auction = new AuctionHouseCommand(plugin);
        bind(plugin, "ah", auction, auction);
        var admin = new EconomyAdminCommand(plugin);
        bind(plugin, "mdveconomy", admin, admin);
    }
    private static void bind(MDVEconomyPlugin plugin, String name, CommandExecutor executor, TabCompleter completer) {
        var command = Objects.requireNonNull(plugin.getCommand(name), "Comando ausente en plugin.yml: " + name);
        command.setExecutor(executor);
        command.setTabCompleter(completer);
    }
}
