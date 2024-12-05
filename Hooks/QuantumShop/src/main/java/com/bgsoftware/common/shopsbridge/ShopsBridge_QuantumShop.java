package com.bgsoftware.common.shopsbridge;

import com.bgsoftware.common.shopsbridge.internal.ItemStackCache;
import com.bgsoftware.common.shopsbridge.internal.PricesAccessorNoTransactions;
import com.bgsoftware.common.shopsbridge.internal.scheduler.Scheduler;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import su.nightexpress.quantumshop.QuantumShop;
import su.nightexpress.quantumshop.modules.list.gui.GUIShop;
import su.nightexpress.quantumshop.modules.list.gui.objects.ShopGUI;
import su.nightexpress.quantumshop.modules.list.gui.objects.ShopProduct;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class ShopsBridge_QuantumShop implements PricesAccessorNoTransactions, IShopsBridge {

    private final CompletableFuture<Void> readyFuture = new CompletableFuture<>();
    private final GUIShop guiShop;

    public ShopsBridge_QuantumShop(Plugin plugin) {
        this.guiShop = QuantumShop.instance.getGUIShop();
        Scheduler.runTaskLater(plugin, () -> this.readyFuture.complete(null), 1L);
    }

    @Override
    public BigDecimal getSellPriceInternal(OfflinePlayer unused, ItemStack itemStack) {
        return this.getSellPriceInternal(itemStack);
    }

    @Override
    public BigDecimal getSellPriceInternal(ItemStack itemStack) {
        return getShopProduct(itemStack)
                .map(shopProduct -> BigDecimal.valueOf(shopProduct.getSellPrice()))
                .orElse(BigDecimal.ZERO);
    }

    @Override
    public BigDecimal getBuyPriceInternal(OfflinePlayer unused, ItemStack itemStack) {
        return this.getBuyPriceInternal(itemStack);
    }

    @Override
    public BigDecimal getBuyPriceInternal(ItemStack itemStack) {
        return getShopProduct(itemStack)
                .map(shopProduct -> BigDecimal.valueOf(shopProduct.getBuyPrice(true)))
                .orElse(BigDecimal.ZERO);
    }

    @Override
    public CompletableFuture<Void> getWhenShopsLoaded() {
        return this.readyFuture;
    }

    @Override
    public BulkTransaction startBulkTransaction() {
        return new BulkTransactionImpl();
    }

    private Optional<ShopProduct> getShopProduct(ItemStack itemStack) {
        for (ShopGUI shop : this.guiShop.getShops()) {
            for (ShopProduct shopProduct : shop.getProducts().values()) {
                if (isValidItem(shopProduct, itemStack))
                    return Optional.of(shopProduct);
            }
        }

        return Optional.empty();
    }

    private boolean isValidItem(ShopProduct product, ItemStack itemStack) {
        ItemStack check = product.getBuyItem();
        if (guiShop.generalIgnoreMetaSell) {
            return check.getType() == itemStack.getType();
        } else {
            return check.isSimilar(itemStack);
        }
    }

    private class BulkTransactionImpl implements PricesAccessorNoTransactions, BulkTransaction {

        private final ItemStackCache<ShopProduct> cache = new ItemStackCache<>();

        @Override
        public BigDecimal getSellPriceInternal(OfflinePlayer unused, ItemStack itemStack) {
            return this.getSellPriceInternal(itemStack);
        }

        @Override
        public BigDecimal getSellPriceInternal(ItemStack itemStack) {
            return Optional.ofNullable(this.cache.computeIfAbsent(itemStack, () -> getShopProduct(itemStack).orElse(null)))
                    .map(shopProduct -> BigDecimal.valueOf(shopProduct.getSellPrice()))
                    .orElse(BigDecimal.ZERO);
        }

        @Override
        public BigDecimal getBuyPriceInternal(OfflinePlayer unused, ItemStack itemStack) {
            return this.getBuyPriceInternal(itemStack);
        }

        @Override
        public BigDecimal getBuyPriceInternal(ItemStack itemStack) {
            return Optional.ofNullable(this.cache.computeIfAbsent(itemStack, () -> getShopProduct(itemStack).orElse(null)))
                    .map(shopProduct -> BigDecimal.valueOf(shopProduct.getBuyPrice(true)))
                    .orElse(BigDecimal.ZERO);
        }

    }

}
