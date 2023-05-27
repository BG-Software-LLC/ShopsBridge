package com.bgsoftware.common.shopsbridge;

import net.brcdev.shopgui.ShopGuiPlugin;
import net.brcdev.shopgui.event.ShopsPostLoadEvent;
import net.brcdev.shopgui.shop.Shop;
import net.brcdev.shopgui.shop.item.ShopItem;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class ShopsBridge_ShopGUIPlus1_80 implements IShopsBridge {

    // Added cache for shop items for better performance
    private final Map<ItemStack, ShopItem> cachedShopItems = new HashMap<>();

    private final CompletableFuture<Void> readyFuture = new CompletableFuture<>();
    private final ShopGuiPlugin plugin;


    public ShopsBridge_ShopGUIPlus1_80(Plugin plugin) {
        this.plugin = ShopGuiPlugin.getInstance();
        plugin.getServer().getPluginManager().registerEvents(new Listener() {
            @EventHandler
            public void onShopsLoad(ShopsPostLoadEvent event) {
                readyFuture.complete(null);
            }
        }, plugin);
    }

    @Override
    public BigDecimal getSellPrice(OfflinePlayer offlinePlayer, ItemStack itemStack) {
        ensureShopsLoaded();
        return Optional.ofNullable(offlinePlayer.getPlayer()).flatMap(player -> getOrCreateShopDataForItem(itemStack)
                .map(shopItem -> BigDecimal.valueOf(shopItem.getSellPriceForAmount(player, itemStack.getAmount())))
        ).orElseGet(() -> this.getSellPrice(itemStack));
    }

    @Override
    public BigDecimal getSellPrice(ItemStack itemStack) {
        ensureShopsLoaded();
        return getOrCreateShopDataForItem(itemStack)
                .map(shopItem -> BigDecimal.valueOf(shopItem.getSellPriceForAmount(itemStack.getAmount())))
                .orElse(BigDecimal.ZERO);
    }

    @Override
    public BigDecimal getBuyPrice(OfflinePlayer offlinePlayer, ItemStack itemStack) {
        ensureShopsLoaded();
        return Optional.ofNullable(offlinePlayer.getPlayer()).flatMap(player -> getOrCreateShopDataForItem(itemStack)
                .map(shopItem -> BigDecimal.valueOf(shopItem.getBuyPriceForAmount(player, itemStack.getAmount())))
        ).orElseGet(() -> this.getBuyPrice(itemStack));
    }

    @Override
    public BigDecimal getBuyPrice(ItemStack itemStack) {
        ensureShopsLoaded();
        return getOrCreateShopDataForItem(itemStack)
                .map(shopItem -> BigDecimal.valueOf(shopItem.getBuyPriceForAmount(itemStack.getAmount())))
                .orElse(BigDecimal.ZERO);
    }

    @Override
    public CompletableFuture<Void> getWhenShopsLoaded() {
        return this.readyFuture;
    }

    private void ensureShopsLoaded() {
        if (!this.plugin.getShopManager().areShopsLoaded())
            this.plugin.getShopManager().load();
    }

    private Optional<ShopItem> getOrCreateShopDataForItem(ItemStack itemStack) {
        ItemStack itemKey = itemStack.clone();
        itemKey.setAmount(1);

        return Optional.ofNullable(this.cachedShopItems.computeIfAbsent(itemKey, i -> {
            for (Shop shop : this.plugin.getShopManager().getShops()) {
                for (ShopItem shopItem : shop.getShopItems()) {
                    if (compareShopItem(shopItem.getItem(), itemStack, shopItem.isCompareMeta())) {
                        return shopItem;
                    }
                }
            }

            return null;
        }));
    }

    private static boolean compareShopItem(ItemStack shopItem, ItemStack itemStack, boolean compareMetadata) {
        if (compareMetadata)
            return shopItem.isSimilar(itemStack);

        return shopItem.getType() == itemStack.getType() && shopItem.getDurability() == itemStack.getDurability();
    }

}
