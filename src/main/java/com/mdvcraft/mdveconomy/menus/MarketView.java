package com.mdvcraft.mdveconomy.menus;
import java.util.*;
import org.bukkit.inventory.*;

public final class MarketView implements InventoryHolder {
    public final String menu;
    public final UUID player;
    public final Map<Integer,String> actions=new HashMap<>();
    public final LinkedHashMap<Integer,ItemStack> selected=new LinkedHashMap<>();
    public String category="",tier="",sort="RECENT",mode="BID",back="main";
    public int page=0,hours=10;
    public long listing=0;
    public long seenPrice=0;
    public boolean busy=false;
    public MarketView parent;
    private Inventory inventory;
    public MarketView(String menu,UUID player) {this.menu=menu;this.player=player;}
    public MarketView copy() {
        var view=new MarketView(menu,player);view.category=category;view.tier=tier;view.sort=sort;view.mode=mode;
        view.back=back;view.page=page;view.hours=hours;view.listing=listing;view.seenPrice=seenPrice;view.parent=parent;
        selected.forEach((slot,item)->view.selected.put(slot,item.clone()));return view;
    }
    public void inventory(Inventory inventory) {this.inventory=inventory;}
    @Override public Inventory getInventory() {return inventory;}
}
