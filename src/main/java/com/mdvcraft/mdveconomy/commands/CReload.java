package com.mdvcraft.mdveconomy.commands;
import com.mdvcraft.commandtree.*;
import com.mdvcraft.mdveconomy.MDVEconomyPlugin;
import org.bukkit.command.*;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.entity.Player;
import java.util.*;
public final class CReload extends MarketCommandNode {
    public CReload(CommandSender sender, MDVEconomyPlugin plugin) { super(sender, plugin); }
    @Override public String getName() { return "reload"; }
    @Override protected String permission() { return "mdveconomy.reload"; }
    @Override protected void run(List<String> args) throws Exception { if (!count(args, 0)) return; plugin.reloadMarketConfiguration(); sender.sendMessage("Configuración, menús y temporizador recargados."); }
}
