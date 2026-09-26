package com.mdvcraft.mdveconomy.items;

import java.util.*;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import net.milkbowl.vault.economy.Economy;
import com.mdvcraft.mdveconomy.market.MarketEngine.Transfer;
import com.mdvcraft.mdveconomy.market.Models.Claim;

/** Precomputes whole-inventory changes. Never drops overflow or uses temporary menu items as assets. */
public final class InventoryTransfers {
    private final Economy economy;
    private final ItemCatalog catalog;
    public InventoryTransfers(Economy economy,ItemCatalog catalog) {this.economy=economy;this.catalog=catalog;}
    private static void mainThread(Player player) {
        if(!Bukkit.isPrimaryThread()||!player.isOnline()) throw new IllegalStateException("El jugador debe estar conectado.");
    }
    private static ItemStack[] copy(ItemStack[] input) { return Arrays.stream(input).map(s->s==null?null:s.clone()).toArray(ItemStack[]::new); }
    private static Transfer replace(Player player,ItemStack[] before,ItemStack[] after) {
        return ()->{ mainThread(player); if(!Arrays.equals(before,player.getInventory().getStorageContents())) return false;
            player.getInventory().setStorageContents(copy(after)); player.saveData(); return true; };
    }
    public Transfer takeSelected(Player player,Map<Integer,ItemStack> selected) {
        mainThread(player); if(selected.isEmpty()||selected.size()>5) throw new IllegalArgumentException("Seleccioná entre 1 y 5 pilas.");
        ItemStack[] before=copy(player.getInventory().getStorageContents()),after=copy(before);
        for(var e:selected.entrySet()) {
            int slot=e.getKey(); if(slot<0||slot>=before.length||!Objects.equals(before[slot],e.getValue())) throw new IllegalArgumentException("El inventario cambió. Volvé a seleccionar los artículos.");
            after[slot]=null;
        } return replace(player,before,after);
    }
    public Transfer debit(Player player,String kind,long amount) {
        mainThread(player); if(amount<1||amount>Integer.MAX_VALUE) throw new IllegalArgumentException("Importe inválido.");
        if(kind.equals("BANK")) return ()->{ mainThread(player); return economy.has(player,amount)&&economy.withdrawPlayer(player,amount).transactionSuccess(); };
        ItemStack[] before=copy(player.getInventory().getStorageContents()),after=copy(before); long remaining=amount;
        for(int i=0;i<after.length&&remaining>0;i++) if(catalog.denar(after[i])) {
            int used=(int)Math.min(remaining,after[i].getAmount()); remaining-=used;
            after[i].setAmount(after[i].getAmount()-used); if(after[i].getAmount()==0) after[i]=null;
        }
        if(remaining>0) throw new IllegalArgumentException("No tenés suficientes denares físicos.");
        return replace(player,before,after);
    }
    public Transfer credit(Player player,Claim claim) {
        mainThread(player);
        if(claim.kind().equals("BANK")) return ()->{mainThread(player); return economy.depositPlayer(player,claim.amount()).transactionSuccess();};
        List<ItemStack> incoming;
        if(claim.kind().equals("CASH")) {
            ItemStack template=catalog.denarTemplate(); long left=claim.amount(); incoming=new ArrayList<>();
            if(left<=0||left>(long)player.getInventory().getStorageContents().length*template.getMaxStackSize()) throw new IllegalArgumentException("No hay espacio suficiente para esta devolución de denares.");
            while(left>0) {var stack=template.clone(); int n=(int)Math.min(left,stack.getMaxStackSize());stack.setAmount(n);incoming.add(stack);left-=n;}
        } else incoming=ItemCodec.unpack(claim.payload());
        ItemStack[] before=copy(player.getInventory().getStorageContents()),after=copy(before);
        for(ItemStack stack:incoming) {
            int left=stack.getAmount(),max=Math.min(stack.getMaxStackSize(),player.getInventory().getMaxStackSize());
            for(int i=0;i<after.length&&left>0;i++) if(after[i]!=null && after[i].isSimilar(stack)) {
                int n=Math.min(left,Math.max(0,max-after[i].getAmount())); after[i].setAmount(after[i].getAmount()+n); left-=n;
            }
            for(int i=0;i<after.length&&left>0;i++) if(after[i]==null||after[i].getType().isAir()) {
                int n=Math.min(left,max); after[i]=stack.clone();after[i].setAmount(n);left-=n;
            }
            if(left>0) throw new IllegalArgumentException("Liberá espacio suficiente: la entrega completa sigue guardada.");
        } return replace(player,before,after);
    }
    public long cashCapacity(Player player) {
        mainThread(player);ItemStack template=catalog.denarTemplate();long room=0;
        int max=Math.min(template.getMaxStackSize(),player.getInventory().getMaxStackSize());
        for(ItemStack item:player.getInventory().getStorageContents()) {
            if(item==null||item.getType().isAir())room+=max;
            else if(item.isSimilar(template))room+=Math.max(0,max-item.getAmount());
        }return room;
    }
}
