package com.bgsoftware.common.shopsbridge;

import com.bgsoftware.common.shopsbridge.internal.ItemStackCache;
import com.bgsoftware.common.shopsbridge.internal.PricesAccessorNoTransactions;
import com.bgsoftware.common.shopsbridge.internal.scheduler.Scheduler;
import fr.maxlego08.shop.api.ShopManager;
import fr.maxlego08.shop.api.button.buttons.ItemButton;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class ShopsBridge_zShop implements PricesAccessorNoTransactions, IShopsBridge {

    private final CompletableFuture<Void> readyFuture = new CompletableFuture<>();
    private final ShopManager shopManager;

    public ShopsBridge_zShop(Plugin plugin) {
        RegisteredServiceProvider<ShopManager> provider = Bukkit.getServicesManager().getRegistration(ShopManager.class);
        this.shopManager = Objects.requireNonNull(provider == null ? null : provider.getProvider());
        Scheduler.runTaskLater(plugin, () -> this.readyFuture.complete(null), 1L);
    }

    @Override
    public BigDecimal getSellPriceInternal(OfflinePlayer offlinePlayer, ItemStack itemStack) {
        return Optional.ofNullable(offlinePlayer.getPlayer()).flatMap(player -> getItemButtonForItem(itemStack)
                .map(itemButton -> BigDecimal.valueOf(itemButton.getSellPrice(player)))
        ).orElseGet(() -> this.getSellPriceInternal(itemStack));
    }

    @Override
    public BigDecimal getSellPriceInternal(ItemStack itemStack) {
        return getItemButtonForItem(itemStack)
                .map(itemButton -> BigDecimal.valueOf(itemButton.getSellPrice()))
                .orElse(BigDecimal.ZERO);
    }

    @Override
    public BigDecimal getBuyPriceInternal(OfflinePlayer offlinePlayer, ItemStack itemStack) {
        return Optional.ofNullable(offlinePlayer.getPlayer()).flatMap(player -> getItemButtonForItem(itemStack)
                .map(itemButton -> BigDecimal.valueOf(itemButton.getBuyPrice(player)))
        ).orElseGet(() -> this.getBuyPriceInternal(itemStack));
    }

    @Override
    public BigDecimal getBuyPriceInternal(ItemStack itemStack) {
        return getItemButtonForItem(itemStack)
                .map(itemButton -> BigDecimal.valueOf(itemButton.getBuyPrice()))
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

    private Optional<ItemButton> getItemButtonForItem(ItemStack itemStack) {
        return shopManager.getItemButton(itemStack);
    }

    private class BulkTransactionImpl implements PricesAccessorNoTransactions, BulkTransaction {

        private final ItemStackCache<ItemButton> cache = new ItemStackCache<>();

        @Override
        public BigDecimal getSellPriceInternal(OfflinePlayer offlinePlayer, ItemStack itemStack) {
            return Optional.ofNullable(offlinePlayer.getPlayer()).flatMap(player -> Optional.ofNullable(
                            this.cache.computeIfAbsent(itemStack, () -> getItemButtonForItem(itemStack).orElse(null)))
                    .map(itemButton -> BigDecimal.valueOf(itemButton.getSellPrice(player)))
            ).orElseGet(() -> this.getSellPriceInternal(itemStack));
        }

        @Override
        public BigDecimal getSellPriceInternal(ItemStack itemStack) {
            return Optional.ofNullable(this.cache.computeIfAbsent(itemStack, () -> getItemButtonForItem(itemStack).orElse(null)))
                    .map(itemButton -> BigDecimal.valueOf(itemButton.getSellPrice()))
                    .orElse(BigDecimal.ZERO);
        }

        @Override
        public BigDecimal getBuyPriceInternal(OfflinePlayer offlinePlayer, ItemStack itemStack) {
            return Optional.ofNullable(offlinePlayer.getPlayer()).flatMap(player -> Optional.ofNullable(
                            this.cache.computeIfAbsent(itemStack, () -> getItemButtonForItem(itemStack).orElse(null)))
                    .map(itemButton -> BigDecimal.valueOf(itemButton.getBuyPrice(player)))
            ).orElseGet(() -> this.getBuyPriceInternal(itemStack));
        }

        @Override
        public BigDecimal getBuyPriceInternal(ItemStack itemStack) {
            return Optional.ofNullable(this.cache.computeIfAbsent(itemStack, () -> getItemButtonForItem(itemStack).orElse(null)))
                    .map(itemButton -> BigDecimal.valueOf(itemButton.getBuyPrice()))
                    .orElse(BigDecimal.ZERO);
        }

    }

}
