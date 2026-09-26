package com.mdvcraft.mdveconomy.menus;

import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import com.google.gson.JsonParser;
import org.bukkit.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

public final class IconFactory {
    private static final LegacyComponentSerializer TEXT=LegacyComponentSerializer.legacyAmpersand();
    private IconFactory() {}
    public static Component text(String value) {return TEXT.deserialize(value.replace('§','&')).decoration(TextDecoration.ITALIC,false);}
    public static String replace(String value,Map<String,String> placeholders) {
        for(var e:placeholders.entrySet()) value=value.replace("{"+e.getKey()+"}",e.getValue()); return value;
    }
    public static ItemStack create(ConfigurationSection c,Map<String,String> p,boolean glow) {
        String texture=c.getString("texture","");
        Material material=texture.isBlank()?Material.matchMaterial(c.getString("material","STONE")):Material.PLAYER_HEAD;
        ItemStack item=new ItemStack(Objects.requireNonNull(material)); var meta=item.getItemMeta();
        meta.displayName(text(replace(c.getString("name"," "),p)));
        meta.lore(c.getStringList("lore").stream().map(s->text(replace(s,p))).toList());
        meta.setEnchantmentGlintOverride(glow||c.getBoolean("glow",false));
        if(c.contains("custom-model-data")) meta.setCustomModelData(c.getInt("custom-model-data"));
        if(!texture.isBlank()&&meta instanceof SkullMeta skull) {
            var profile=Bukkit.createPlayerProfile(UUID.nameUUIDFromBytes(texture.getBytes(StandardCharsets.UTF_8)));
            var textures=profile.getTextures();textures.setSkin(textureUrl(texture));profile.setTextures(textures);skull.setOwnerProfile(profile);
        }
        item.setItemMeta(meta);return item;
    }
    public static URL textureUrl(String base64) {
        try {
            String json=new String(Base64.getDecoder().decode(base64),StandardCharsets.UTF_8);
            String url=JsonParser.parseString(json).getAsJsonObject().getAsJsonObject("textures").getAsJsonObject("SKIN").get("url").getAsString();
            URI uri=URI.create(url);
            if(!Set.of("http","https").contains(uri.getScheme())||!"textures.minecraft.net".equalsIgnoreCase(uri.getHost())) throw new IllegalArgumentException("La textura debe usar textures.minecraft.net");
            return uri.toURL();
        } catch(Exception e) {throw new IllegalArgumentException("Textura Base64 inválida",e);}
    }
}
