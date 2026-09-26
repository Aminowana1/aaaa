package com.mdvcraft.mdveconomy.commands;
import com.mdvcraft.commandtree.*;
import com.mdvcraft.mdveconomy.MDVEconomyPlugin;
import org.bukkit.command.*;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.entity.Player;
import java.util.*;
public final class CMDVEconomy extends MarketCommandNode {
    public CMDVEconomy(CommandSender sender, JavaPlugin plugin) { super(sender, (MDVEconomyPlugin) plugin); }
    @Override public String getName() { return "mdveconomy"; }
    @Override public List<CommandNode<MDVEconomyPlugin>> getSubCommands() {
        return List.of(new CReload(sender, plugin), new CReviews(sender, plugin), new CResolve(sender, plugin));
    }
    @Override protected void run(List<String> args) {
        if (!executeSubcommands(args)) sender.sendMessage("/mdveconomy reload | revisiones | resolver <id> <retirado|no-retirado|entregado|no-entregado>");
    }
    @Override public List<String> getTabCompletions(List<String> args) {
        var children = getSubCommands().stream().map(n -> (MarketCommandNode)n).filter(MarketCommandNode::hasPermission).toList();
        if (args.size() > 1) return children.stream().filter(n -> n.matches(args)).findFirst()
            .map(n -> n.getTabCompletions(args.subList(1, args.size()))).orElse(List.of());
        String prefix = args.isEmpty() ? "" : args.getFirst().toLowerCase(Locale.ROOT);
        return children.stream().map(CommandNode::getName).filter(n -> n.startsWith(prefix)).sorted().toList();
    }
}
