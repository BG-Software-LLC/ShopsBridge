package com.bgsoftware.common.shopsbridge;

import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;

import java.math.BigDecimal;

public interface BulkTransaction extends PricesAccessor {

    static BulkTransaction noBulk(PricesAccessor delegate) {
        return new BulkTransaction() {
            @Override
            public BigDecimal getSellPrice(OfflinePlayer offlinePlayer, ItemStack itemStack) {
                return delegate.getSellPrice(offlinePlayer, itemStack);
            }

            @Override
            public BigDecimal getSellPrice(ItemStack itemStack) {
                return delegate.getSellPrice(itemStack);
            }

            @Override
            public BigDecimal getBuyPrice(OfflinePlayer offlinePlayer, ItemStack itemStack) {
                return delegate.getBuyPrice(offlinePlayer, itemStack);
            }

            @Override
            public BigDecimal getBuyPrice(ItemStack itemStack) {
                return delegate.getBuyPrice(itemStack);
            }
        };
    }

}
