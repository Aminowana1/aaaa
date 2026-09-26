package com.mdvcraft.mdveconomy;
import java.util.*;
import org.bukkit.entity.Player;
import org.bukkit.configuration.file.FileConfiguration;

public final class MarketSettings {
    private final FileConfiguration config;
    public MarketSettings(FileConfiguration config) {
        this.config=config;
        if(config.getInt("limits.default",5)<1||config.getLong("max-bid",999999)<1||config.getLong("max-bid",999999)>Integer.MAX_VALUE
            ||config.getInt("expiration.interval-seconds",5)<1||config.getInt("expiration.batch-size",100)<1
            ||config.getInt("max-offers-per-player",5)<1) throw new IllegalArgumentException("Límites/intervalos inválidos en config.yml");
        var ranks=config.getConfigurationSection("limits.ranks");
        if(ranks!=null) for(String key:ranks.getKeys(false)) if(ranks.getInt(key+".slots",0)<1||ranks.getString(key+".permission","").isBlank()) throw new IllegalArgumentException("Rango inválido: "+key);
    }
    public int limit(Player p) {int max=config.getInt("limits.default",5);var ranks=config.getConfigurationSection("limits.ranks");
        if(ranks!=null) for(String key:ranks.getKeys(false)) if(p.hasPermission(ranks.getString(key+".permission","mdveconomy.none"))) max=Math.max(max,ranks.getInt(key+".slots",max));return max;}
    public List<Integer> hours(Player p) {var values=new ArrayList<Integer>();values.add(10);for(int h:List.of(16,24,48)) if(p.hasPermission("mdveconomy.duration."+h)) values.add(h);return values;}
    public long maxBid() {return config.getLong("max-bid",999999);}
    public int maxOffers() {return config.getInt("max-offers-per-player",5);}
    public int interval() {return config.getInt("expiration.interval-seconds",5);}
    public int batch() {return config.getInt("expiration.batch-size",100);}
}
