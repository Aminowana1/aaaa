package com.mdvcraft.mdveconomy.menus;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.*;
import com.mdvcraft.mdveconomy.MDVEconomyPlugin;

public final class MenuListener implements Listener {
    private final MDVEconomyPlugin plugin;
    public MenuListener(MDVEconomyPlugin plugin) {this.plugin=plugin;}
    @EventHandler(priority=EventPriority.HIGHEST) public void click(InventoryClickEvent event) {
        if(!(event.getView().getTopInventory().getHolder() instanceof MarketView view))return;
        event.setCancelled(true);
        if(!(event.getWhoClicked() instanceof Player p)||!view.player.equals(p.getUniqueId())||view.busy)return;
        if(event.getClick()!=ClickType.LEFT && event.getClick()!=ClickType.RIGHT)return;
        boolean bottom=event.getClickedInventory()==p.getInventory();int slot=event.getSlot();
        String action=event.getClickedInventory()==event.getView().getTopInventory()?view.actions.get(event.getRawSlot()):null;
        if(!bottom&&action==null)return;
        view.busy=true;
        plugin.getServer().getScheduler().runTask(plugin,()-> {
            try {
                if(!p.isOnline()||p.getOpenInventory().getTopInventory().getHolder()!=view)return;
                if(bottom)plugin.menus().select(p,view,slot);else plugin.menus().action(p,view,action);
            } catch(Exception e) {plugin.failure(p,e);}
            finally {view.busy=false;}
        });
    }
    @EventHandler(priority=EventPriority.HIGHEST) public void drag(InventoryDragEvent event) {
        if(event.getView().getTopInventory().getHolder() instanceof MarketView)event.setCancelled(true);
    }
    @EventHandler(priority=EventPriority.HIGHEST) public void swap(PlayerSwapHandItemsEvent event) {
        if(event.getPlayer().getOpenInventory().getTopInventory().getHolder() instanceof MarketView)event.setCancelled(true);
    }
    @EventHandler(priority=EventPriority.HIGHEST) public void drop(PlayerDropItemEvent event) {
        if(event.getPlayer().getOpenInventory().getTopInventory().getHolder() instanceof MarketView)event.setCancelled(true);
    }
    @EventHandler public void join(PlayerJoinEvent event) {
        plugin.getServer().getScheduler().runTask(plugin,()-> {
            if(!event.getPlayer().isOnline())return;
            try {if(!plugin.engine().claims(event.getPlayer().getUniqueId().toString(),0,0,1).isEmpty())plugin.message(event.getPlayer(),"pending-on-join");}
            catch(Exception e) {plugin.getLogger().warning("No se pudieron consultar pendientes: "+e.getMessage());}
        });
    }
}
