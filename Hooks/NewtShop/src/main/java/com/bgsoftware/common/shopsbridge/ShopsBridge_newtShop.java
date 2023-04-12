package com.bgsoftware.common.shopsbridge;

import com.newtjam.newtShop.newtShop;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.math.BigDecimal;
import java.util.concurrent.CompletableFuture;

public class ShopsBridge_newtShop implements IShopsBridge {

    private final CompletableFuture<Void> readyFuture = new CompletableFuture<>();

    public ShopsBridge_newtShop(Plugin plugin) {
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> this.readyFuture.complete(null), 1L);
    }

    @Override
    public BigDecimal getSellPrice(OfflinePlayer unused, ItemStack itemStack) {
        return this.getSellPrice(itemStack);
    }

    @Override
    public BigDecimal getSellPrice(ItemStack itemStack) {
        return BigDecimal.valueOf(newtShop.shop.getItemFromItemStack(itemStack).getSellPrice() * itemStack.getAmount());
    }

    @Override
    public BigDecimal getBuyPrice(OfflinePlayer unused, ItemStack itemStack) {
        return this.getBuyPrice(itemStack);
    }

    @Override
    public BigDecimal getBuyPrice(ItemStack itemStack) {
        return BigDecimal.valueOf(newtShop.shop.getItemFromItemStack(itemStack).getBuyPrice() * itemStack.getAmount());
    }

    @Override
    public CompletableFuture<Void> getWhenShopsLoaded() {
        return this.readyFuture;
    }

}
