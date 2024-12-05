package com.bgsoftware.common.shopsbridge;

import com.bgsoftware.common.annotations.Nullable;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;

import java.math.BigDecimal;

public interface Transaction {

    @Nullable
    OfflinePlayer getPlayer();

    Type getType();

    ItemStack getItem();

    BigDecimal getPrice();

    void onTransact();

    static Transaction newTransaction(@Nullable OfflinePlayer offlinePlayer, Type type, ItemStack itemStack, BigDecimal price) {
        return new Transaction() {
            @Override
            public OfflinePlayer getPlayer() {
                return offlinePlayer;
            }

            @Override
            public Type getType() {
                return type;
            }

            @Override
            public ItemStack getItem() {
                return itemStack;
            }

            @Override
            public BigDecimal getPrice() {
                return price;
            }

            @Override
            public void onTransact() {
                // Do nothing.
            }
        };
    }

    enum Type {

        SELL,
        BUY

    }

}
