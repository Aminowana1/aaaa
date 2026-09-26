package com.mdvcraft.mdveconomy.commands;

import com.mdvcraft.mdveconomy.MDVEconomyPlugin;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import java.util.*;

/** Administrative actions; permission checks run before any mutation. */
public final class EconomyAdminCommand implements CommandExecutor, TabCompleter {
    private final MDVEconomyPlugin plugin;
    public EconomyAdminCommand(MDVEconomyPlugin plugin) { this.plugin = plugin; }
    @Override public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        try {
            if(args.length==1&&args[0].equalsIgnoreCase("reload")) {
                if(!sender.hasPermission("mdveconomy.reload"))throw new IllegalArgumentException("Sin permiso.");
                plugin.reloadMarketConfiguration();sender.sendMessage("Configuración, menús y temporizador recargados.");return true;
            }
            if(args.length==1&&args[0].equalsIgnoreCase("revisiones")) {
                if(!sender.hasPermission("mdveconomy.admin"))throw new IllegalArgumentException("Sin permiso.");
                var ops=plugin.engine().reviews();sender.sendMessage("Operaciones en revisión (máximo 100): "+ops.size());
                for(var o:ops)sender.sendMessage("#"+o.id()+" "+o.direction()+" "+o.kind()+" "+o.amount()+" jugador="+o.player()+" publicación="+o.listing()+" "+o.note());return true;
            }
            if(args.length==3&&args[0].equalsIgnoreCase("resolver")) {
                if(!sender.hasPermission("mdveconomy.admin"))throw new IllegalArgumentException("Sin permiso.");
                plugin.engine().resolve(Long.parseLong(args[1]),args[2].toLowerCase(Locale.ROOT));
                plugin.getLogger().warning("Conciliación manual por "+sender.getName()+": operación "+args[1]+" "+args[2]);sender.sendMessage("Operación conciliada.");return true;
            }
            sender.sendMessage("/mdveconomy reload | revisiones | resolver <id> <retirado|no-retirado|entregado|no-entregado>");
        } catch(Exception e) {plugin.failure(sender,e);}return true;
    }
    @Override public List<String> onTabComplete(CommandSender sender,Command command,String alias,String[] args) {
        if(args.length!=1)return List.of();
        var choices=new ArrayList<String>();if(sender.hasPermission("mdveconomy.reload"))choices.add("reload");if(sender.hasPermission("mdveconomy.admin")){choices.add("revisiones");choices.add("resolver");}
        return choices.stream().filter(s->s.startsWith(args[0].toLowerCase(Locale.ROOT))).toList();
    }
}
