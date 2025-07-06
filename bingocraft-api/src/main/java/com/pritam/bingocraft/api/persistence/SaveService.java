package com.pritam.bingocraft.api.persistence;

import java.util.concurrent.CompletableFuture;

public interface SaveService {
    CompletableFuture<Void> save(SaveableObject object, String key);
    <T extends SaveableObject> CompletableFuture<T> load(String key, Class<T> clazz);
    CompletableFuture<Boolean> delete(String key);
    CompletableFuture<Boolean> exists(String key);
}
