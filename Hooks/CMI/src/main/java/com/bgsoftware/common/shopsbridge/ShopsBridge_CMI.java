package com.bgsoftware.common.shopsbridge;

import com.Zrips.CMI.CMI;
import com.Zrips.CMI.Modules.Worth.WorthItem;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.math.BigDecimal;
import java.util.concurrent.CompletableFuture;

public class ShopsBridge_CMI implements IShopsBridge {

    private final CompletableFuture<Void> readyFuture = new CompletableFuture<>();

    public ShopsBridge_CMI(Plugin plugin) {
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> this.readyFuture.complete(null), 1L);
    }

    @Override
    public BigDecimal getSellPrice(OfflinePlayer unused, ItemStack itemStack) {
        return this.getSellPrice(itemStack);
    }

    @Override
    public BigDecimal getSellPrice(ItemStack itemStack) {
        WorthItem worth = CMI.getInstance().getWorthManager().getWorth(itemStack);
        return worth == null ? BigDecimal.ZERO : BigDecimal.valueOf(worth.getSellPrice() * itemStack.getAmount());
    }

    @Override
    public BigDecimal getBuyPrice(OfflinePlayer unused, ItemStack itemStack) {
        return this.getBuyPrice(itemStack);
    }

    @Override
    public BigDecimal getBuyPrice(ItemStack itemStack) {
        WorthItem worth = CMI.getInstance().getWorthManager().getWorth(itemStack);
        return worth == null ? BigDecimal.ZERO : BigDecimal.valueOf(worth.getBuyPrice() * itemStack.getAmount());
    }

    @Override
    public CompletableFuture<Void> getWhenShopsLoaded() {
        return this.readyFuture;
    }

}
