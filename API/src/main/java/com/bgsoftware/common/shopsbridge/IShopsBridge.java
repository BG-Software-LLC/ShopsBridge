package com.bgsoftware.common.shopsbridge;

import java.util.concurrent.CompletableFuture;

public interface IShopsBridge extends PricesAccessor {

    CompletableFuture<Void> getWhenShopsLoaded();

    default BulkTransaction startBulkTransaction() {
        return BulkTransaction.noBulk(this);
    }

}
