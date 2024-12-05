package com.bgsoftware.common.shopsbridge;

import com.bgsoftware.common.shopsbridge.internal.PricesAccessorNoTransactions;
import com.bgsoftware.common.shopsbridge.internal.scheduler.Scheduler;
import com.pablo67340.guishop.Main;
import com.pablo67340.guishop.definition.Price;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.math.BigDecimal;
import java.util.concurrent.CompletableFuture;

public class ShopsBridge_GUIShop implements PricesAccessorNoTransactions, IShopsBridge {

    private final CompletableFuture<Void> readyFuture = new CompletableFuture<>();
    private final Main plugin;

    public ShopsBridge_GUIShop(Plugin plugin) {
        this.plugin = Main.getINSTANCE();
        Scheduler.runTaskLater(plugin, () -> this.readyFuture.complete(null), 1L);
    }

    @Override
    public BigDecimal getSellPriceInternal(OfflinePlayer unused, ItemStack itemStack) {
        return this.getSellPriceInternal(itemStack);
    }

    @Override
    public BigDecimal getSellPriceInternal(ItemStack itemStack) {
        return BigDecimal.valueOf(getPrice(itemStack).getSellPrice() * itemStack.getAmount());
    }

    @Override
    public BigDecimal getBuyPriceInternal(OfflinePlayer unused, ItemStack itemStack) {
        return this.getBuyPriceInternal(itemStack);
    }

    @Override
    public BigDecimal getBuyPriceInternal(ItemStack itemStack) {
        return BigDecimal.valueOf(getPrice(itemStack).getBuyPrice() * itemStack.getAmount());
    }

    @Override
    public CompletableFuture<Void> getWhenShopsLoaded() {
        return this.readyFuture;
    }

    private Price getPrice(ItemStack itemStack) {
        return plugin.getPRICETABLE().get(itemStack.getType().toString());
    }

}
