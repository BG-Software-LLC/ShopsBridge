package com.bgsoftware.common.shopsbridge;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.common.reflection.ReflectMethod;
import com.bgsoftware.common.shopsbridge.internal.ItemStackCache;
import com.bgsoftware.common.shopsbridge.internal.scheduler.Scheduler;
import com.bgsoftware.common.shopsbridge.internal.transaction.AbstractTransaction;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import su.nightexpress.nexshop.ExcellentShop;
import su.nightexpress.nexshop.api.IPurchaseListener;
import su.nightexpress.nexshop.api.event.VirtualShopTransactionEvent;
import su.nightexpress.nexshop.api.shop.ItemProduct;
import su.nightexpress.nexshop.api.type.PriceType;
import su.nightexpress.nexshop.api.type.TradeType;
import su.nightexpress.nexshop.shop.util.TransactionResult;
import su.nightexpress.nexshop.shop.virtual.VirtualShopModule;
import su.nightexpress.nexshop.shop.virtual.impl.product.VirtualProduct;
import su.nightexpress.nexshop.shop.virtual.impl.shop.VirtualShop;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class ShopsBridge_ExcellentShop4_4 implements IShopsBridge {

    private static final ReflectMethod<VirtualProduct> VIRTUAL_SHOP_MODULE_GET_BEST_PRODUCT_FOR_METHOD =
            new ReflectMethod<>(VirtualShopModule.class, VirtualProduct.class, "getBestProductFor", Player.class, ItemStack.class, TradeType.class);
    private final CompletableFuture<Void> readyFuture = new CompletableFuture<>();
    private final ExcellentShop excellentShop;

    public ShopsBridge_ExcellentShop4_4(Plugin plugin) {
        this.excellentShop = JavaPlugin.getPlugin(ExcellentShop.class);
        Scheduler.runTaskLater(plugin, () -> this.readyFuture.complete(null), 1L);
    }

    @Override
    public Transaction getSellPrice(OfflinePlayer offlinePlayer, ItemStack itemStack) {
        return Optional.ofNullable(this.excellentShop.getVirtualShop())
                .flatMap(virtualShopModule ->
                        Optional.ofNullable(offlinePlayer.getPlayer())
                                .map(player -> VIRTUAL_SHOP_MODULE_GET_BEST_PRODUCT_FOR_METHOD.invoke(virtualShopModule, player, itemStack, TradeType.SELL))
                                .map(virtualProduct -> getTransactionFor(offlinePlayer, virtualProduct, itemStack, TradeType.SELL)))
                .orElseGet(() -> (TransactionImpl) this.getSellPrice(itemStack));
    }

    @Override
    public Transaction getSellPrice(ItemStack itemStack) {
        return getShopProduct(itemStack, TradeType.SELL)
                .map(virtualProduct -> getTransactionFor(null, virtualProduct, itemStack, TradeType.SELL))
                .orElseGet(() -> new TransactionImpl(null, Transaction.Type.SELL, itemStack, BigDecimal.ZERO, null));
    }

    @Override
    public Transaction getBuyPrice(OfflinePlayer offlinePlayer, ItemStack itemStack) {
        return Optional.ofNullable(this.excellentShop.getVirtualShop())
                .flatMap(virtualShopModule ->
                        Optional.ofNullable(offlinePlayer.getPlayer())
                                .map(player -> VIRTUAL_SHOP_MODULE_GET_BEST_PRODUCT_FOR_METHOD.invoke(virtualShopModule, player, itemStack, TradeType.BUY))
                                .map(virtualProduct -> getTransactionFor(offlinePlayer, virtualProduct, itemStack, TradeType.BUY)))
                .orElseGet(() -> (TransactionImpl) this.getBuyPrice(itemStack));
    }

    @Override
    public Transaction getBuyPrice(ItemStack itemStack) {
        return getShopProduct(itemStack, TradeType.BUY)
                .map(virtualProduct -> getTransactionFor(null, virtualProduct, itemStack, TradeType.BUY))
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

    private Optional<VirtualProduct> getShopProduct(ItemStack itemStack, TradeType tradeType) {
        return Optional.ofNullable(this.excellentShop.getVirtualShop()).map(virtualShopModule -> {
            for (VirtualShop virtualShop : virtualShopModule.getShops()) {
                if (virtualShop.isTransactionEnabled(tradeType)) {
                    for (VirtualProduct virtualProduct : (Collection<VirtualProduct>) virtualShop.getProducts()) {
                        if (virtualProduct instanceof ItemProduct) {
                            if (!((ItemProduct) virtualProduct).isItemMatches(itemStack))
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

    private static TransactionImpl getTransactionFor(OfflinePlayer offlinePlayer, VirtualProduct product, ItemStack itemStack, TradeType tradeType) {
        double price = product.getPricer().getPrice(tradeType) * ((double) itemStack.getAmount() / product.getUnitAmount());
        Transaction.Type type = tradeType == TradeType.SELL ? Transaction.Type.SELL : Transaction.Type.BUY;
        return new TransactionImpl(offlinePlayer, type, itemStack, BigDecimal.valueOf(price), product);
    }

    private class BulkTransactionImpl implements BulkTransaction {

        private final ItemStackCache<VirtualProduct> cache = new ItemStackCache<>();

        @Override
        public Transaction getSellPrice(OfflinePlayer offlinePlayer, ItemStack itemStack) {
            return Optional.ofNullable(this.cache.computeIfAbsent(itemStack, () -> {
                        return Optional.ofNullable(excellentShop.getVirtualShop())
                                .flatMap(virtualShopModule -> Optional.ofNullable(offlinePlayer.getPlayer())
                                        .map(player -> VIRTUAL_SHOP_MODULE_GET_BEST_PRODUCT_FOR_METHOD.invoke(virtualShopModule, player, itemStack, TradeType.SELL)))
                                .orElse(null);
                    })).map(virtualProduct -> getTransactionFor(offlinePlayer, virtualProduct, itemStack, TradeType.SELL))
                    .orElseGet(() -> (TransactionImpl) this.getSellPrice(itemStack));
        }

        @Override
        public Transaction getSellPrice(ItemStack itemStack) {
            return Optional.ofNullable(this.cache.computeIfAbsent(itemStack, () -> getShopProduct(itemStack, TradeType.SELL).orElse(null)))
                    .map(virtualProduct -> getTransactionFor(null, virtualProduct, itemStack, TradeType.SELL))
                    .orElseGet(() -> new TransactionImpl(null, Transaction.Type.SELL, itemStack, BigDecimal.ZERO, null));
        }

        @Override
        public Transaction getBuyPrice(OfflinePlayer offlinePlayer, ItemStack itemStack) {
            return Optional.ofNullable(this.cache.computeIfAbsent(itemStack, () -> {
                        return Optional.ofNullable(excellentShop.getVirtualShop())
                                .flatMap(virtualShopModule -> Optional.ofNullable(offlinePlayer.getPlayer())
                                        .map(player -> VIRTUAL_SHOP_MODULE_GET_BEST_PRODUCT_FOR_METHOD.invoke(virtualShopModule, player, itemStack, TradeType.BUY)))
                                .orElse(null);
                    })).map(virtualProduct -> getTransactionFor(offlinePlayer, virtualProduct, itemStack, TradeType.BUY))
                    .orElseGet(() -> (TransactionImpl) this.getBuyPrice(itemStack));
        }

        @Override
        public Transaction getBuyPrice(ItemStack itemStack) {
            return Optional.ofNullable(this.cache.computeIfAbsent(itemStack, () -> getShopProduct(itemStack, TradeType.BUY).orElse(null)))
                    .map(virtualProduct -> getTransactionFor(null, virtualProduct, itemStack, TradeType.BUY))
                    .orElseGet(() -> new TransactionImpl(null, Transaction.Type.BUY, itemStack, BigDecimal.ZERO, null));
        }

    }

    private static class TransactionImpl extends AbstractTransaction {

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
            TransactionResult transaction = new TransactionResult(
                    this.product, tradeType, getItem().getAmount(), getPrice().doubleValue(),
                    TransactionResult.Result.SUCCESS);
            VirtualShopTransactionEvent shopTransactionEvent = new VirtualShopTransactionEvent(player, transaction);

            product.getStock().onPurchase(shopTransactionEvent);
            if (product.getPricer() instanceof IPurchaseListener)
                ((IPurchaseListener) product.getPricer()).onPurchase(shopTransactionEvent);
        }
    }

}
