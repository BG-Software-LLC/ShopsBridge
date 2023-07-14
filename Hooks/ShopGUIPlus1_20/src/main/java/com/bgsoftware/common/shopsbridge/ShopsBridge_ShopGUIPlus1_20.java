package com.bgsoftware.common.shopsbridge;

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

public class ShopsBridge_ShopGUIPlus1_20 implements IShopsBridge {

    private final CompletableFuture<Void> readyFuture = new CompletableFuture<>();
    private final ShopGuiPlugin plugin;

    public ShopsBridge_ShopGUIPlus1_20(Plugin plugin) {
        this.plugin = ShopGuiPlugin.getInstance();
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> this.readyFuture.complete(null), 1L);
    }

    @Override
    public BigDecimal getSellPrice(OfflinePlayer offlinePlayer, ItemStack itemStack) {
        return Optional.ofNullable(offlinePlayer.getPlayer()).flatMap(player -> findShopItem(itemStack, player).map(shopData -> {
            PlayerData playerData = ShopGuiPlugin.getInstance().getPlayerManager().getPlayerData(player);
            return BigDecimal.valueOf(shopData.shopItem.getSellPriceForAmount(shopData.shop, player, playerData, itemStack.getAmount()));
        })).orElseGet(() -> this.getSellPrice(itemStack));
    }

    @Override
    public BigDecimal getSellPrice(ItemStack itemStack) {
        return findShopItem(itemStack, null).map(shopData -> {
            // noinspection deprecation
            return BigDecimal.valueOf(shopData.shopItem.getSellPriceForAmount(itemStack.getAmount()));
        }).orElse(BigDecimal.ZERO);
    }

    @Override
    public BigDecimal getBuyPrice(OfflinePlayer offlinePlayer, ItemStack itemStack) {
        return Optional.ofNullable(offlinePlayer.getPlayer()).flatMap(player -> findShopItem(itemStack, player).map(shopData -> {
            PlayerData playerData = ShopGuiPlugin.getInstance().getPlayerManager().getPlayerData(player);
            return BigDecimal.valueOf(shopData.shopItem.getBuyPriceForAmount(shopData.shop, player, playerData, itemStack.getAmount()));
        })).orElseGet(() -> this.getBuyPrice(itemStack));
    }

    @Override
    public BigDecimal getBuyPrice(ItemStack itemStack) {
        return findShopItem(itemStack, null).map(shopData -> {
            // noinspection deprecation
            return BigDecimal.valueOf(shopData.shopItem.getBuyPriceForAmount(itemStack.getAmount()));
        }).orElse(BigDecimal.ZERO);
    }

    @Override
    public CompletableFuture<Void> getWhenShopsLoaded() {
        return this.readyFuture;
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

    private static class ShopData {

        private final Shop shop;
        private final ShopItem shopItem;

        ShopData(Shop shop, ShopItem shopItem) {
            this.shopItem = shopItem;
            this.shop = shop;
        }

    }

}
