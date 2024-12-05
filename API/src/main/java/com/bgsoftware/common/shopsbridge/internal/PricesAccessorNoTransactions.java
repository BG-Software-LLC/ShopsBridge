package com.bgsoftware.common.shopsbridge.internal;

import com.bgsoftware.common.shopsbridge.PricesAccessor;
import com.bgsoftware.common.shopsbridge.Transaction;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;

import java.math.BigDecimal;

public interface PricesAccessorNoTransactions extends PricesAccessor {

    default Transaction getSellPrice(OfflinePlayer offlinePlayer, ItemStack itemStack) {
        return Transaction.newTransaction(offlinePlayer, Transaction.Type.SELL, itemStack,
                getSellPriceInternal(offlinePlayer, itemStack));
    }

    default Transaction getSellPrice(ItemStack itemStack) {
        return Transaction.newTransaction(null, Transaction.Type.SELL, itemStack,
                getSellPriceInternal(itemStack));
    }

    default Transaction getBuyPrice(OfflinePlayer offlinePlayer, ItemStack itemStack) {
        return Transaction.newTransaction(offlinePlayer, Transaction.Type.BUY, itemStack,
                getBuyPriceInternal(offlinePlayer, itemStack));
    }

    default Transaction getBuyPrice(ItemStack itemStack) {
        return Transaction.newTransaction(null, Transaction.Type.BUY, itemStack,
                getBuyPriceInternal(itemStack));
    }

    BigDecimal getSellPriceInternal(OfflinePlayer offlinePlayer, ItemStack itemStack);

    BigDecimal getSellPriceInternal(ItemStack itemStack);

    BigDecimal getBuyPriceInternal(OfflinePlayer offlinePlayer, ItemStack itemStack);

    BigDecimal getBuyPriceInternal(ItemStack itemStack);

}
