package com.bgsoftware.common.shopsbridge;

import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;

import java.math.BigDecimal;

public interface PricesAccessor {

    BigDecimal getSellPrice(OfflinePlayer offlinePlayer, ItemStack itemStack);

    BigDecimal getSellPrice(ItemStack itemStack);

    BigDecimal getBuyPrice(OfflinePlayer offlinePlayer, ItemStack itemStack);

    BigDecimal getBuyPrice(ItemStack itemStack);

}
