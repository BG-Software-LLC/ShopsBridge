package com.bgsoftware.common.shopsbridge;

import me.gypopo.economyshopgui.api.EconomyShopGUIHook;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class ShopsBridge_EconomyShopGUI implements IShopsBridge {

    private final CompletableFuture<Void> readyFuture = new CompletableFuture<>();

    public ShopsBridge_EconomyShopGUI(Plugin plugin) {
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> this.readyFuture.complete(null), 1L);
    }

    @Override
    public BigDecimal getSellPrice(OfflinePlayer offlinePlayer, ItemStack itemStack) {
        return Optional.ofNullable(offlinePlayer.getPlayer())
                .map(player -> BigDecimal.valueOf(EconomyShopGUIHook.getItemSellPrice(player, itemStack)))
                .orElse(this.getSellPrice(itemStack));
    }

    @Override
    public BigDecimal getSellPrice(ItemStack itemStack) {
        return BigDecimal.valueOf(EconomyShopGUIHook.getItemSellPrice(itemStack));
    }

    @Override
    public BigDecimal getBuyPrice(OfflinePlayer offlinePlayer, ItemStack itemStack) {
        return Optional.ofNullable(offlinePlayer.getPlayer())
                .map(player -> BigDecimal.valueOf(EconomyShopGUIHook.getItemBuyPrice(player, itemStack)))
                .orElse(this.getSellPrice(itemStack));
    }

    @Override
    public BigDecimal getBuyPrice(ItemStack itemStack) {
        return BigDecimal.valueOf(EconomyShopGUIHook.getItemBuyPrice(itemStack));
    }

    @Override
    public CompletableFuture<Void> getWhenShopsLoaded() {
        return this.readyFuture;
    }

}
