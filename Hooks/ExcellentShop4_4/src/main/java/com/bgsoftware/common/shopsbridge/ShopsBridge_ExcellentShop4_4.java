package com.bgsoftware.common.shopsbridge;

import com.bgsoftware.common.reflection.ReflectMethod;
import com.bgsoftware.common.shopsbridge.internal.ItemStackCache;
import com.bgsoftware.common.shopsbridge.internal.scheduler.Scheduler;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import su.nightexpress.nexshop.ExcellentShop;
import su.nightexpress.nexshop.api.shop.ItemProduct;
import su.nightexpress.nexshop.api.type.TradeType;
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
    public BigDecimal getSellPrice(OfflinePlayer offlinePlayer, ItemStack itemStack) {
        return Optional.ofNullable(this.excellentShop.getVirtualShop())
                .flatMap(virtualShopModule ->
                        Optional.ofNullable(offlinePlayer.getPlayer())
                                .map(player -> VIRTUAL_SHOP_MODULE_GET_BEST_PRODUCT_FOR_METHOD.invoke(virtualShopModule, player, itemStack, TradeType.SELL))
                                .map(virtualProduct -> getPrice(virtualProduct, itemStack)))
                .map(BigDecimal::valueOf)
                .orElseGet(() -> this.getSellPrice(itemStack));
    }

    @Override
    public BigDecimal getSellPrice(ItemStack itemStack) {
        return getShopProduct(itemStack, TradeType.SELL)
                .map(virtualProduct -> getPrice(virtualProduct, itemStack))
                .map(BigDecimal::valueOf)
                .orElse(BigDecimal.ZERO);
    }

    @Override
    public BigDecimal getBuyPrice(OfflinePlayer offlinePlayer, ItemStack itemStack) {
        return Optional.ofNullable(this.excellentShop.getVirtualShop())
                .flatMap(virtualShopModule ->
                        Optional.ofNullable(offlinePlayer.getPlayer())
                                .map(player -> VIRTUAL_SHOP_MODULE_GET_BEST_PRODUCT_FOR_METHOD.invoke(virtualShopModule, player, itemStack, TradeType.BUY))
                                .map(virtualProduct -> getPrice(virtualProduct, itemStack)))
                .map(BigDecimal::valueOf)
                .orElseGet(() -> this.getBuyPrice(itemStack));
    }

    @Override
    public BigDecimal getBuyPrice(ItemStack itemStack) {
        return getShopProduct(itemStack, TradeType.BUY)
                .map(virtualProduct -> getPrice(virtualProduct, itemStack))
                .map(BigDecimal::valueOf)
                .orElse(BigDecimal.ZERO);
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

    private static double getPrice(VirtualProduct product, ItemStack itemStack) {
        return product.getPricer().getPriceSell() * ((double) itemStack.getAmount() / product.getUnitAmount());
    }

    private class BulkTransactionImpl implements BulkTransaction {

        private final ItemStackCache<VirtualProduct> cache = new ItemStackCache<>();

        @Override
        public BigDecimal getSellPrice(OfflinePlayer offlinePlayer, ItemStack itemStack) {
            return Optional.ofNullable(this.cache.computeIfAbsent(itemStack, () -> {
                        return Optional.ofNullable(excellentShop.getVirtualShop())
                                .flatMap(virtualShopModule -> Optional.ofNullable(offlinePlayer.getPlayer())
                                        .map(player -> VIRTUAL_SHOP_MODULE_GET_BEST_PRODUCT_FOR_METHOD.invoke(virtualShopModule, player, itemStack, TradeType.SELL)))
                                .orElse(null);
                    })).map(virtualProduct -> getPrice(virtualProduct, itemStack))
                    .map(BigDecimal::valueOf)
                    .orElseGet(() -> this.getSellPrice(itemStack));
        }

        @Override
        public BigDecimal getSellPrice(ItemStack itemStack) {
            return Optional.ofNullable(this.cache.computeIfAbsent(itemStack, () -> getShopProduct(itemStack, TradeType.SELL).orElse(null)))
                    .map(virtualProduct -> getPrice(virtualProduct, itemStack))
                    .map(BigDecimal::valueOf)
                    .orElse(BigDecimal.ZERO);
        }

        @Override
        public BigDecimal getBuyPrice(OfflinePlayer offlinePlayer, ItemStack itemStack) {
            return Optional.ofNullable(this.cache.computeIfAbsent(itemStack, () -> {
                        return Optional.ofNullable(excellentShop.getVirtualShop())
                                .flatMap(virtualShopModule -> Optional.ofNullable(offlinePlayer.getPlayer())
                                        .map(player -> VIRTUAL_SHOP_MODULE_GET_BEST_PRODUCT_FOR_METHOD.invoke(virtualShopModule, player, itemStack, TradeType.BUY)))
                                .orElse(null);
                    })).map(virtualProduct -> getPrice(virtualProduct, itemStack))
                    .map(BigDecimal::valueOf)
                    .orElseGet(() -> this.getBuyPrice(itemStack));
        }

        @Override
        public BigDecimal getBuyPrice(ItemStack itemStack) {
            return Optional.ofNullable(this.cache.computeIfAbsent(itemStack, () -> getShopProduct(itemStack, TradeType.BUY).orElse(null)))
                    .map(virtualProduct -> getPrice(virtualProduct, itemStack))
                    .map(BigDecimal::valueOf)
                    .orElse(BigDecimal.ZERO);
        }

    }

}
