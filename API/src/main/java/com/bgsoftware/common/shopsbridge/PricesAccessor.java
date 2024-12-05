package com.bgsoftware.common.shopsbridge;

import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;

public interface PricesAccessor {

    Transaction getSellPrice(OfflinePlayer offlinePlayer, ItemStack itemStack);

    Transaction getSellPrice(ItemStack itemStack);

    Transaction getBuyPrice(OfflinePlayer offlinePlayer, ItemStack itemStack);

    Transaction getBuyPrice(ItemStack itemStack);

}
