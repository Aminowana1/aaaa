package com.mdvcraft.mdveconomy.menus;

import java.io.*;
import java.util.*;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

/** Loads complete editable menu layouts; invalid reloads retain the previous definitions. */
public final class MenuFiles {
    public static final List<String> NAMES=List.of("main","browse","bid","trade","manage","owner","offers","create","participations","claims","history");
    private final JavaPlugin plugin;
    private Map<String,YamlConfiguration> menus=Map.of();
    public MenuFiles(JavaPlugin plugin) {this.plugin=plugin;}
    public void load() throws Exception {
        var next=new HashMap<String,YamlConfiguration>();
        for(String name:NAMES) {
            File file=new File(plugin.getDataFolder(),"menus/"+name+".yml");
            if(!file.exists()) plugin.saveResource("menus/"+name+".yml",false);
            var config=new YamlConfiguration(); config.load(file); validate(name,config);
            for(String key:config.getKeys(true)) if(key.endsWith(".material")) {
                Material material=Material.matchMaterial(config.getString(key,""));
                if(material==null||!material.isItem()||material.isAir())throw new IllegalArgumentException(name+": material de ítem inválido en "+key);
            }
            for(var decoration:config.getMapList("decorations")) {
                Material material=Material.matchMaterial(String.valueOf(decoration.get("material")));
                if(material==null||!material.isItem()||material.isAir())throw new IllegalArgumentException(name+": material inválido en decorations");
            }
            next.put(name,config);
        } menus=Map.copyOf(next);
    }
    public YamlConfiguration get(String name) {return Objects.requireNonNull(menus.get(name),name);}
    public static void validate(String name,YamlConfiguration c) {
        int rows=c.getInt("rows",6); if(rows<1||rows>6) throw new IllegalArgumentException(name+": rows debe ser 1..6");
        Set<Integer> used=new HashSet<>(); int size=rows*9;
        for(String key:List.of("content-slots","input-slots","head-slots","accept-slots")) for(int slot:c.getIntegerList(key)) reserve(name,used,slot,size);
        if(c.contains("preview-slot")) reserve(name,used,c.getInt("preview-slot"),size);
        for(List<?> row:c.getList("offer-slots",List.of()).stream().map(x->(List<?>)x).toList())
            for(Object slot:row) reserve(name,used,((Number)slot).intValue(),size);
        var buttons=c.getConfigurationSection("buttons"); if(buttons!=null) for(String key:buttons.getKeys(false)) {
            var b=buttons.getConfigurationSection(key); if(b==null) throw new IllegalArgumentException(name+": botón inválido");
            reserve(name,used,b.getInt("slot",-1),size); validateIcon(name,b);
            String action=b.getString("action",key);
            if(!action.startsWith("CATEGORY:")&&!Set.of("PREVIOUS","NEXT","BROWSE","MANAGE","PARTICIPATIONS","BANK","CLOSE","BACK","CANCEL_DRAFT","SORT","TIER","CREATE","TOGGLE_MODE","DURATION","PUBLISH","BID_BANK","BID_CASH","SUBMIT_TRADE","CANCEL_LISTING","CLAIM","CLAIMS","HISTORY","OFFERS","OPEN_BUY","INFO","SETTLEMENT").contains(action))throw new IllegalArgumentException(name+": acción desconocida "+action);
            if(action.equals("SETTLEMENT")&&!c.isConfigurationSection("variants.claim"))throw new IllegalArgumentException(name+": falta variants.claim");
            if(action.equals("TOGGLE_MODE")&&(!c.isConfigurationSection("variants.trade-mode")||!c.isConfigurationSection("variants.bid-mode")))throw new IllegalArgumentException(name+": faltan las variantes del modo");
        }
        if(!c.isConfigurationSection("filler"))throw new IllegalArgumentException(name+": falta filler");
        for(String key:c.getKeys(true)) if(key.endsWith(".material"))validateIcon(name,c.getConfigurationSection(key.substring(0,key.length()-9)));
        if(Set.of("browse","manage","participations","claims","history").contains(name)&&c.getIntegerList("content-slots").isEmpty()) throw new IllegalArgumentException(name+": faltan content-slots");
        if(name.equals("create")&&c.getIntegerList("input-slots").size()!=1) throw new IllegalArgumentException("create: debe haber un input-slot");
        if(name.equals("trade")&&c.getIntegerList("input-slots").size()!=5) throw new IllegalArgumentException("trade: deben haber cinco input-slots");
        if(name.equals("offers")) {
            int n=c.getIntegerList("head-slots").size(); var offerRows=c.getList("offer-slots",List.of());
            if(n<1||c.getIntegerList("accept-slots").size()!=n||offerRows.size()!=n||offerRows.stream().anyMatch(x->!(x instanceof List<?> l)||l.size()!=5)) throw new IllegalArgumentException("offers: cada fila necesita cabeza, cinco ítems y aceptar");
        }
        for(var fill:c.getMapList("decorations")) {
            if(!(fill.get("slots") instanceof List<?> slots))throw new IllegalArgumentException(name+": decorations requiere slots");
            var icon=new org.bukkit.configuration.MemoryConfiguration();fill.forEach((key,value)->icon.set(key.toString(),value));validateIcon(name,icon);
            for(Object slot:slots)reserve(name,used,((Number)slot).intValue(),size);
        }
    }
    private static void reserve(String name,Set<Integer> used,int slot,int size) {if(slot<0||slot>=size||!used.add(slot)) throw new IllegalArgumentException(name+": slot inválido/duplicado "+slot);}
    private static void validateIcon(String name,ConfigurationSection c) {
        Material m=Material.matchMaterial(c.getString("material","STONE"));
        if(m==null||Set.of("AIR","CAVE_AIR","VOID_AIR").contains(m.name())) throw new IllegalArgumentException(name+": material inválido");
        String texture=c.getString("texture",""); if(!texture.isBlank()) IconFactory.textureUrl(texture);
    }
}
