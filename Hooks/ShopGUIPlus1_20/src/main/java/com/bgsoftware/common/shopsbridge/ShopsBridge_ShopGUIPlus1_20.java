package com.bgsoftware.common.shopsbridge;

import com.bgsoftware.common.shopsbridge.internal.ItemStackCache;
import com.bgsoftware.common.shopsbridge.internal.PricesAccessorNoTransactions;
import com.bgsoftware.common.shopsbridge.internal.scheduler.Scheduler;
import net.brcdev.shopgui.ShopGuiPlugin;
import net.brcdev.shopgui.player.PlayerData;
import net.brcdev.shopgui.shop.Shop;
import net.brcdev.shopgui.shop.ShopItem;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import javax.annotation.Nullable;
import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class ShopsBridge_ShopGUIPlus1_20 implements PricesAccessorNoTransactions, IShopsBridge {

    private final CompletableFuture<Void> readyFuture = new CompletableFuture<>();
    private final ShopGuiPlugin plugin;

    public ShopsBridge_ShopGUIPlus1_20(Plugin plugin) {
        this.plugin = ShopGuiPlugin.getInstance();
        Scheduler.runTaskLater(plugin, () -> this.readyFuture.complete(null), 1L);
    }

    @Override
    public BigDecimal getSellPriceInternal(OfflinePlayer offlinePlayer, ItemStack itemStack) {
        return Optional.ofNullable(offlinePlayer.getPlayer()).flatMap(player -> findShopItem(itemStack, player).map(shopData -> {
            PlayerData playerData = ShopGuiPlugin.getInstance().getPlayerManager().getPlayerData(player);
            return BigDecimal.valueOf(shopData.shopItem.getSellPriceForAmount(shopData.shop, player, playerData, itemStack.getAmount()));
        })).orElseGet(() -> this.getSellPriceInternal(itemStack));
    }

    @Override
    public BigDecimal getSellPriceInternal(ItemStack itemStack) {
        return findShopItem(itemStack, null).map(shopData -> {
            // noinspection deprecation
            return BigDecimal.valueOf(shopData.shopItem.getSellPriceForAmount(itemStack.getAmount()));
        }).orElse(BigDecimal.ZERO);
    }

    @Override
    public BigDecimal getBuyPriceInternal(OfflinePlayer offlinePlayer, ItemStack itemStack) {
        return Optional.ofNullable(offlinePlayer.getPlayer()).flatMap(player -> findShopItem(itemStack, player).map(shopData -> {
            PlayerData playerData = ShopGuiPlugin.getInstance().getPlayerManager().getPlayerData(player);
            return BigDecimal.valueOf(shopData.shopItem.getBuyPriceForAmount(shopData.shop, player, playerData, itemStack.getAmount()));
        })).orElseGet(() -> this.getBuyPriceInternal(itemStack));
    }

    @Override
    public BigDecimal getBuyPriceInternal(ItemStack itemStack) {
        return findShopItem(itemStack, null).map(shopData -> {
            // noinspection deprecation
            return BigDecimal.valueOf(shopData.shopItem.getBuyPriceForAmount(itemStack.getAmount()));
        }).orElse(BigDecimal.ZERO);
    }

    @Override
    public CompletableFuture<Void> getWhenShopsLoaded() {
        return this.readyFuture;
    }

    @Override
    public BulkTransaction startBulkTransaction() {
        return new BulkTransactionImpl();
    }

    private Optional<ShopData> findShopItem(ItemStack itemStack, @Nullable Player player) {
        Map<String, Shop> shops = this.plugin.getShopManager().shops;
        for (Shop shop : shops.values()) {
            for (ShopItem shopItem : shop.getShopItems()) {
                if ((player == null || shop.hasAccess(player, shopItem, true)) &&
                        shopItem.getItem().isSimilar(itemStack)) {
                    return Optional.of(new ShopData(shop, shopItem));
                }
            }
        }

        return Optional.empty();
    }

    private class BulkTransactionImpl implements PricesAccessorNoTransactions, BulkTransaction {

        private final ItemStackCache<ShopData> cache = new ItemStackCache<>();

        @Override
        public BigDecimal getSellPriceInternal(OfflinePlayer offlinePlayer, ItemStack itemStack) {
            return Optional.ofNullable(offlinePlayer.getPlayer()).flatMap(player ->
                    Optional.ofNullable(this.cache.computeIfAbsent(itemStack, () -> findShopItem(itemStack, player).orElse(null))).map(shopData -> {
                        PlayerData playerData = ShopGuiPlugin.getInstance().getPlayerManager().getPlayerData(player);
                        return BigDecimal.valueOf(shopData.shopItem.getSellPriceForAmount(shopData.shop, player, playerData, itemStack.getAmount()));
                    })).orElseGet(() -> this.getSellPriceInternal(itemStack));
        }

        @Override
        public BigDecimal getSellPriceInternal(ItemStack itemStack) {
            return Optional.ofNullable(this.cache.computeIfAbsent(itemStack, () -> findShopItem(itemStack, null).orElse(null))).map(shopData -> {
                // noinspection deprecation
                return BigDecimal.valueOf(shopData.shopItem.getSellPriceForAmount(itemStack.getAmount()));
            }).orElse(BigDecimal.ZERO);
        }

        @Override
        public BigDecimal getBuyPriceInternal(OfflinePlayer offlinePlayer, ItemStack itemStack) {
            return Optional.ofNullable(offlinePlayer.getPlayer()).flatMap(player ->
                    Optional.ofNullable(this.cache.computeIfAbsent(itemStack, () -> findShopItem(itemStack, player).orElse(null))).map(shopData -> {
                        PlayerData playerData = ShopGuiPlugin.getInstance().getPlayerManager().getPlayerData(player);
                        return BigDecimal.valueOf(shopData.shopItem.getBuyPriceForAmount(shopData.shop, player, playerData, itemStack.getAmount()));
                    })).orElseGet(() -> this.getBuyPriceInternal(itemStack));
        }

        @Override
        public BigDecimal getBuyPriceInternal(ItemStack itemStack) {
            return Optional.ofNullable(this.cache.computeIfAbsent(itemStack, () -> findShopItem(itemStack, null).orElse(null))).map(shopData -> {
                // noinspection deprecation
                return BigDecimal.valueOf(shopData.shopItem.getBuyPriceForAmount(itemStack.getAmount()));
            }).orElse(BigDecimal.ZERO);
        }

    }

    private static class ShopData {

        private final Shop shop;
        private final ShopItem shopItem;

        ShopData(Shop shop, ShopItem shopItem) {
            this.shopItem = shopItem;
            this.shop = shop;
        }

    }

}
