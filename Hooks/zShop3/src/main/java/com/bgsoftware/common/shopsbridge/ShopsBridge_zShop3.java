package com.bgsoftware.common.shopsbridge;

import com.bgsoftware.common.annotations.Nullable;
import fr.maxlego08.zshop.api.ShopManager;
import fr.maxlego08.zshop.api.buttons.ItemButton;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class ShopsBridge_zShop3 implements IShopsBridge {

    private final CompletableFuture<Void> readyFuture = new CompletableFuture<>();
    private final ShopManager shopManager;

    public ShopsBridge_zShop3(Plugin plugin) {
        RegisteredServiceProvider<ShopManager> provider = Bukkit.getServicesManager().getRegistration(ShopManager.class);
        this.shopManager = Objects.requireNonNull(provider == null ? null : provider.getProvider());
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> this.readyFuture.complete(null), 1L);
    }

    @Override
    public BigDecimal getSellPrice(OfflinePlayer offlinePlayer, ItemStack itemStack) {
        return Optional.ofNullable(offlinePlayer.getPlayer()).flatMap(player -> getItemButtonForItem(itemStack, player)
                .map(itemButton -> BigDecimal.valueOf(itemButton.getSellPrice()))
        ).orElseGet(() -> this.getSellPrice(itemStack));
    }

    @Override
    public BigDecimal getSellPrice(ItemStack itemStack) {
        return getItemButtonForItem(itemStack, null)
                .map(itemButton -> BigDecimal.valueOf(itemButton.getSellPrice()))
                .orElse(BigDecimal.ZERO);
    }

    @Override
    public BigDecimal getBuyPrice(OfflinePlayer offlinePlayer, ItemStack itemStack) {
        return Optional.ofNullable(offlinePlayer.getPlayer()).flatMap(player -> getItemButtonForItem(itemStack, player)
                .map(itemButton -> BigDecimal.valueOf(itemButton.getBuyPrice()))
        ).orElseGet(() -> this.getBuyPrice(itemStack));
    }

    @Override
    public BigDecimal getBuyPrice(ItemStack itemStack) {
        return getItemButtonForItem(itemStack, null)
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

    private Optional<ItemButton> getItemButtonForItem(ItemStack itemStack, @Nullable Player player) {
        return shopManager.getItemButton(player, itemStack);
    }

    private class BulkTransactionImpl implements BulkTransaction {

        private final ItemStackCache<ItemButton> cache = new ItemStackCache<>();

        @Override
        public BigDecimal getSellPrice(OfflinePlayer offlinePlayer, ItemStack itemStack) {
            return Optional.ofNullable(offlinePlayer.getPlayer()).flatMap(player -> Optional.ofNullable(
                            this.cache.computeIfAbsent(itemStack, () -> getItemButtonForItem(itemStack, player).orElse(null)))
                    .map(itemButton -> BigDecimal.valueOf(itemButton.getSellPrice()))
            ).orElseGet(() -> this.getSellPrice(itemStack));
        }

        @Override
        public BigDecimal getSellPrice(ItemStack itemStack) {
            return Optional.ofNullable(this.cache.computeIfAbsent(itemStack, () ->
                            getItemButtonForItem(itemStack, null).orElse(null)))
                    .map(itemButton -> BigDecimal.valueOf(itemButton.getSellPrice()))
                    .orElse(BigDecimal.ZERO);
        }

        @Override
        public BigDecimal getBuyPrice(OfflinePlayer offlinePlayer, ItemStack itemStack) {
            return Optional.ofNullable(offlinePlayer.getPlayer()).flatMap(player -> Optional.ofNullable(
                            this.cache.computeIfAbsent(itemStack, () -> getItemButtonForItem(itemStack, player).orElse(null)))
                    .map(itemButton -> BigDecimal.valueOf(itemButton.getBuyPrice()))
            ).orElseGet(() -> this.getBuyPrice(itemStack));
        }

        @Override
        public BigDecimal getBuyPrice(ItemStack itemStack) {
            return Optional.ofNullable(this.cache.computeIfAbsent(itemStack, () ->
                            getItemButtonForItem(itemStack, null).orElse(null)))
                    .map(itemButton -> BigDecimal.valueOf(itemButton.getBuyPrice()))
                    .orElse(BigDecimal.ZERO);
        }

    }

}
