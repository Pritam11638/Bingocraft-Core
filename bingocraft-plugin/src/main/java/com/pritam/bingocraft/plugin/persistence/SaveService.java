package com.pritam.bingocraft.plugin.persistence;

import com.pritam.bingocraft.api.persistence.SaveableObject;
import com.pritam.bingocraft.plugin.BingocraftCore;
import lombok.Getter;

import java.sql.*;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

public class SaveService implements com.pritam.bingocraft.api.persistence.SaveService {
    @Getter private final boolean enabled;
    Connection connection = null;

    public SaveService(boolean enable) {
        if (enable) {
            try {
                connection = DriverManager.getConnection("jdbc:sqlite:" + BingocraftCore.getPlugin().getDataFolder() + "/bingocraft.db");

                try (Statement statement = connection.createStatement()) {
                    statement.execute("CREATE TABLE IF NOT EXISTS saved_objects (key VARCHAR(255) PRIMARY KEY, data LONGTEXT);");
                }
            } catch (SQLException e) {
                enable = false;
                BingocraftCore.getPlugin().getLogger().log(Level.WARNING, "Could not connect to the database!", e);
            }
        }

        this.enabled = enable;
    }

    @Override
    public CompletableFuture<Void> save(SaveableObject object, String key) {
        if (!enabled) {
            return CompletableFuture.completedFuture(null);
        }

        return CompletableFuture.supplyAsync(() -> {
            try {
                PreparedStatement statement = connection.prepareStatement("INSERT OR REPLACE INTO saved_objects (key, data) VALUES (?, ?);");
                statement.setString(1, key);
                statement.setString(2, object.toString());
            } catch (SQLException e) {
                BingocraftCore.getPlugin().getLogger().log(Level.SEVERE, "Could not connect to the database!", e);
            }

            return null;
        });
    }

    @Override
    public <T extends SaveableObject> CompletableFuture<T> load(String key, Class<T> clazz) {
        return CompletableFuture.supplyAsync(() -> {
            if (!enabled) {
                return null;
            }

            try {
                PreparedStatement statement = connection.prepareStatement("SELECT data FROM saved_objects WHERE key = ?;");
                statement.setString(1, key);
                ResultSet resultSet = statement.executeQuery();

                if (resultSet.next()) {
                    String data = resultSet.getString("data");
                    T object = clazz.getDeclaredConstructor().newInstance();
                    object.fromString(data);
                    return object;
                }
            } catch (SQLException | ReflectiveOperationException e) {
                BingocraftCore.getPlugin().getLogger().log(Level.SEVERE, "Could not load object from the database!", e);
            }

            return null;
        });
    }

    @Override
    public CompletableFuture<Boolean> delete(String key) {
        if (!enabled) {
            return CompletableFuture.completedFuture(false);
        }

        return CompletableFuture.supplyAsync(() -> {
            try {
                PreparedStatement statement = connection.prepareStatement("DELETE FROM saved_objects WHERE key = ?;");
                statement.setString(1, key);
                int rowsAffected = statement.executeUpdate();
                return rowsAffected > 0;
            } catch (SQLException e) {
                BingocraftCore.getPlugin().getLogger().log(Level.SEVERE, "Could not delete object from the database!", e);
                return false;
            }
        });
    }

    @Override
    public CompletableFuture<Boolean> exists(String key) {
        return CompletableFuture.supplyAsync(() -> {
            if (!enabled) {
                return false;
            }

            try {
                PreparedStatement statement = connection.prepareStatement("SELECT 1 FROM saved_objects WHERE key = ?;");
                statement.setString(1, key);
                ResultSet resultSet = statement.executeQuery();
                return resultSet.next();
            } catch (SQLException e) {
                BingocraftCore.getPlugin().getLogger().log(Level.SEVERE, "Could not check existence of object in the database!", e);
                return false;
            }
        });
    }
}
