package com.bgsoftware.common.shopsbridge;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.common.shopsbridge.internal.ItemStackCache;
import com.bgsoftware.common.shopsbridge.internal.scheduler.Scheduler;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import su.nightexpress.nexshop.ShopAPI;
import su.nightexpress.nexshop.api.shop.VirtualShop;
import su.nightexpress.nexshop.api.shop.packer.ItemPacker;
import su.nightexpress.nexshop.api.shop.packer.ProductPacker;
import su.nightexpress.nexshop.api.shop.product.VirtualProduct;
import su.nightexpress.nexshop.api.shop.type.TradeType;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class ShopsBridge_ExcellentShop4_11 implements IShopsBridge {

    private final CompletableFuture<Void> readyFuture = new CompletableFuture<>();

    public ShopsBridge_ExcellentShop4_11(Plugin plugin) {
        Scheduler.runTaskLater(plugin, () -> this.readyFuture.complete(null), 1L);
    }

    @Override
    public BigDecimal getSellPrice(OfflinePlayer offlinePlayer, ItemStack itemStack) {
        Player player = offlinePlayer.getPlayer();
        if (player != null) {
            return getVirtualProductFor(itemStack, TradeType.SELL, player)
                    .map(virtualProduct -> getSellPriceFor(virtualProduct, player, itemStack))
                    .map(BigDecimal::valueOf)
                    .orElseGet(() -> this.getSellPrice(itemStack));
        }

        return this.getSellPrice(itemStack);
    }

    @Override
    public BigDecimal getSellPrice(ItemStack itemStack) {
        return getShopProduct(itemStack, TradeType.SELL)
                .map(virtualProduct -> getSellPriceFor(virtualProduct, null, itemStack))
                .map(BigDecimal::valueOf)
                .orElse(BigDecimal.ZERO);
    }

    @Override
    public BigDecimal getBuyPrice(OfflinePlayer offlinePlayer, ItemStack itemStack) {
        Player player = offlinePlayer.getPlayer();
        if (player != null) {
            return getVirtualProductFor(itemStack, TradeType.BUY, player)
                    .map(virtualProduct -> getBuyPriceFor(virtualProduct, player, itemStack))
                    .map(BigDecimal::valueOf)
                    .orElseGet(() -> this.getBuyPrice(itemStack));
        }

        return this.getBuyPrice(itemStack);
    }

    @Override
    public BigDecimal getBuyPrice(ItemStack itemStack) {
        return getShopProduct(itemStack, TradeType.BUY)
                .map(virtualProduct -> getBuyPriceFor(virtualProduct, null, itemStack))
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

    private Optional<VirtualProduct> getVirtualProductFor(ItemStack itemStack, TradeType tradeType, @Nullable Player player) {
        return player == null ? getShopProduct(itemStack, tradeType) :
                Optional.ofNullable(ShopAPI.getVirtualShop())
                        .map(virtualShopModule -> virtualShopModule.getBestProductFor(player, itemStack, tradeType));
    }

    private Optional<VirtualProduct> getShopProduct(ItemStack itemStack, TradeType tradeType) {
        return Optional.ofNullable(ShopAPI.getVirtualShop()).map(virtualShopModule -> {
            for (VirtualShop virtualShop : virtualShopModule.getShops()) {
                if (virtualShop.isTransactionEnabled(tradeType)) {
                    for (VirtualProduct virtualProduct : virtualShop.getProducts()) {
                        ProductPacker packer = virtualProduct.getPacker();
                        if (packer instanceof ItemPacker) {
                            if (!((ItemPacker) packer).isItemMatches(itemStack))
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

    private static double getSellPriceFor(VirtualProduct product, @Nullable Player player, ItemStack itemStack) {
        double sellPrice = player == null ? product.getPricer().getSellPrice() : product.getPriceSell(player);
        return sellPrice * ((double) itemStack.getAmount() / product.getUnitAmount());
    }

    private static double getBuyPriceFor(VirtualProduct product, @Nullable Player player, ItemStack itemStack) {
        double buyPrice = player == null ? product.getPricer().getBuyPrice() : product.getPriceBuy(player);
        return buyPrice * ((double) itemStack.getAmount() / product.getUnitAmount());
    }

    private class BulkTransactionImpl implements BulkTransaction {

        private final ItemStackCache<VirtualProduct> cache = new ItemStackCache<>();

        @Override
        public BigDecimal getSellPrice(OfflinePlayer offlinePlayer, ItemStack itemStack) {
            Player player = offlinePlayer.getPlayer();
            if (player != null) {
                return Optional.ofNullable(this.cache.computeIfAbsent(itemStack,
                                () -> getVirtualProductFor(itemStack, TradeType.SELL, player).orElse(null)))
                        .map(virtualProduct -> getSellPriceFor(virtualProduct, player, itemStack))
                        .map(BigDecimal::valueOf)
                        .orElseGet(() -> this.getSellPrice(itemStack));
            }

            return this.getSellPrice(itemStack);
        }

        @Override
        public BigDecimal getSellPrice(ItemStack itemStack) {
            return Optional.ofNullable(this.cache.computeIfAbsent(itemStack,
                            () -> getShopProduct(itemStack, TradeType.SELL).orElse(null)))
                    .map(virtualProduct -> getSellPriceFor(virtualProduct, null, itemStack))
                    .map(BigDecimal::valueOf)
                    .orElse(BigDecimal.ZERO);
        }

        @Override
        public BigDecimal getBuyPrice(OfflinePlayer offlinePlayer, ItemStack itemStack) {
            Player player = offlinePlayer.getPlayer();
            if (player != null) {
                return Optional.ofNullable(this.cache.computeIfAbsent(itemStack,
                                () -> getVirtualProductFor(itemStack, TradeType.BUY, player).orElse(null)))
                        .map(virtualProduct -> getBuyPriceFor(virtualProduct, player, itemStack))
                        .map(BigDecimal::valueOf)
                        .orElseGet(() -> this.getBuyPrice(itemStack));
            }

            return this.getBuyPrice(itemStack);
        }

        @Override
        public BigDecimal getBuyPrice(ItemStack itemStack) {
            return Optional.ofNullable(this.cache.computeIfAbsent(itemStack,
                            () -> getShopProduct(itemStack, TradeType.BUY).orElse(null)))
                    .map(virtualProduct -> getBuyPriceFor(virtualProduct, null, itemStack))
                    .map(BigDecimal::valueOf)
                    .orElse(BigDecimal.ZERO);
        }

    }

}
