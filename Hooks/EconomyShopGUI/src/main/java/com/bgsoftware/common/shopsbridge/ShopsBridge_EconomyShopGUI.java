package com.bgsoftware.common.shopsbridge;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.common.shopsbridge.internal.scheduler.Scheduler;
import com.bgsoftware.common.shopsbridge.internal.transaction.AbstractTransaction;
import me.gypopo.economyshopgui.api.EconomyShopGUIHook;
import me.gypopo.economyshopgui.objects.ShopItem;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class ShopsBridge_EconomyShopGUI implements IShopsBridge {

    private final CompletableFuture<Void> readyFuture = new CompletableFuture<>();

    public ShopsBridge_EconomyShopGUI(Plugin plugin) {
        Scheduler.runTaskLater(plugin, () -> this.readyFuture.complete(null), 1L);
    }

    @Override
    public Transaction getSellPrice(OfflinePlayer offlinePlayer, ItemStack itemStack) {
        return Optional.ofNullable(offlinePlayer.getPlayer())
                .map(player -> getSellTransactionFor(itemStack, offlinePlayer.getUniqueId(), player))
                .orElseGet(() -> (TransactionImpl) this.getSellPrice(itemStack));
    }

    @Override
    public Transaction getSellPrice(ItemStack itemStack) {
        return getSellTransactionFor(itemStack, null, null);
    }

    @Override
    public Transaction getBuyPrice(OfflinePlayer offlinePlayer, ItemStack itemStack) {
        return Optional.ofNullable(offlinePlayer.getPlayer())
                .map(player -> getBuyTransactionFor(itemStack, offlinePlayer.getUniqueId(), player))
                .orElseGet(() -> (TransactionImpl) this.getBuyPrice(itemStack));
    }

    @Override
    public Transaction getBuyPrice(ItemStack itemStack) {
        return getBuyTransactionFor(itemStack, null, null);
    }

    @Override
    public CompletableFuture<Void> getWhenShopsLoaded() {
        return this.readyFuture;
    }

    private static TransactionImpl getSellTransactionFor(ItemStack itemStack, @Nullable UUID playerUUID, @Nullable Player player) {
        ShopItem shopItem = player == null ? EconomyShopGUIHook.getShopItem(itemStack) :
                EconomyShopGUIHook.getShopItem(player, itemStack);

        if (shopItem == null)
            return TransactionImpl.empty(Transaction.Type.SELL, itemStack);

        ItemStack toSell = itemStack;

        if (playerUUID != null && shopItem.getLimitedSellMode() != 0) {
            int stock = EconomyShopGUIHook.getSellLimit(shopItem, playerUUID);
            if (toSell.getAmount() > stock) {
                toSell = cloneWithAmount(itemStack, toSell, stock);
            }
        }

        BigDecimal price = BigDecimal.valueOf(player == null ? shopItem.getSellPrice(toSell) :
                shopItem.getSellPrice(player, toSell));

        return new TransactionImpl(player, Transaction.Type.SELL, toSell, price, shopItem);
    }

    private static TransactionImpl getBuyTransactionFor(ItemStack itemStack, @Nullable UUID playerUUID, @Nullable Player player) {
        ShopItem shopItem = player == null ? EconomyShopGUIHook.getShopItem(itemStack) :
                EconomyShopGUIHook.getShopItem(player, itemStack);
        if (shopItem == null)
            return TransactionImpl.empty(Transaction.Type.BUY, itemStack);

        ItemStack toBuy = itemStack;

        if (playerUUID != null && shopItem.getLimitedStockMode() != 0) {
            int stock = EconomyShopGUIHook.getItemStock(shopItem, playerUUID);
            if (toBuy.getAmount() > stock) {
                toBuy = cloneWithAmount(itemStack, toBuy, stock);
            }
        }

        BigDecimal price = BigDecimal.valueOf(player == null ? shopItem.getBuyPrice(toBuy.getAmount()) :
                shopItem.getBuyPrice(player, toBuy.getAmount()));

        return new TransactionImpl(player, Transaction.Type.BUY, toBuy, price, shopItem);
    }

    private static ItemStack cloneWithAmount(ItemStack original, ItemStack itemStack, int amount) {
        if (original == itemStack)
            itemStack = itemStack.clone();
        itemStack.setAmount(amount);
        return itemStack;
    }

    private static class TransactionImpl extends AbstractTransaction {

        @Nullable
        private final ShopItem shopItem;

        static TransactionImpl empty(Type type, ItemStack itemStack) {
            return new TransactionImpl(null, type, itemStack, BigDecimal.ZERO, null);
        }

        TransactionImpl(@Nullable OfflinePlayer player, Type type, ItemStack itemStack, BigDecimal price, @Nullable ShopItem shopItem) {
            super(player, type, itemStack, price);
            this.shopItem = shopItem;
        }

        @Override
        public void onTransact() {
            if (this.shopItem == null)
                return;

            UUID playerUUID = Optional.ofNullable(getPlayer()).map(OfflinePlayer::getUniqueId).orElse(null);
            if (playerUUID == null)
                return;

            int itemSoldAmount = getItem().getAmount();

            if (getType() == Type.BUY) {
                if (this.shopItem.getLimitedStockMode() != 0)
                    EconomyShopGUIHook.buyItemStock(this.shopItem, playerUUID, itemSoldAmount);
                if (this.shopItem.isDynamicPricing())
                    EconomyShopGUIHook.buyItem(this.shopItem, itemSoldAmount);
            } else {
                if (this.shopItem.isRefillStock())
                    EconomyShopGUIHook.sellItemStock(this.shopItem, playerUUID, itemSoldAmount);
                if (this.shopItem.getLimitedSellMode() != 0)
                    EconomyShopGUIHook.sellItemLimit(this.shopItem, playerUUID, itemSoldAmount);
                if (this.shopItem.isDynamicPricing())
                    EconomyShopGUIHook.sellItem(this.shopItem, itemSoldAmount);
            }

        }
    }

}
