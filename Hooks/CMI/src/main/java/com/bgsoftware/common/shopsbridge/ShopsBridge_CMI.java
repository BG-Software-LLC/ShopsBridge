package com.bgsoftware.common.shopsbridge;

import com.Zrips.CMI.CMI;
import com.Zrips.CMI.Modules.Worth.WorthItem;
import com.bgsoftware.common.shopsbridge.internal.ItemStackCache;
import com.bgsoftware.common.shopsbridge.internal.PricesAccessorNoTransactions;
import com.bgsoftware.common.shopsbridge.internal.scheduler.Scheduler;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.math.BigDecimal;
import java.util.concurrent.CompletableFuture;

public class ShopsBridge_CMI implements PricesAccessorNoTransactions, IShopsBridge {

    private final CompletableFuture<Void> readyFuture = new CompletableFuture<>();

    public ShopsBridge_CMI(Plugin plugin) {
        Scheduler.runTaskLater(plugin, () -> this.readyFuture.complete(null), 1L);
    }

    @Override
    public BigDecimal getSellPriceInternal(OfflinePlayer unused, ItemStack itemStack) {
        return this.getSellPriceInternal(itemStack);
    }

    @Override
    public BigDecimal getSellPriceInternal(ItemStack itemStack) {
        WorthItem worth = CMI.getInstance().getWorthManager().getWorth(itemStack);
        return worth == null ? BigDecimal.ZERO : BigDecimal.valueOf(worth.getSellPrice() * itemStack.getAmount());
    }

    @Override
    public BigDecimal getBuyPriceInternal(OfflinePlayer unused, ItemStack itemStack) {
        return this.getBuyPriceInternal(itemStack);
    }

    @Override
    public BigDecimal getBuyPriceInternal(ItemStack itemStack) {
        WorthItem worth = CMI.getInstance().getWorthManager().getWorth(itemStack);
        return worth == null ? BigDecimal.ZERO : BigDecimal.valueOf(worth.getBuyPrice() * itemStack.getAmount());
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

        private final ItemStackCache<WorthItem> cache = new ItemStackCache<>();

        @Override
        public BigDecimal getSellPriceInternal(OfflinePlayer unused, ItemStack itemStack) {
            return this.getSellPriceInternal(itemStack);
        }

        @Override
        public BigDecimal getSellPriceInternal(ItemStack itemStack) {
            WorthItem worth = this.cache.computeIfAbsent(itemStack, () -> CMI.getInstance().getWorthManager().getWorth(itemStack));
            return worth == null ? BigDecimal.ZERO : BigDecimal.valueOf(worth.getSellPrice() * itemStack.getAmount());
        }

        @Override
        public BigDecimal getBuyPriceInternal(OfflinePlayer unused, ItemStack itemStack) {
            return this.getBuyPriceInternal(itemStack);
        }

        @Override
        public BigDecimal getBuyPriceInternal(ItemStack itemStack) {
            WorthItem worth = this.cache.computeIfAbsent(itemStack, () -> CMI.getInstance().getWorthManager().getWorth(itemStack));
            return worth == null ? BigDecimal.ZERO : BigDecimal.valueOf(worth.getBuyPrice() * itemStack.getAmount());
        }

    }

}
