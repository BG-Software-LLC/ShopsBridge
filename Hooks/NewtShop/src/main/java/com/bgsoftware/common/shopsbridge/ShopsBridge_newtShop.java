package com.bgsoftware.common.shopsbridge;

import com.bgsoftware.common.shopsbridge.internal.ItemStackCache;
import com.bgsoftware.common.shopsbridge.internal.PricesAccessorNoTransactions;
import com.bgsoftware.common.shopsbridge.internal.scheduler.Scheduler;
import com.newtjam.newtShop.newtShop;
import com.newtjam.newtShop.structure.Item;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.math.BigDecimal;
import java.util.concurrent.CompletableFuture;

public class ShopsBridge_newtShop implements PricesAccessorNoTransactions, IShopsBridge {

    private final CompletableFuture<Void> readyFuture = new CompletableFuture<>();

    public ShopsBridge_newtShop(Plugin plugin) {
        Scheduler.runTaskLater(plugin, () -> this.readyFuture.complete(null), 1L);
    }

    @Override
    public BigDecimal getSellPriceInternal(OfflinePlayer unused, ItemStack itemStack) {
        return this.getSellPriceInternal(itemStack);
    }

    @Override
    public BigDecimal getSellPriceInternal(ItemStack itemStack) {
        return BigDecimal.valueOf(newtShop.shop.getItemFromItemStack(itemStack).getSellPrice() * itemStack.getAmount());
    }

    @Override
    public BigDecimal getBuyPriceInternal(OfflinePlayer unused, ItemStack itemStack) {
        return this.getBuyPriceInternal(itemStack);
    }

    @Override
    public BigDecimal getBuyPriceInternal(ItemStack itemStack) {
        return BigDecimal.valueOf(newtShop.shop.getItemFromItemStack(itemStack).getBuyPrice() * itemStack.getAmount());
    }

    @Override
    public CompletableFuture<Void> getWhenShopsLoaded() {
        return this.readyFuture;
    }

    @Override
    public BulkTransaction startBulkTransaction() {
        return new BulkTransactionImpl();
    }

    private static class BulkTransactionImpl implements PricesAccessorNoTransactions, BulkTransaction {

        private final ItemStackCache<Item> cache = new ItemStackCache<>();

        @Override
        public BigDecimal getSellPriceInternal(OfflinePlayer unused, ItemStack itemStack) {
            return this.getSellPriceInternal(itemStack);
        }

        @Override
        public BigDecimal getSellPriceInternal(ItemStack itemStack) {
            return BigDecimal.valueOf(this.cache.computeIfAbsent(itemStack, () ->
                    newtShop.shop.getItemFromItemStack(itemStack)).getSellPrice() * itemStack.getAmount());
        }

        @Override
        public BigDecimal getBuyPriceInternal(OfflinePlayer unused, ItemStack itemStack) {
            return this.getBuyPriceInternal(itemStack);
        }

        @Override
        public BigDecimal getBuyPriceInternal(ItemStack itemStack) {
            return BigDecimal.valueOf(this.cache.computeIfAbsent(itemStack, () ->
                    newtShop.shop.getItemFromItemStack(itemStack)).getBuyPrice() * itemStack.getAmount());
        }

    }

}
