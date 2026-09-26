package com.mdvcraft.mdveconomy.items;
import java.util.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import net.Indyuce.mmoitems.MMOItems;
import io.lumine.mythic.lib.api.item.NBTItem;

public final class ItemCatalog {
    private final FileConfiguration config;
    public ItemCatalog(FileConfiguration config) { this.config=config; }
    public boolean denar(ItemStack item) {
        return item!=null && !item.getType().isAir()
            && config.getString("cash.type","MISCELLANEOUS").equalsIgnoreCase(MMOItems.getTypeName(item))
            && config.getString("cash.id","DENAR").equalsIgnoreCase(MMOItems.getID(item));
    }
    public ItemStack denarTemplate() {
        ItemStack item=MMOItems.plugin.getItem(config.getString("cash.type","MISCELLANEOUS"),config.getString("cash.id","DENAR"));
        if(item==null||item.getType().isAir()) throw new IllegalStateException("No está configurado el DENAR de MMOItems.");
        item.setAmount(1); return item;
    }
    public String category(ItemStack item) {
        String type=MMOItems.getTypeName(item); var categories=config.getConfigurationSection("categories");
        if(categories==null) return "other";
        // MMOItems types take precedence; vanilla matching remains a configurable fallback.
        if(type!=null) for(String key:categories.getKeys(false)) {
            if(config.getStringList("categories."+key+".mmoitems-types").stream().anyMatch(type::equalsIgnoreCase)) return key;
        }
        String material=item.getType().name();
        for(String key:categories.getKeys(false)) {
            if(config.getStringList("categories."+key+".vanilla").stream().anyMatch(pattern->glob(material,pattern))) return key;
        } return "other";
    }
    static boolean glob(String value,String pattern) {
        String regex=java.util.regex.Pattern.quote(pattern.toUpperCase(Locale.ROOT)).replace("*","\\E.*\\Q");
        return value.matches(regex);
    }
    public String tier(ItemStack item) {
        String value=NBTItem.get(item).getString("MMOITEMS_TIER"); return value==null?"":value.toUpperCase(Locale.ROOT);
    }
}
