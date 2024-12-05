package com.bgsoftware.common.shopsbridge;

import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;

public interface BulkTransaction extends PricesAccessor {

    static BulkTransaction noBulk(PricesAccessor delegate) {
        return new BulkTransaction() {
            @Override
            public Transaction getSellPrice(OfflinePlayer offlinePlayer, ItemStack itemStack) {
                return delegate.getSellPrice(offlinePlayer, itemStack);
            }

            @Override
            public Transaction getSellPrice(ItemStack itemStack) {
                return delegate.getSellPrice(itemStack);
            }

            @Override
            public Transaction getBuyPrice(OfflinePlayer offlinePlayer, ItemStack itemStack) {
                return delegate.getBuyPrice(offlinePlayer, itemStack);
            }

            @Override
            public Transaction getBuyPrice(ItemStack itemStack) {
                return delegate.getBuyPrice(itemStack);
            }
        };
    }

}
