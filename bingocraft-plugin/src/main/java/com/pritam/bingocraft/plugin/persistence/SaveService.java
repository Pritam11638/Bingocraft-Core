package com.pritam.bingocraft.plugin.persistence;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheStats;
import com.pritam.bingocraft.api.persistence.SaveableObject;
import com.pritam.bingocraft.api.persistence.SaveServiceReturnCode;
import com.pritam.bingocraft.api.utils.Pair;
import com.pritam.bingocraft.plugin.BingocraftCore;
import lombok.Getter;
import org.bukkit.Bukkit;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

/**
 * Thread‑safe, write‑behind persistence layer backed by SQLite and a Guava cache.
 */
public class SaveService implements com.pritam.bingocraft.api.persistence.SaveService {
    @Getter
    private final boolean enabled;

    /** Objects waiting to be flushed to disk. */
    private final Set<Pair<String, SaveableObject>> queuedObjects = ConcurrentHashMap.newKeySet();

    /** In‑memory cache that expires after access and caps its size. */
    private final Cache<String, SaveableObject> cachedObjects;

    /** SQLite connection (WAL, busy‑timeout, etc.). */
    private Connection connection;

    public SaveService() {
        SaveServiceMeta meta = BingocraftCore.getMainConfig().getSaveServiceMeta();

        Cache<String, SaveableObject> cache = CacheBuilder.newBuilder().maximumSize(0).build();
        boolean serviceEnabled = false;

        if (meta.isEnabled()) {
            try {
                String dbUrl = "jdbc:sqlite:" + BingocraftCore.getPlugin().getDataFolder() + "/bingocraft.db?busy_timeout=5000";
                connection = DriverManager.getConnection(dbUrl);

                try (Statement stmt = connection.createStatement()) {
                    stmt.execute("PRAGMA journal_mode=WAL;");
                    stmt.execute("PRAGMA cache_size=10000;");
                    stmt.execute("PRAGMA synchronous=NORMAL;");
                    stmt.execute("PRAGMA mmap_size=268435456;");

                    stmt.executeUpdate("CREATE TABLE IF NOT EXISTS saved_objects (key TEXT PRIMARY KEY, data TEXT);");
                }

                cache = CacheBuilder.newBuilder()
                        .maximumSize(meta.getCacheSize())
                        .expireAfterAccess(meta.getCacheDuration(), TimeUnit.SECONDS)
                        .build();

                serviceEnabled = true;
                startSaveTask(meta.getSaveInterval());
            } catch (SQLException e) {
                BingocraftCore.getPlugin().getLogger().log(Level.WARNING, "SaveService disabled: database connection failure", e);
            }
        }

        this.cachedObjects = cache;
        this.enabled = serviceEnabled;
    }

    @Override
    public SaveServiceReturnCode save(String key, SaveableObject object) {
        if (!enabled) return SaveServiceReturnCode.OFFLINE;
        if (key == null || key.isBlank()) return SaveServiceReturnCode.INVALID_KEY;

        queuedObjects.add(new Pair<>(key, object));
        cachedObjects.put(key, object);

        return SaveServiceReturnCode.SUCCESS;
    }

