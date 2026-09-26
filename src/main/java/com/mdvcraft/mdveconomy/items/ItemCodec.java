package com.mdvcraft.mdveconomy.items;
import java.util.*;
import org.bukkit.inventory.ItemStack;
public final class ItemCodec {
    private ItemCodec() {}
    public static String encode(ItemStack item) { return Base64.getEncoder().encodeToString(item.serializeAsBytes()); }
    public static ItemStack decode(String data) { return ItemStack.deserializeBytes(Base64.getDecoder().decode(data)); }
    public static List<ItemStack> unpack(String data) { return data.isBlank()?List.of():Arrays.stream(data.split("\n")).map(ItemCodec::decode).toList(); }
}
