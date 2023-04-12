package com.bgsoftware.common.shopsbridge;

import com.bgsoftware.common.reflection.ReflectMethod;
import net.brcdev.shopgui.ShopGuiPlugin;
import net.brcdev.shopgui.event.ShopsPostLoadEvent;
import net.brcdev.shopgui.shop.Shop;
import net.brcdev.shopgui.shop.ShopItem;
import net.brcdev.shopgui.shop.ShopManager;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class ShopsBridge_ShopGUIPlus1_43 implements IShopsBridge {

    private static final ReflectMethod<Set<Shop>> GET_SHOPS_METHOD = new ReflectMethod<>(ShopManager.class, Set.class, "getShops");

    // Added cache for shop items for better performance
    private final Map<ItemStack, ShopItem> cachedShopItems = new HashMap<>();

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
    public BigDecimal getSellPrice(OfflinePlayer offlinePlayer, ItemStack itemStack) {
        return Optional.ofNullable(offlinePlayer.getPlayer()).flatMap(player -> getOrCreateShopDataForItem(itemStack)
                .map(shopItem -> BigDecimal.valueOf(shopItem.getSellPriceForAmount(player, itemStack.getAmount())))
        ).orElse(BigDecimal.ZERO);
    }

    @Override
    public BigDecimal getSellPrice(ItemStack itemStack) {
        return getOrCreateShopDataForItem(itemStack)
                .map(shopItem -> BigDecimal.valueOf(shopItem.getSellPriceForAmount(itemStack.getAmount())))
                .orElse(BigDecimal.ZERO);
    }

    @Override
    public BigDecimal getBuyPrice(OfflinePlayer offlinePlayer, ItemStack itemStack) {
        return Optional.ofNullable(offlinePlayer.getPlayer()).flatMap(player -> getOrCreateShopDataForItem(itemStack)
                .map(shopItem -> BigDecimal.valueOf(shopItem.getBuyPriceForAmount(player, itemStack.getAmount())))
        ).orElse(BigDecimal.ZERO);
    }

    @Override
    public BigDecimal getBuyPrice(ItemStack itemStack) {
        return getOrCreateShopDataForItem(itemStack)
                .map(shopItem -> BigDecimal.valueOf(shopItem.getBuyPriceForAmount(itemStack.getAmount())))
                .orElse(BigDecimal.ZERO);
    }

    @Override
    public CompletableFuture<Void> getWhenShopsLoaded() {
        return this.readyFuture;
    }

    private Optional<ShopItem> getOrCreateShopDataForItem(ItemStack itemStack) {
        ItemStack itemKey = itemStack.clone();
        itemKey.setAmount(1);

        return Optional.ofNullable(this.cachedShopItems.computeIfAbsent(itemKey, i -> {
            for (Shop shop : getShops()) {
                for (ShopItem shopItem : shop.getShopItems()) {
                    if (compareShopItem(shopItem.getItem(), itemStack, shopItem.isCompareMeta())) {
                        return shopItem;
                    }
                }
            }

            return null;
        }));
    }

    private Collection<Shop> getShops() {
        return GET_SHOPS_METHOD.isValid() ? GET_SHOPS_METHOD.invoke(plugin.getShopManager()) :
                plugin.getShopManager().shops.values();
    }

    private static boolean compareShopItem(ItemStack shopItem, ItemStack itemStack, boolean compareMetadata) {
        if (compareMetadata)
            return shopItem.isSimilar(itemStack);

        return shopItem.getType() == itemStack.getType() && shopItem.getDurability() == itemStack.getDurability();
    }

}
