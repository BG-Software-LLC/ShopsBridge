package com.bgsoftware.common.shopsbridge;

import com.earth2me.essentials.Essentials;
import com.earth2me.essentials.Worth;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.math.BigDecimal;
import java.util.concurrent.CompletableFuture;

public class ShopsBridge_Essentials2_15 implements IShopsBridge {

    private final CompletableFuture<Void> readyFuture = new CompletableFuture<>();
    private final Essentials plugin;


    public ShopsBridge_Essentials2_15(Plugin plugin) {
        this.plugin = Essentials.getPlugin(Essentials.class);
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> this.readyFuture.complete(null), 1L);
    }

    @Override
    public BigDecimal getSellPrice(OfflinePlayer unused, ItemStack itemStack) {
        return this.getSellPrice(itemStack);
    }

    @Override
    public BigDecimal getSellPrice(ItemStack itemStack) {
        Worth worth = plugin.getWorth();
        BigDecimal price = worth.getPrice(itemStack);
        return price == null ? BigDecimal.ZERO : price.multiply(BigDecimal.valueOf(itemStack.getAmount()));
    }

    @Override
    public BigDecimal getBuyPrice(OfflinePlayer unused, ItemStack itemStack) {
        return this.getBuyPrice(itemStack);
    }

    @Override
    public BigDecimal getBuyPrice(ItemStack itemStack) {
        // No buy price, we'll use sell price instead.
        return this.getSellPrice(itemStack);
    }

    @Override
    public CompletableFuture<Void> getWhenShopsLoaded() {
        return this.readyFuture;
    }

}
