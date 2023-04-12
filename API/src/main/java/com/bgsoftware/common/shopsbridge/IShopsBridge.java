package com.bgsoftware.common.shopsbridge;

import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;

import java.math.BigDecimal;
import java.util.concurrent.CompletableFuture;

public interface IShopsBridge {

    BigDecimal getSellPrice(OfflinePlayer offlinePlayer, ItemStack itemStack);

    BigDecimal getSellPrice(ItemStack itemStack);

    BigDecimal getBuyPrice(OfflinePlayer offlinePlayer, ItemStack itemStack);

    BigDecimal getBuyPrice(ItemStack itemStack);

    CompletableFuture<Void> getWhenShopsLoaded();

}
