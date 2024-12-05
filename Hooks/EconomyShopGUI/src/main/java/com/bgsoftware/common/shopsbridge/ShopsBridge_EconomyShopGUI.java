package com.bgsoftware.common.shopsbridge;

import com.bgsoftware.common.shopsbridge.internal.PricesAccessorNoTransactions;
import com.bgsoftware.common.shopsbridge.internal.scheduler.Scheduler;
import me.gypopo.economyshopgui.api.EconomyShopGUIHook;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class ShopsBridge_EconomyShopGUI implements PricesAccessorNoTransactions, IShopsBridge {

    private final CompletableFuture<Void> readyFuture = new CompletableFuture<>();

    public ShopsBridge_EconomyShopGUI(Plugin plugin) {
        Scheduler.runTaskLater(plugin, () -> this.readyFuture.complete(null), 1L);
    }

    @Override
    public BigDecimal getSellPriceInternal(OfflinePlayer offlinePlayer, ItemStack itemStack) {
        return Optional.ofNullable(offlinePlayer.getPlayer())
                .map(player -> EconomyShopGUIHook.getItemSellPrice(player, itemStack))
                .map(BigDecimal::valueOf)
                .orElseGet(() -> this.getSellPriceInternal(itemStack));
    }

    @Override
    public BigDecimal getSellPriceInternal(ItemStack itemStack) {
        return Optional.ofNullable(EconomyShopGUIHook.getItemSellPrice(itemStack))
                .map(BigDecimal::valueOf)
                .orElse(BigDecimal.ZERO);
    }

    @Override
    public BigDecimal getBuyPriceInternal(OfflinePlayer offlinePlayer, ItemStack itemStack) {
        return Optional.ofNullable(offlinePlayer.getPlayer())
                .map(player -> EconomyShopGUIHook.getItemBuyPrice(player, itemStack))
                .map(BigDecimal::valueOf)
                .orElseGet(() -> this.getBuyPriceInternal(itemStack));
    }

    @Override
    public BigDecimal getBuyPriceInternal(ItemStack itemStack) {
        return Optional.ofNullable(EconomyShopGUIHook.getItemBuyPrice(itemStack))
                .map(BigDecimal::valueOf)
                .orElse(BigDecimal.ZERO);
    }

    @Override
    public CompletableFuture<Void> getWhenShopsLoaded() {
        return this.readyFuture;
    }

}
