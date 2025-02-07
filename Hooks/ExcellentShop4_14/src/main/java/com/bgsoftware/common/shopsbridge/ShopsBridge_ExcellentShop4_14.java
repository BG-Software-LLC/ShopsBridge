package com.bgsoftware.common.shopsbridge;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.common.shopsbridge.internal.ItemStackCache;
import com.bgsoftware.common.shopsbridge.internal.scheduler.Scheduler;
import com.bgsoftware.common.shopsbridge.internal.transaction.AbstractTransaction;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import su.nightexpress.nexshop.ShopAPI;
import su.nightexpress.nexshop.ShopPlugin;
import su.nightexpress.nexshop.api.shop.event.ShopTransactionEvent;
import su.nightexpress.nexshop.api.shop.product.typing.PhysicalTyping;
import su.nightexpress.nexshop.api.shop.product.typing.ProductTyping;
import su.nightexpress.nexshop.api.shop.type.PriceType;
import su.nightexpress.nexshop.api.shop.type.TradeType;
import su.nightexpress.nexshop.shop.virtual.impl.VirtualProduct;
import su.nightexpress.nexshop.shop.virtual.impl.VirtualShop;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class ShopsBridge_ExcellentShop4_14 implements IShopsBridge {

    private final CompletableFuture<Void> readyFuture = new CompletableFuture<>();

    public ShopsBridge_ExcellentShop4_14(Plugin plugin) {
        Scheduler.runTaskLater(plugin, () -> this.readyFuture.complete(null), 1L);
    }

    @Override
    public Transaction getSellPrice(OfflinePlayer offlinePlayer, ItemStack itemStack) {
        Player player = offlinePlayer.getPlayer();
        if (player != null) {
            return getVirtualProductFor(itemStack, TradeType.SELL, player)
                    .map(virtualProduct -> getSellTransactionFor(virtualProduct, player, itemStack))
                    .orElseGet(() -> (TransactionImpl) this.getSellPrice(itemStack));
        }

        return this.getSellPrice(itemStack);
    }

    @Override
    public Transaction getSellPrice(ItemStack itemStack) {
        return getShopProduct(itemStack, TradeType.SELL)
                .map(virtualProduct -> getSellTransactionFor(virtualProduct, null, itemStack))
                .orElseGet(() -> new TransactionImpl(null, Transaction.Type.SELL, itemStack, BigDecimal.ZERO, null));
    }

    @Override
    public Transaction getBuyPrice(OfflinePlayer offlinePlayer, ItemStack itemStack) {
        Player player = offlinePlayer.getPlayer();
        if (player != null) {
            return getVirtualProductFor(itemStack, TradeType.BUY, player)
                    .map(virtualProduct -> getBuyTransactionFor(virtualProduct, player, itemStack))
                    .orElseGet(() -> (TransactionImpl) this.getBuyPrice(itemStack));
        }

        return this.getBuyPrice(itemStack);
    }

    @Override
    public Transaction getBuyPrice(ItemStack itemStack) {
        return getShopProduct(itemStack, TradeType.BUY)
                .map(virtualProduct -> getBuyTransactionFor(virtualProduct, null, itemStack))
                .orElseGet(() -> new TransactionImpl(null, Transaction.Type.BUY, itemStack, BigDecimal.ZERO, null));
    }

    @Override
    public CompletableFuture<Void> getWhenShopsLoaded() {
        return this.readyFuture;
    }

    @Override
    public BulkTransaction startBulkTransaction() {
        return new BulkTransactionImpl();
    }

    private Optional<VirtualProduct> getVirtualProductFor(ItemStack itemStack, TradeType tradeType, @Nullable Player player) {
        return player == null ? getShopProduct(itemStack, tradeType) :
                Optional.ofNullable(ShopAPI.getVirtualShop())
                        .map(virtualShopModule -> virtualShopModule.getBestProductFor(itemStack, tradeType, player));
    }

    private Optional<VirtualProduct> getShopProduct(ItemStack itemStack, TradeType tradeType) {
        return Optional.ofNullable(ShopAPI.getVirtualShop()).map(virtualShopModule -> {
            for (VirtualShop virtualShop : virtualShopModule.getShops()) {
                if (virtualShop.isTradeAllowed(tradeType)) {
                    for (VirtualProduct virtualProduct : virtualShop.getProducts()) {
                        ProductTyping type = virtualProduct.getType();
                        if (type instanceof PhysicalTyping) {
                            if (!((PhysicalTyping) type).isItemMatches(itemStack))
                                continue;
                        }

                        switch (tradeType) {
                            case BUY:
                                if (!virtualProduct.isBuyable())
                                    continue;
                                break;
                            case SELL:
                                if (!virtualProduct.isSellable())
                                    continue;
                                break;
                        }

                        return virtualProduct;
                    }
                }
            }

            return null;
        });
    }

    private static TransactionImpl getSellTransactionFor(VirtualProduct product, @Nullable Player player, ItemStack itemStack) {
        double sellPrice = player == null ? product.getPricer().getSellPrice() : product.getPriceSell(player);
        double finalPrice = sellPrice * ((double) itemStack.getAmount() / product.getUnitAmount());
        return new TransactionImpl(player, Transaction.Type.SELL, itemStack, BigDecimal.valueOf(finalPrice), product);
    }

    private static TransactionImpl getBuyTransactionFor(VirtualProduct product, @Nullable Player player, ItemStack itemStack) {
        double buyPrice = player == null ? product.getPricer().getBuyPrice() : product.getPriceBuy(player);
        double finalPrice = buyPrice * ((double) itemStack.getAmount() / product.getUnitAmount());
        return new TransactionImpl(player, Transaction.Type.BUY, itemStack, BigDecimal.valueOf(finalPrice), product);
    }

    private class BulkTransactionImpl implements BulkTransaction {

        private final ItemStackCache<VirtualProduct> cache = new ItemStackCache<>();

        @Override
        public Transaction getSellPrice(OfflinePlayer offlinePlayer, ItemStack itemStack) {
            Player player = offlinePlayer.getPlayer();
            if (player != null) {
                return Optional.ofNullable(this.cache.computeIfAbsent(itemStack,
                                () -> getVirtualProductFor(itemStack, TradeType.SELL, player).orElse(null)))
                        .map(virtualProduct -> getSellTransactionFor(virtualProduct, player, itemStack))
                        .orElseGet(() -> (TransactionImpl) this.getSellPrice(itemStack));
            }

            return this.getSellPrice(itemStack);
        }

        @Override
        public Transaction getSellPrice(ItemStack itemStack) {
            return Optional.ofNullable(this.cache.computeIfAbsent(itemStack,
                            () -> getShopProduct(itemStack, TradeType.SELL).orElse(null)))
                    .map(virtualProduct -> getSellTransactionFor(virtualProduct, null, itemStack))
                    .orElseGet(() -> new TransactionImpl(null, Transaction.Type.SELL, itemStack, BigDecimal.ZERO, null));
        }

        @Override
        public Transaction getBuyPrice(OfflinePlayer offlinePlayer, ItemStack itemStack) {
            Player player = offlinePlayer.getPlayer();
            if (player != null) {
                return Optional.ofNullable(this.cache.computeIfAbsent(itemStack,
                                () -> getVirtualProductFor(itemStack, TradeType.BUY, player).orElse(null)))
                        .map(virtualProduct -> getBuyTransactionFor(virtualProduct, player, itemStack))
                        .orElseGet(() -> (TransactionImpl) this.getBuyPrice(itemStack));
            }

            return this.getBuyPrice(itemStack);
        }

        @Override
        public Transaction getBuyPrice(ItemStack itemStack) {
            return Optional.ofNullable(this.cache.computeIfAbsent(itemStack,
                            () -> getShopProduct(itemStack, TradeType.BUY).orElse(null)))
                    .map(virtualProduct -> getBuyTransactionFor(virtualProduct, null, itemStack))
                    .orElseGet(() -> new TransactionImpl(null, Transaction.Type.BUY, itemStack, BigDecimal.ZERO, null));
        }

    }

    private static class TransactionImpl extends AbstractTransaction {

        private static final ShopPlugin shopPlugin = JavaPlugin.getPlugin(ShopPlugin.class);

        @Nullable
        private final VirtualProduct product;

        TransactionImpl(@Nullable OfflinePlayer player, Type type, ItemStack itemStack, BigDecimal price, @Nullable VirtualProduct product) {
            super(player, type, itemStack, price);
            this.product = product;
        }

        @Override
        public void onTransact() {
            if (this.product == null || product.getPricer().getType() != PriceType.DYNAMIC)
                return;

            Player player = Optional.ofNullable(getPlayer()).map(OfflinePlayer::getPlayer).orElse(null);
            if (player == null)
                return;

            VirtualShop virtualShop = product.getShop();

            TradeType tradeType = getType() == Type.SELL ? TradeType.SELL : TradeType.BUY;
            su.nightexpress.nexshop.api.shop.Transaction transaction = new su.nightexpress.nexshop.api.shop.Transaction(
                    shopPlugin, this.product, tradeType, getItem().getAmount(), getPrice().doubleValue(),
                    su.nightexpress.nexshop.api.shop.Transaction.Result.SUCCESS);
            ShopTransactionEvent shopTransactionEvent = new ShopTransactionEvent(player, virtualShop, transaction);

            virtualShop.onTransaction(shopTransactionEvent);
        }
    }

}
