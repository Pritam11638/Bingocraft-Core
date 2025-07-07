package com.pritam.bingocraft.api.persistence;

import java.util.concurrent.CompletableFuture;

public interface SaveService {
    SaveServiceReturnCode save(String key, SaveableObject object);
    <T extends SaveableObject> CompletableFuture<SaveServiceReturnCode> load(String key, T emptyInstace);
    CompletableFuture<SaveServiceReturnCode> delete(String key);
    CompletableFuture<SaveServiceReturnCode> exists(String key);
}
