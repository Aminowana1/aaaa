package com.mdvcraft.mdveconomy;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.logging.Level;
import org.bukkit.*;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import net.milkbowl.vault.economy.Economy;
import com.mdvcraft.mdveconomy.commands.CommandRegistration;
import com.mdvcraft.mdveconomy.items.*;
import com.mdvcraft.mdveconomy.market.*;
import com.mdvcraft.mdveconomy.menus.*;

public final class MDVEconomyPlugin extends JavaPlugin {
    private MarketStore store;
    private MarketEngine engine;
    private ItemCatalog catalog;
    private InventoryTransfers transfers;
    private MarketSettings settings;
    private MarketMenus menus;
    private MenuFiles menuFiles;
    private YamlConfiguration messages;
    private BukkitTask timer;
    private Economy economy;
    @Override public void onEnable() {
        try {
            Files.createDirectories(getDataFolder().toPath());
            File configFile=new File(getDataFolder(),"config.yml");
            if(configFile.exists()&&YamlConfiguration.loadConfiguration(configFile).getInt("config-version",1)<2) {
                Files.copy(configFile.toPath(),getDataFolder().toPath().resolve("config-v1-backup-"+System.currentTimeMillis()+".yml"));
                saveResource("config.yml",true);
            }
            saveDefaultConfig();
            var provider=getServer().getServicesManager().getRegistration(Economy.class);
            if(provider==null)throw new IllegalStateException("Instalá un proveedor de economía compatible con Vault.");
            economy=provider.getProvider();
            reloadMarket();
            store=new MarketStore(getDataFolder().toPath().resolve("database.db"));engine=new MarketEngine(store);
            if(store.number("SELECT count(*) FROM market_meta WHERE key='legacy-import'")==0) {
                Path backup=getDataFolder().toPath().resolve("database-before-v2-"+System.currentTimeMillis()+".db");
                store.execute("VACUUM INTO ?",backup.toAbsolutePath().toString());
                engine.migrateLegacy();
            }
            engine.expire(settings.batch());
            CommandRegistration.register(this);
            getServer().getPluginManager().registerEvents(new MenuListener(this),this);schedule();
            if(!engine.reviews().isEmpty())getLogger().severe("Hay operaciones inciertas: /mdveconomy revisiones. Revisar antes de resolver.");
            getLogger().info("MDVEconomy 2: pujas y trueques habilitados.");
        } catch(Exception e) {getLogger().log(Level.SEVERE,"No se pudo iniciar MDVEconomy",e);getServer().getPluginManager().disablePlugin(this);}
    }
    private void reloadMarket() throws Exception {
        var candidate=new YamlConfiguration();candidate.load(new File(getDataFolder(),"config.yml"));
        var nextSettings=new MarketSettings(candidate);var nextFiles=new MenuFiles(this);nextFiles.load();
        File msg=new File(getDataFolder(),"messages-v2.yml");if(!msg.exists())saveResource("messages-v2.yml",false);
        var nextMessages=new YamlConfiguration();nextMessages.load(msg);
        try(var reader=new InputStreamReader(Objects.requireNonNull(getResource("messages-v2.yml")),java.nio.charset.StandardCharsets.UTF_8)) {nextMessages.setDefaults(YamlConfiguration.loadConfiguration(reader));}
        // All checks finish before replacing the live configuration and closing old layouts.
        reloadConfig();settings=nextSettings;catalog=new ItemCatalog(candidate);transfers=new InventoryTransfers(economy,catalog);
        menuFiles=nextFiles;messages=nextMessages;menus=new MarketMenus(this,menuFiles);
        for(Player p:Bukkit.getOnlinePlayers())if(p.getOpenInventory().getTopInventory().getHolder() instanceof MarketView)p.closeInventory();
    }
    public void reloadMarketConfiguration() throws Exception {
        reloadMarket();
        schedule();
    }
    private void schedule() {
        if(timer!=null)timer.cancel();long ticks=settings.interval()*20L;
        timer=getServer().getScheduler().runTaskTimer(this,()-> {try {engine.expire(settings.batch());}
            catch(Exception e) {getLogger().log(Level.SEVERE,"No se pudieron cerrar subastas vencidas",e);}},ticks,ticks);
    }
    @Override public void onDisable() {
        if(timer!=null)timer.cancel();
        for(Player p:Bukkit.getOnlinePlayers())if(p.getOpenInventory().getTopInventory().getHolder() instanceof MarketView)p.closeInventory();
        if(store!=null)try {store.close();}catch(Exception e){getLogger().log(Level.SEVERE,"Error cerrando SQLite",e);}
    }
    public void message(CommandSender player,String key) {player.sendMessage(IconFactory.text(messages.getString(key,key)));}
    public void failure(CommandSender player,Exception e) {
        player.sendMessage(IconFactory.text("&c"+(e.getMessage()==null?"No se pudo completar la operación.":e.getMessage())));
        if(!(e instanceof IllegalArgumentException))getLogger().log(Level.SEVERE,"Error de mercado",e);
    }
    public MarketEngine engine(){return engine;} public ItemCatalog catalog(){return catalog;}
    public InventoryTransfers transfers(){return transfers;} public MarketSettings settings(){return settings;}
    public MarketMenus menus(){return menus;}
}
