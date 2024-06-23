package com.bgsoftware.common.shopsbridge;

import com.bgsoftware.common.shopsbridge.internal.scheduler.Scheduler;
import com.muhammaddaffa.nextgens.NextGens;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class ShopsBridge_NextGens implements IShopsBridge {

    private final CompletableFuture<Void> readyFuture = new CompletableFuture<>();


    public ShopsBridge_NextGens(Plugin plugin) {
        Scheduler.runTaskLater(plugin, () -> this.readyFuture.complete(null), 1L);
    }

    @Override
    public BigDecimal getSellPrice(OfflinePlayer unused, ItemStack itemStack) {
        return this.getSellPrice(itemStack);
    }

    @Override
    public BigDecimal getSellPrice(ItemStack itemStack) {
        BigDecimal price = Optional.ofNullable(NextGens.getApi().getWorth(itemStack))
                .map(BigDecimal::valueOf).orElse(null);
        return price == null ? BigDecimal.ZERO : itemStack.getAmount() <= 1 ? price :
                price.multiply(BigDecimal.valueOf(itemStack.getAmount()));
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
