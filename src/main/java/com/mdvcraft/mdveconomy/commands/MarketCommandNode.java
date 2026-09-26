package com.mdvcraft.mdveconomy.commands;
import com.mdvcraft.commandtree.*;
import com.mdvcraft.mdveconomy.MDVEconomyPlugin;
import org.bukkit.command.*;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.entity.Player;
import java.util.*;
/** Shared permission and error boundary for every command-tree node. */
public abstract class MarketCommandNode extends CommandNode<MDVEconomyPlugin> {
    protected MarketCommandNode(CommandSender sender, MDVEconomyPlugin plugin) { super(sender, plugin); }
    protected String permission() { return ""; }
    @Override protected boolean hasPermission() { return permission().isEmpty() || sender.hasPermission(permission()); }
    @Override public final boolean execute(List<String> args) {
        if (!hasPermission()) { sender.sendMessage("Sin permiso."); return true; }
        try { run(new ArrayList<>(args)); } catch (Exception e) { plugin.failure(sender, e); }
        return true;
    }
    protected abstract void run(List<String> args) throws Exception;
    protected final boolean count(List<String> args, int expected) {
        if (args.size() == expected) return true;
        sender.sendMessage("Uso: /mdveconomy " + getName() + (expected == 2 ? " <id> <retirado|no-retirado|entregado|no-entregado>" : ""));
        return false;
    }
    @Override public List<String> getTabCompletions(List<String> args) {
        return hasPermission() ? super.getTabCompletions(args) : List.of();
    }
}
