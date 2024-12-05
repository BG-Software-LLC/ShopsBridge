package com.bgsoftware.common.shopsbridge;

import com.bgsoftware.common.shopsbridge.internal.ItemStackCache;
import com.bgsoftware.common.shopsbridge.internal.PricesAccessorNoTransactions;
import net.brcdev.shopgui.ShopGuiPlugin;
import net.brcdev.shopgui.event.ShopsPostLoadEvent;
import net.brcdev.shopgui.shop.ShopItem;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import javax.annotation.Nullable;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class ShopsBridge_ShopGUIPlus1_43 implements PricesAccessorNoTransactions, IShopsBridge {

    private final CompletableFuture<Void> readyFuture = new CompletableFuture<>();
    private final ShopGuiPlugin plugin;

    public ShopsBridge_ShopGUIPlus1_43(Plugin plugin) {
        this.plugin = ShopGuiPlugin.getInstance();
        plugin.getServer().getPluginManager().registerEvents(new Listener() {
            @EventHandler
            public void onShopsLoad(ShopsPostLoadEvent event) {
                readyFuture.complete(null);
            }
        }, plugin);
    }

    @Override
    public BigDecimal getSellPriceInternal(OfflinePlayer offlinePlayer, ItemStack itemStack) {
        return Optional.ofNullable(offlinePlayer.getPlayer()).flatMap(player -> findShopItem(itemStack, player)
                .map(shopItem -> BigDecimal.valueOf(shopItem.getSellPriceForAmount(player, itemStack.getAmount())))
        ).orElseGet(() -> this.getSellPriceInternal(itemStack));
    }

    @Override
    public BigDecimal getSellPriceInternal(ItemStack itemStack) {
        return findShopItem(itemStack, null)
                .map(shopItem -> BigDecimal.valueOf(shopItem.getSellPriceForAmount(itemStack.getAmount())))
                .orElse(BigDecimal.ZERO);
    }

    @Override
    public BigDecimal getBuyPriceInternal(OfflinePlayer offlinePlayer, ItemStack itemStack) {
        return Optional.ofNullable(offlinePlayer.getPlayer()).flatMap(player -> findShopItem(itemStack, player)
                .map(shopItem -> BigDecimal.valueOf(shopItem.getBuyPriceForAmount(player, itemStack.getAmount())))
        ).orElseGet(() -> this.getBuyPriceInternal(itemStack));
    }

    @Override
    public BigDecimal getBuyPriceInternal(ItemStack itemStack) {
        return findShopItem(itemStack, null)
                .map(shopItem -> BigDecimal.valueOf(shopItem.getBuyPriceForAmount(itemStack.getAmount())))
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

    private Optional<ShopItem> findShopItem(ItemStack itemStack, @Nullable Player player) {
        if (player == null) {
            return Optional.ofNullable(this.plugin.getShopManager().findShopItemByItemStack(itemStack, false));
        } else {
            return Optional.ofNullable(this.plugin.getShopManager().findShopItemByItemStack(player, itemStack, false));
        }
    }

    private class BulkTransactionImpl implements PricesAccessorNoTransactions, BulkTransaction {

        private final ItemStackCache<ShopItem> cache = new ItemStackCache<>();

        @Override
        public BigDecimal getSellPriceInternal(OfflinePlayer offlinePlayer, ItemStack itemStack) {
            return Optional.ofNullable(offlinePlayer.getPlayer()).flatMap(player ->
                    Optional.ofNullable(this.cache.computeIfAbsent(itemStack, () -> findShopItem(itemStack, player).orElse(null)))
                            .map(shopItem -> BigDecimal.valueOf(shopItem.getSellPriceForAmount(player, itemStack.getAmount())))
            ).orElseGet(() -> this.getSellPriceInternal(itemStack));
        }

        @Override
        public BigDecimal getSellPriceInternal(ItemStack itemStack) {
            return Optional.ofNullable(this.cache.computeIfAbsent(itemStack, () -> findShopItem(itemStack, null).orElse(null)))
                    .map(shopItem -> BigDecimal.valueOf(shopItem.getSellPriceForAmount(itemStack.getAmount())))
                    .orElse(BigDecimal.ZERO);
        }

        @Override
        public BigDecimal getBuyPriceInternal(OfflinePlayer offlinePlayer, ItemStack itemStack) {
            return Optional.ofNullable(offlinePlayer.getPlayer()).flatMap(player ->
                    Optional.ofNullable(this.cache.computeIfAbsent(itemStack, () -> findShopItem(itemStack, player).orElse(null)))
                            .map(shopItem -> BigDecimal.valueOf(shopItem.getBuyPriceForAmount(player, itemStack.getAmount())))
            ).orElseGet(() -> this.getBuyPriceInternal(itemStack));
        }

        @Override
        public BigDecimal getBuyPriceInternal(ItemStack itemStack) {
            return Optional.ofNullable(this.cache.computeIfAbsent(itemStack, () -> findShopItem(itemStack, null).orElse(null)))
                    .map(shopItem -> BigDecimal.valueOf(shopItem.getBuyPriceForAmount(itemStack.getAmount())))
                    .orElse(BigDecimal.ZERO);
        }

    }

}
