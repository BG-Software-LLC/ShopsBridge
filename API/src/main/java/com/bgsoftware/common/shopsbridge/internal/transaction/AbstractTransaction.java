package com.bgsoftware.common.shopsbridge.internal.transaction;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.common.shopsbridge.Transaction;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;

import java.math.BigDecimal;

public abstract class AbstractTransaction implements Transaction {

    @Nullable
    private final OfflinePlayer player;
    private final Type type;
    private final ItemStack itemStack;
    private final BigDecimal price;

    protected AbstractTransaction(@Nullable OfflinePlayer player, Type type, ItemStack itemStack, BigDecimal price) {
        this.player = player;
        this.type = type;
        this.itemStack = itemStack;
        this.price = price;
    }

    @Override
    @Nullable
    public OfflinePlayer getPlayer() {
        return this.player;
    }

    @Override
    public Type getType() {
        return this.type;
    }

    @Override
    public ItemStack getItem() {
        return this.itemStack;
    }

    @Override
    public BigDecimal getPrice() {
        return this.price;
    }

    @Override
    public abstract void onTransact();

}
