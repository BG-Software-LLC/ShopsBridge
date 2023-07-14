package com.bgsoftware.common.shopsbridge;

import me.gypopo.economyshopgui.api.EconomyShopGUIHook;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import javax.annotation.Nullable;
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
                .map(player -> EconomyShopGUIHook.getItemSellPrice(player, itemStack))
                .map(BigDecimal::valueOf)
                .orElseGet(() -> this.getSellPrice(itemStack));
    }

    @Override
    public BigDecimal getSellPrice(ItemStack itemStack) {
        return Optional.ofNullable(EconomyShopGUIHook.getItemSellPrice(itemStack))
                .map(BigDecimal::valueOf)
                .orElse(BigDecimal.ZERO);
    }

    @Override
    public BigDecimal getBuyPrice(OfflinePlayer offlinePlayer, ItemStack itemStack) {
        return Optional.ofNullable(offlinePlayer.getPlayer())
                .map(player -> EconomyShopGUIHook.getItemBuyPrice(player, itemStack))
                .map(BigDecimal::valueOf)
                .orElseGet(() -> this.getBuyPrice(itemStack));
    }

    @Override
    public BigDecimal getBuyPrice(ItemStack itemStack) {
        return Optional.ofNullable(EconomyShopGUIHook.getItemBuyPrice(itemStack))
                .map(BigDecimal::valueOf)
                .orElse(BigDecimal.ZERO);
    }

    @Override
    public CompletableFuture<Void> getWhenShopsLoaded() {
        return this.readyFuture;
    }

}