    @Override
    public <T extends SaveableObject> CompletableFuture<SaveServiceReturnCode> load(String key, T emptyInstance) {
        if (!enabled) return CompletableFuture.completedFuture(SaveServiceReturnCode.OFFLINE);
        if (key == null || key.isBlank()) return CompletableFuture.completedFuture(SaveServiceReturnCode.INVALID_KEY);

        return CompletableFuture.supplyAsync(() -> {
            SaveableObject cached = cachedObjects.getIfPresent(key);

            if (cached != null) {
                emptyInstance.fromString(cached.toString());
                return SaveServiceReturnCode.SUCCESS;
            }

            try (PreparedStatement stmt = connection.prepareStatement("SELECT data FROM saved_objects WHERE key = ?")) {
                stmt.setString(1, key);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        emptyInstance.fromString(rs.getString("data"));
                        cachedObjects.put(key, emptyInstance);
                        return SaveServiceReturnCode.SUCCESS;
                    }
                }
            } catch (SQLException e) {
                BingocraftCore.getPlugin().getLogger().log(Level.SEVERE, "Error loading key " + key, e);
                return SaveServiceReturnCode.SQL_ERROR;
            }

            return SaveServiceReturnCode.KEY_NOT_FOUND;
        });
    }

    @Override
    public CompletableFuture<SaveServiceReturnCode> delete(String key) {
        if (!enabled) return CompletableFuture.completedFuture(SaveServiceReturnCode.OFFLINE);
        if (key == null || key.isBlank()) return CompletableFuture.completedFuture(SaveServiceReturnCode.INVALID_KEY);

        return CompletableFuture.supplyAsync(() -> {
            cachedObjects.invalidate(key);

            try (PreparedStatement stmt = connection.prepareStatement("DELETE FROM saved_objects WHERE key = ?")) {
                stmt.setString(1, key);
                return stmt.executeUpdate() > 0 ? SaveServiceReturnCode.SUCCESS : SaveServiceReturnCode.KEY_NOT_FOUND;
            } catch (SQLException e) {
                BingocraftCore.getPlugin().getLogger().log(Level.SEVERE, "Error deleting key " + key, e);
                return SaveServiceReturnCode.SQL_ERROR;
            }
        });
    }

    @Override
    public CompletableFuture<SaveServiceReturnCode> exists(String key) {
        if (!enabled) return CompletableFuture.completedFuture(SaveServiceReturnCode.OFFLINE);
        if (key == null || key.isBlank()) return CompletableFuture.completedFuture(SaveServiceReturnCode.INVALID_KEY);

        return CompletableFuture.supplyAsync(() -> {
            if (cachedObjects.getIfPresent(key) != null) return SaveServiceReturnCode.EXISTS;

            try (PreparedStatement stmt = connection.prepareStatement("SELECT 1 FROM saved_objects WHERE key = ?")) {
                stmt.setString(1, key);
                try (ResultSet rs = stmt.executeQuery()) {
                    return rs.next() ? SaveServiceReturnCode.EXISTS : SaveServiceReturnCode.NOT_EXISTS;
                }
            } catch (SQLException e) {
                BingocraftCore.getPlugin().getLogger().log(Level.SEVERE, "Exists check failed for key " + key, e);
                return SaveServiceReturnCode.SQL_ERROR;
            }
        });
    }

    private void startSaveTask(int saveIntervalSeconds) {
        Bukkit.getScheduler().runTaskTimerAsynchronously(
                BingocraftCore.getPlugin(),
                this::flushQueuedObjects,
                saveIntervalSeconds * 20L,
                saveIntervalSeconds * 20L);
    }

    /**
     * Drains the queue atomically and writes everything to SQLite in a single batch.
     */
    private void flushQueuedObjects() {
        if (!enabled || queuedObjects.isEmpty()) return;

        List<Pair<String, SaveableObject>> snapshot = new ArrayList<>(queuedObjects);
        snapshot.forEach(queuedObjects::remove);

        try (PreparedStatement stmt = connection.prepareStatement(
                "INSERT OR REPLACE INTO saved_objects (key, data) VALUES (?, ?);")) {
            for (Pair<String, SaveableObject> pair : snapshot) {
                stmt.setString(1, pair.primary());
                stmt.setString(2, pair.secondary().toString());
                stmt.addBatch();
            }

            stmt.executeBatch();
        } catch (SQLException e) {
            BingocraftCore.getPlugin().getLogger().log(Level.SEVERE, "Batch save failed", e);
            queuedObjects.addAll(snapshot);
        }
    }

    /** Flush everything and close the DB on plugin disable. */
    public void shutdown() {
        if (!enabled) return;

        flushQueuedObjects();
        try {
            connection.close();
            BingocraftCore.getPlugin().getLogger().info("SaveService shut down.");
        } catch (SQLException e) {
            BingocraftCore.getPlugin().getLogger().log(Level.WARNING, "Error closing database connection", e);
        }
    }
}