package com.mdvcraft.mdveconomy.menus;

import java.util.*;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import com.mdvcraft.mdveconomy.MDVEconomyPlugin;
import com.mdvcraft.mdveconomy.items.ItemCodec;
import com.mdvcraft.mdveconomy.market.Models.*;

public final class MarketMenus {
    private final MDVEconomyPlugin plugin;
    private final MenuFiles files;
    private static final List<String> SORTS=List.of("RECENT","PRICE_HIGH","PRICE_LOW","SELLER_COUNT","POPULAR");
    public MarketMenus(MDVEconomyPlugin plugin,MenuFiles files) {this.plugin=plugin;this.files=files;}
    public void main(Player p) throws Exception {show(p,new MarketView("main",p.getUniqueId()));}
    private void go(Player p,MarketView from,String menu,long listing) throws Exception {
        var next=new MarketView(menu,p.getUniqueId());next.listing=listing;next.parent=from;show(p,next);
    }
    private Map<String,String> placeholders(Player p,MarketView v,Listing l) throws Exception {
        var values=new HashMap<String,String>();values.put("page",String.valueOf(v.page+1));values.put("hours",String.valueOf(v.hours));
        values.put("mode",v.mode.equals("BID")?"Pujas":"Trueque");values.put("sort",plugin.getConfig().getString("sort-names."+v.sort,v.sort));
        values.put("tier",v.tier.isEmpty()?"Todas":plugin.getConfig().getString("tiers."+v.tier,v.tier));
        values.put("active",String.valueOf(plugin.engine().activeCount(p.getUniqueId().toString())));values.put("limit",String.valueOf(plugin.settings().limit(p)));
        values.put("selected",String.valueOf(v.selected.size()));
        if(l!=null) {
            values.put("id",String.valueOf(l.id()));values.put("seller",l.sellerName());values.put("status",status(l.status()));
            values.put("price",String.valueOf(l.price()));values.put("next",String.valueOf(l.price()+1));
            values.put("cost",String.valueOf(l.winner().equals(p.getUniqueId().toString())?1:l.price()+1));
            values.put("winner",l.winnerName().isEmpty()?"Sin ofertas":l.winnerName());
            long minutes=Math.max(0,(l.expires()-System.currentTimeMillis())/60000);
            values.put("time",l.status().equals("ACTIVE")?(minutes/60+"h "+minutes%60+"m"):"Finalizada");
            values.put("mode",l.mode().equals("BID")?"Pujas":"Trueque");values.put("offers",String.valueOf(l.offers()));
            values.put("claimable",String.valueOf(plugin.engine().ready(p.getUniqueId().toString(),l.id())));
            values.put("tier",l.tier().isBlank()?"Vanilla / sin tier":plugin.getConfig().getString("tiers."+l.tier(),l.tier()));
        } return values;
    }
    private String status(String code) {return plugin.getConfig().getString("status-names."+code,code);}
    public void show(Player p,MarketView requested) throws Exception {
        if(!p.isOnline()) return;
        // Never replace the action map of a still-visible inventory before rendering succeeds.
        MarketView v=requested.copy();
        plugin.engine().expire(plugin.settings().batch());
        var c=files.get(v.menu);Listing l=v.listing>0?plugin.engine().get(v.listing):null;
        if(v.listing>0&&l==null) throw new IllegalArgumentException("Publicación inexistente.");
        if(l!=null)v.seenPrice=l.price();
        var values=placeholders(p,v,l);
        v.actions.clear();v.inventory(Bukkit.createInventory(v,c.getInt("rows",6)*9,IconFactory.text(IconFactory.replace(c.getString("title","Subastas"),values))));
        ItemStack filler=IconFactory.create(c.getConfigurationSection("filler"),Map.of(),false);
        for(int i=0;i<v.getInventory().getSize();i++) v.getInventory().setItem(i,filler.clone());
        for(var d:c.getMapList("decorations")) {
            var section=new org.bukkit.configuration.MemoryConfiguration(); d.forEach((k,value)->{if(!k.equals("slots"))section.set(k.toString(),value);});
            for(Object slot:(List<?>)d.get("slots"))v.getInventory().setItem(((Number)slot).intValue(),IconFactory.create(section,values,false));
        }
        for(int slot:c.getIntegerList("content-slots"))v.getInventory().setItem(slot,null);
        for(int slot:c.getIntegerList("input-slots"))v.getInventory().setItem(slot,null);
        if(c.contains("preview-slot")) {int slot=c.getInt("preview-slot");v.getInventory().setItem(slot,l==null?null:display(l,c,values));}
        var buttons=c.getConfigurationSection("buttons");
        if(buttons!=null) for(String key:buttons.getKeys(false)) {
            var b=buttons.getConfigurationSection(key);String action=b.getString("action",key);
            if(action.equals("PREVIOUS")||action.equals("NEXT"))continue;
            boolean owner=l!=null&&l.seller().equals(p.getUniqueId().toString());
            boolean active=l!=null&&l.status().equals("ACTIVE")&&l.expires()>System.currentTimeMillis();
            if(action.equals("SETTLEMENT")) {
                if(owner&&active)action="CANCEL_LISTING";
                else if(l!=null&&plugin.engine().ready(p.getUniqueId().toString(),l.id())>0) {
                    action="CLAIM"; b=c.getConfigurationSection("variants.claim");
                } else continue;
            }
            if(action.equals("CANCEL_LISTING")&&(!owner||!active))continue;
            if(action.equals("CLAIM")&&(l==null||plugin.engine().ready(p.getUniqueId().toString(),l.id())==0))continue;
            if(action.equals("HISTORY")&&(l==null||!l.mode().equals("BID")))continue;
            if(action.equals("OFFERS")&&(l==null||!l.mode().equals("TRADE")))continue;
            if(action.equals("OPEN_BUY")&&(!active||owner))continue;
            if((action.startsWith("BID_")||action.equals("SUBMIT_TRADE"))&&(!active||owner))continue;
            boolean glow=action.startsWith("CATEGORY:")&&action.substring(9).equals(v.category);
            if(action.equals("TOGGLE_MODE")) {
                String variant=v.mode.equals("BID")?"trade-mode":"bid-mode";
                if(c.isConfigurationSection("variants."+variant)) b=c.getConfigurationSection("variants."+variant);
            }
            put(v,buttons.getConfigurationSection(key).getInt("slot"),IconFactory.create(b,values,glow),action);
        }
        switch(v.menu) {
            case "browse","manage","participations" -> renderListings(p,v);
            case "offers" -> renderOffers(p,v,l);
            case "history" -> renderHistory(v,l);
            case "claims" -> renderClaims(p,v);
            case "trade","create" -> renderSelection(v);
            default -> {}
        }
        p.openInventory(v.getInventory());
    }
    private ItemStack display(Listing l,ConfigurationSection c,Map<String,String> values) {
        ItemStack item=ItemCodec.decode(l.item());var meta=item.getItemMeta();
        var lore=new ArrayList<>(meta.lore()==null?List.<net.kyori.adventure.text.Component>of():meta.lore());
        for(String line:c.getStringList("listing-lore"))lore.add(IconFactory.text(IconFactory.replace(line,values)));
        if(c.contains("listing-name"))meta.displayName(IconFactory.text(IconFactory.replace(c.getString("listing-name"),values)));
        meta.lore(lore);item.setItemMeta(meta);return item;
    }
    private void put(MarketView v,int slot,ItemStack item,String action) {v.getInventory().setItem(slot,item);v.actions.put(slot,action);}
    private void pagination(MarketView v,boolean next) {
        var c=files.get(v.menu).getConfigurationSection("buttons");if(c==null)return;
        for(String key:c.getKeys(false)) {var b=c.getConfigurationSection(key);String a=b.getString("action",key);
            if((a.equals("PREVIOUS")&&v.page>0)||(a.equals("NEXT")&&next))put(v,b.getInt("slot"),IconFactory.create(b,Map.of("page",String.valueOf(v.page+1)),false),a);
        }
    }
    private void renderListings(Player p,MarketView v) throws Exception {
        var c=files.get(v.menu);var slots=c.getIntegerList("content-slots");String owner=v.menu.equals("manage")?p.getUniqueId().toString():"";
        String participant=v.menu.equals("participations")?p.getUniqueId().toString():"";
        var rows=plugin.engine().listings(new Filter(v.category,v.tier,v.sort,owner,participant),v.page,slots.size());
        if(rows.isEmpty()&&v.page>0) {v.page--;renderListings(p,v);return;}
        for(int i=0;i<Math.min(rows.size(),slots.size());i++) {
            Listing l=rows.get(i);put(v,slots.get(i),display(l,c,placeholders(p,v,l)),"LISTING:"+l.id());
        } pagination(v,rows.size()>slots.size());
    }
    @SuppressWarnings("unchecked") private void renderOffers(Player p,MarketView v,Listing l) throws Exception {
        var c=files.get("offers");var heads=c.getIntegerList("head-slots");var accept=c.getIntegerList("accept-slots");
        var rows=(List<List<Integer>>)(List<?>)c.getList("offer-slots");var offers=plugin.engine().offers(l.id(),v.page,heads.size());
        if(offers.isEmpty()&&v.page>0) {v.page--;renderOffers(p,v,l);return;}
        for(int r=0;r<heads.size();r++) {v.getInventory().setItem(heads.get(r),null);for(int slot:rows.get(r))v.getInventory().setItem(slot,null);}
        for(int r=0;r<Math.min(offers.size(),heads.size());r++) {
            Offer o=offers.get(r);var vars=Map.of("player",o.name(),"status",status(o.status()),"date",date(o.created()),"offer",String.valueOf(o.id()));
            ItemStack head=IconFactory.create(c.getConfigurationSection("offer-head"),vars,false);
            if(head.getItemMeta() instanceof SkullMeta meta && c.getString("offer-head.texture","").isEmpty()) {
                // Known online profiles already contain signed skins; offline identity remains visible by name.
                Player online=Bukkit.getPlayer(UUID.fromString(o.player()));
                if(online!=null)meta.setOwnerProfile(online.getPlayerProfile()); else meta.setOwnerProfile(Bukkit.createPlayerProfile(UUID.fromString(o.player()),o.name()));
                head.setItemMeta(meta);
            }
            v.getInventory().setItem(heads.get(r),head);var items=ItemCodec.unpack(o.items());
            for(int j=0;j<Math.min(5,items.size());j++)v.getInventory().setItem(rows.get(r).get(j),items.get(j));
            if(l.seller().equals(p.getUniqueId().toString())&&l.status().equals("ACTIVE")&&o.status().equals("ACTIVE"))
                put(v,accept.get(r),IconFactory.create(c.getConfigurationSection("accept-icon"),vars,false),"ACCEPT:"+o.id());
        } pagination(v,offers.size()>heads.size());
    }
    private String date(long ms) {return DateTimeFormatter.ofPattern("dd/MM HH:mm").withZone(ZoneId.systemDefault()).format(Instant.ofEpochMilli(ms));}
    private void renderHistory(MarketView v,Listing l) throws Exception {
        var c=files.get("history");var rows=plugin.engine().bids(l.id());var slots=c.getIntegerList("content-slots");
        for(int i=0;i<Math.min(slots.size(),rows.size());i++) {Bid b=rows.get(i);v.getInventory().setItem(slots.get(i),IconFactory.create(c.getConfigurationSection("entry"),Map.of("player",b.name(),"amount",String.valueOf(b.amount()),"date",date(b.created())),false));}
    }
    private void renderClaims(Player p,MarketView v) throws Exception {
        var c=files.get("claims");var slots=c.getIntegerList("content-slots");var rows=plugin.engine().claims(p.getUniqueId().toString(),v.listing,v.page,slots.size());
        if(rows.isEmpty()&&v.page>0) {v.page--;renderClaims(p,v);return;}
        for(int i=0;i<Math.min(rows.size(),slots.size());i++) {
            Claim claim=rows.get(i);var values=Map.of("label",claim.label(),"amount",String.valueOf(claim.amount()),"kind",claim.kind(),"status",status(claim.status()),"id",String.valueOf(claim.id()));
            ItemStack icon=IconFactory.create(c.getConfigurationSection("entry"),values,false);
            if(claim.kind().equals("ITEMS")) {var items=ItemCodec.unpack(claim.payload());if(!items.isEmpty()) {icon=items.getFirst().clone();var meta=icon.getItemMeta();meta.lore(c.getStringList("entry.lore").stream().map(s->IconFactory.text(IconFactory.replace(s,values))).toList());icon.setItemMeta(meta);}}
            put(v,slots.get(i),icon,"REDEEM:"+claim.id());
        } pagination(v,rows.size()>slots.size());
    }
    private void renderSelection(MarketView v) {
        var slots=files.get(v.menu).getIntegerList("input-slots");int i=0;
        for(var e:v.selected.entrySet()) {ItemStack item=e.getValue().clone();var meta=item.getItemMeta();var lore=new ArrayList<>(meta.lore()==null?List.<net.kyori.adventure.text.Component>of():meta.lore());
            for(String line:files.get(v.menu).getStringList("selection-lore"))lore.add(IconFactory.text(line));meta.lore(lore);item.setItemMeta(meta);
            put(v,slots.get(i++),item,"UNSELECT:"+e.getKey());}
    }
    public void select(Player p,MarketView v,int slot) throws Exception {
        if(!Set.of("create","trade").contains(v.menu)||slot<0||slot>=p.getInventory().getStorageContents().length)return;
        if(v.selected.remove(slot)!=null) {show(p,v);return;}
        ItemStack stack=p.getInventory().getItem(slot);if(stack==null||stack.getType().isAir())return;
        if(v.menu.equals("create")) {if(plugin.catalog().denar(stack))throw new IllegalArgumentException("No podés publicar denares físicos.");v.selected.clear();}
        if(v.selected.size()>=5)throw new IllegalArgumentException("Solo podés ofrecer cinco pilas.");
        v.selected.put(slot,stack.clone());show(p,v);
    }
    public void action(Player p,MarketView v,String action) throws Exception {
        String player=p.getUniqueId().toString();
        if(action.startsWith("CATEGORY:")) {String key=action.substring(9);v.category=key.equals(v.category)?"":key;v.page=0;show(p,v);return;}
        if(action.startsWith("LISTING:")) {
            long id=Long.parseLong(action.substring(8));Listing l=plugin.engine().get(id);
            if(l==null)throw new IllegalArgumentException("Publicación inexistente.");
            String target=l.seller().equals(player)||!l.status().equals("ACTIVE")||v.menu.equals("participations")?"owner":l.mode().equals("BID")?"bid":"trade";
            go(p,v,target,id);return;
        }
        if(action.startsWith("UNSELECT:")) {v.selected.remove(Integer.parseInt(action.substring(9)));show(p,v);return;}
        if(action.startsWith("ACCEPT:")) {plugin.engine().accept(player,v.listing,Long.parseLong(action.substring(7)));plugin.message(p,"trade-accepted");show(p,v);return;}
        if(action.startsWith("REDEEM:")) {
            long claimId=Long.parseLong(action.substring(7));
            boolean cash=plugin.engine().claims(player,v.listing,v.page,files.get("claims").getIntegerList("content-slots").size()).stream().anyMatch(c->c.id()==claimId&&c.kind().equals("CASH"));
            boolean ok=plugin.engine().redeem(player,claimId,cash?plugin.transfers().cashCapacity(p):Long.MAX_VALUE,c->plugin.transfers().credit(p,c));
            plugin.message(p,ok?"claimed":"payment-failed");show(p,v);return;
        }
        switch(action) {
            case "BROWSE" -> go(p,v,"browse",0);
            case "MANAGE" -> go(p,v,"manage",0);
            case "PARTICIPATIONS" -> go(p,v,"participations",0);
            case "BANK" -> plugin.message(p,"bank-unavailable");
            case "CLOSE" -> p.closeInventory();
            case "BACK","CANCEL_DRAFT" -> {if(v.parent!=null)show(p,v.parent);else main(p);}
            case "PREVIOUS" -> {if(v.page>0)v.page--;show(p,v);}
            case "NEXT" -> {v.page++;show(p,v);}
            case "SORT" -> {v.sort=SORTS.get((SORTS.indexOf(v.sort)+1)%SORTS.size());v.page=0;show(p,v);}
            case "TIER" -> {var tiers=new ArrayList<String>();tiers.add("");var section=plugin.getConfig().getConfigurationSection("tiers");if(section!=null)tiers.addAll(section.getKeys(false));v.tier=tiers.get((tiers.indexOf(v.tier)+1)%tiers.size());v.page=0;show(p,v);}
            case "CREATE" -> {if(plugin.engine().activeCount(player)>=plugin.settings().limit(p))throw new IllegalArgumentException("No tenés espacios disponibles.");go(p,v,"create",0);}
            case "TOGGLE_MODE" -> {v.mode=v.mode.equals("BID")?"TRADE":"BID";show(p,v);}
            case "DURATION" -> {var hours=plugin.settings().hours(p);v.hours=hours.get((hours.indexOf(v.hours)+1)%hours.size());show(p,v);}
            case "PUBLISH" -> {
                if(v.selected.size()!=1)throw new IllegalArgumentException("Seleccioná el artículo de tu inventario.");
                if(!plugin.settings().hours(p).contains(v.hours))throw new IllegalArgumentException("No tenés permiso para esa duración.");
                ItemStack item=v.selected.values().iterator().next();if(plugin.catalog().denar(item))throw new IllegalArgumentException("No podés publicar denares.");
                plugin.engine().create(player,p.getName(),ItemCodec.encode(item),plugin.catalog().category(item),plugin.catalog().tier(item),v.mode,v.hours*3600000L,plugin.settings().limit(p),plugin.transfers().takeSelected(p,v.selected));
                v.selected.clear();plugin.message(p,"published");go(p,null,"manage",0);
            }
            case "BID_BANK","BID_CASH" -> {
                Listing current=plugin.engine().get(v.listing);
                if(current!=null&&current.price()!=v.seenPrice) {show(p,v);plugin.message(p,"bid-changed");return;}
                String kind=action.equals("BID_BANK")?"BANK":"CASH";
                plugin.engine().bid(player,p.getName(),v.listing,kind,plugin.settings().maxBid(),amount->plugin.transfers().debit(p,kind,amount));plugin.message(p,"bid-placed");show(p,v);
            }
            case "SUBMIT_TRADE" -> {
                var items=v.selected.values().stream().map(ItemCodec::encode).toList();
                plugin.engine().offer(player,p.getName(),v.listing,items,plugin.settings().maxOffers(),plugin.transfers().takeSelected(p,v.selected));
                v.selected.clear();plugin.message(p,"offer-sent");show(p,v);
            }
            case "CANCEL_LISTING" -> {plugin.engine().cancel(player,v.listing);plugin.message(p,"listing-cancelled");show(p,v);}
            case "CLAIM","CLAIMS" -> go(p,v,"claims",action.equals("CLAIM")?v.listing:0);
            case "HISTORY" -> go(p,v,"history",v.listing);
            case "OFFERS" -> go(p,v,"offers",v.listing);
            case "OPEN_BUY" -> {Listing l=plugin.engine().get(v.listing);if(l==null)throw new IllegalArgumentException("Publicación inexistente.");go(p,v,l.mode().equals("BID")?"bid":"trade",v.listing);}
            case "INFO" -> {}
            default -> throw new IllegalArgumentException("Acción de menú desconocida: "+action);
        }
    }
}
