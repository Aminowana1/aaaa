package com.mdvcraft.mdveconomy.commands;
import com.mdvcraft.commandtree.*;
import com.mdvcraft.mdveconomy.MDVEconomyPlugin;
import org.bukkit.command.*;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.entity.Player;
import java.util.*;
public final class CResolve extends MarketCommandNode {
    public CResolve(CommandSender sender, MDVEconomyPlugin plugin) { super(sender, plugin); }
    @Override public String getName() { return "resolver"; }
    @Override protected String permission() { return "mdveconomy.admin"; }
    @Override protected void run(List<String> args) throws Exception { if (!count(args, 2)) return;
        plugin.engine().resolve(Long.parseLong(args.get(0)), args.get(1).toLowerCase(Locale.ROOT));
        plugin.getLogger().warning("Conciliación manual por "+sender.getName()+": operación "+args.get(0)+" "+args.get(1)); sender.sendMessage("Operación conciliada."); }
}
