package com.mdvcraft.mdveconomy.commands;
import com.mdvcraft.commandtree.CommandTreeManager;
import com.mdvcraft.mdveconomy.MDVEconomyPlugin;
import org.bukkit.command.CommandSender;
import org.junit.jupiter.api.Test;
import java.lang.reflect.Proxy;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
class CommandTreeTest {
    private CommandSender sender(Set<String> permissions, List<String> messages) {
        return (CommandSender) Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[]{CommandSender.class}, (p,m,a) -> {
            if (m.getName().equals("hasPermission")) return permissions.contains(a[0]);
            if (m.getName().equals("sendMessage")) { if(a[0] instanceof String s) messages.add(s); return null; }
            if (m.getName().equals("getName")) return "tester";
            if (m.getReturnType() == boolean.class) return false;
            return null;
        });
    }
    @Test void managerCreatesRootReflectivelyAndHandlesAuctionAlias() {
        var messages=new ArrayList<String>(); var sender=sender(Set.of(),messages);
        var tree=new CommandTreeManager<MDVEconomyPlugin>(null,CAuctionHouse.class);
        assertTrue(tree.onCommand(sender,null,"subastas",new String[0]));
        assertEquals(List.of("Solo jugadores."),messages);
    }
    @Test void treeDispatchChecksAllAdministrativePermissionsBeforeAccessingPlugin() {
        var messages=new ArrayList<String>(); var sender=sender(Set.of(),messages);
        var tree=new CommandTreeManager<MDVEconomyPlugin>(null,CMDVEconomy.class);
        tree.onCommand(sender,null,"mdveconomy",new String[]{"reload"});
        tree.onCommand(sender,null,"mdveconomy",new String[]{"revisiones"});
        tree.onCommand(sender,null,"mdveconomy",new String[]{"resolver","1","entregado"});
        assertEquals(List.of("Sin permiso.","Sin permiso.","Sin permiso."),messages);
    }
    @Test void completionOnlyShowsPermittedChildren() {
        var tree=new CommandTreeManager<MDVEconomyPlugin>(null,CMDVEconomy.class);
        assertEquals(List.of(),tree.onTabComplete(sender(Set.of(),new ArrayList<>()),null,"mdveconomy",new String[]{""}));
        assertEquals(List.of("reload"),tree.onTabComplete(sender(Set.of("mdveconomy.reload"),new ArrayList<>()),null,"mdveconomy",new String[]{"RE"}));
        assertEquals(List.of("resolver","revisiones"),tree.onTabComplete(sender(Set.of("mdveconomy.admin"),new ArrayList<>()),null,"mdveconomy",new String[]{"re"}));
    }
    @Test void wrongArityDoesNotPerformAdministrativeActions() {
        var messages=new ArrayList<String>(); var sender=sender(Set.of("mdveconomy.admin","mdveconomy.reload"),messages);
        var tree=new CommandTreeManager<MDVEconomyPlugin>(null,CMDVEconomy.class);
        tree.onCommand(sender,null,"mdveconomy",new String[]{"reload","extra"});
        tree.onCommand(sender,null,"mdveconomy",new String[]{"revisiones","extra"});
        tree.onCommand(sender,null,"mdveconomy",new String[]{"resolver","1"});
        assertEquals(3,messages.size()); assertTrue(messages.stream().allMatch(s->s.startsWith("Uso:")));
    }
}
