package com.mdvcraft.mdveconomy.commands;
import com.mdvcraft.commandtree.*;
import com.mdvcraft.mdveconomy.MDVEconomyPlugin;
import org.bukkit.command.*;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.entity.Player;
import java.util.*;
public final class CReviews extends MarketCommandNode {
    public CReviews(CommandSender sender, MDVEconomyPlugin plugin) { super(sender, plugin); }
    @Override public String getName() { return "revisiones"; }
    @Override protected String permission() { return "mdveconomy.admin"; }
    @Override protected void run(List<String> args) throws Exception { if (!count(args, 0)) return;
        var ops = plugin.engine().reviews(); sender.sendMessage("Operaciones en revisión (máximo 100): " + ops.size());
        for (var o : ops) sender.sendMessage("#"+o.id()+" "+o.direction()+" "+o.kind()+" "+o.amount()+" jugador="+o.player()+" publicación="+o.listing()+" "+o.note()); }
}
