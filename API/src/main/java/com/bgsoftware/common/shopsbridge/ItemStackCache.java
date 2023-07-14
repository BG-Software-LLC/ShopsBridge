package com.bgsoftware.common.shopsbridge;

import org.bukkit.inventory.ItemStack;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

public class ItemStackCache<V> {

    private final Map<ItemStack, V> cache = new LinkedHashMap<>();

    public ItemStackCache() {

    }

    public V computeIfAbsent(ItemStack itemStack, Supplier<V> absentValue) {
        return this.cache.computeIfAbsent(getItemStackKey(itemStack), _unused -> absentValue.get());
    }

    private static ItemStack getItemStackKey(ItemStack itemStack) {
        ItemStack itemKey = itemStack.clone();
        itemKey.setAmount(1);
        return itemKey;
    }

}
