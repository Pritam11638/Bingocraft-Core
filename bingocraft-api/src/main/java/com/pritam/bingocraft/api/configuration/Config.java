package com.pritam.bingocraft.api.configuration;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.ComponentDecoder;
import net.kyori.adventure.text.serializer.ComponentEncoder;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.file.YamlConfigurationOptions;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Config {
    private final File file;
    private final YamlConfiguration config;

    public Config(String name, File folder, JavaPlugin plugin) {
        this.file = new File(folder, name + ".yml");

        try {
            if (!folder.exists() && !folder.mkdirs()) {
                throw new IOException("Failed to create config folder: " + folder.getAbsolutePath());
            }

            if (!file.exists() && !file.createNewFile()) {
                throw new IOException("Failed to create config file: " + file.getAbsolutePath());
            }

            this.config = YamlConfiguration.loadConfiguration(file);

            config.options().copyDefaults(true);
            save();
        } catch (IOException e) {
            plugin.getLogger().severe("Error occurred while initializing config file: " + name + ".yml");
            plugin.getLogger().severe(e.getMessage());
            Bukkit.getPluginManager().disablePlugin(plugin);
            throw new RuntimeException("Could not initialize configuration", e);
        }
    }

    public @NotNull String saveToString() {
        return this.config.saveToString();
    }

    public @NotNull YamlConfigurationOptions options() {
        return this.config.options();
    }

    public void save() throws IOException {
        this.config.save(file);
    }

    public void addDefault(@NotNull String path, @Nullable Object value) {
        this.config.addDefault(path, value);
    }

    public void addDefaults(@NotNull Map<String, Object> defaults) {
        this.config.addDefaults(defaults);
    }

    public void addDefaults(@NotNull Configuration defaults) {
        this.config.addDefaults(defaults);
    }

    public @Nullable Configuration getDefaults() {
        return this.config.getDefaults();
    }

    public void setDefaults(@NotNull Configuration defaults) {
        this.config.setDefaults(defaults);
    }

    public @Nullable ConfigurationSection getParent() {
        return this.config.getParent();
    }

    public @NotNull Set<String> getKeys(boolean deep) {
        return this.config.getKeys(deep);
    }

    public @NotNull Map<String, Object> getValues(boolean deep) {
        return this.config.getValues(deep);
    }

    public boolean contains(@NotNull String path) {
        return this.config.contains(path);
    }

    public boolean isSet(@NotNull String path) {
        return this.config.isSet(path);
    }

    public boolean contains(@NotNull String path, boolean ignoreDefault) {
        return this.config.contains(path, ignoreDefault);
    }

    public @NotNull String getCurrentPath() {
        return this.config.getCurrentPath();
    }

    public @Nullable Configuration getRoot() {
        return this.config.getRoot();
    }

    public @NotNull String getName() {
        return this.config.getName();
    }

    public @Nullable ConfigurationSection getDefaultSection() {
        return this.config.getDefaultSection();
    }

    public void set(@NotNull String path, @Nullable Object value) {
        this.config.set(path, value);
    }

    public @Nullable Object get(@NotNull String path) {
        return this.config.get(path);
    }

    public @Nullable Object get(@NotNull String path, @Nullable Object def) {
        return this.config.get(path, def);
    }

    public @NotNull ConfigurationSection createSection(@NotNull String path) {
        return this.config.createSection(path);
    }

    public @NotNull ConfigurationSection createSection(@NotNull String path, @NotNull Map<?, ?> map) {
        return this.config.createSection(path, map);
    }

    public @Nullable String getString(@NotNull String path) {
        return this.config.getString(path);
    }

    public boolean isString(@NotNull String path) {
        return this.config.isString(path);
    }

    public int getInt(@NotNull String path) {
        return this.config.getInt(path);
    }

    public @Nullable String getString(@NotNull String path, @Nullable String def) {
        return this.config.getString(path, def);
    }

    public boolean isInt(@NotNull String path) {
        return this.config.isInt(path);
    }

    public boolean getBoolean(@NotNull String path) {
        return this.config.getBoolean(path);
    }

    public int getInt(@NotNull String path, int def) {
        return this.config.getInt(path, def);
    }

    public boolean getBoolean(@NotNull String path, boolean def) {
        return this.config.getBoolean(path, def);
    }

    public boolean isBoolean(@NotNull String path) {
        return this.config.isBoolean(path);
    }

    public double getDouble(@NotNull String path) {
        return this.config.getDouble(path);
    }

    public double getDouble(@NotNull String path, double def) {
        return this.config.getDouble(path, def);
    }

    public boolean isDouble(@NotNull String path) {
        return this.config.isDouble(path);
    }

    public long getLong(@NotNull String path, long def) {
        return this.config.getLong(path, def);
    }

    public long getLong(@NotNull String path) {
        return this.config.getLong(path);
    }

    public boolean isLong(@NotNull String path) {
        return this.config.isLong(path);
    }

    public @Nullable List<?> getList(@NotNull String path, @Nullable List<?> def) {
        return this.config.getList(path, def);
    }

    public @Nullable List<?> getList(@NotNull String path) {
        return this.config.getList(path);
    }

    public boolean isList(@NotNull String path) {
        return this.config.isList(path);
    }

    public @NotNull List<String> getStringList(@NotNull String path) {
        return this.config.getStringList(path);
    }

    public @NotNull List<Integer> getIntegerList(@NotNull String path) {
        return this.config.getIntegerList(path);
    }

    public @NotNull List<Boolean> getBooleanList(@NotNull String path) {
        return this.config.getBooleanList(path);
    }

    public @NotNull List<Double> getDoubleList(@NotNull String path) {
        return this.config.getDoubleList(path);
    }

    public @NotNull List<Float> getFloatList(@NotNull String path) {
        return this.config.getFloatList(path);
    }

    public @NotNull List<Long> getLongList(@NotNull String path) {
        return this.config.getLongList(path);
    }

    public @NotNull List<Byte> getByteList(@NotNull String path) {
        return this.config.getByteList(path);
    }

    public @NotNull List<Character> getCharacterList(@NotNull String path) {
        return this.config.getCharacterList(path);
    }

    public @NotNull List<Short> getShortList(@NotNull String path) {
        return this.config.getShortList(path);
    }

    public <T> @Nullable T getObject(@NotNull String path, @NotNull Class<T> clazz) {
        return this.config.getObject(path, clazz);
    }

    public <T> @Nullable T getObject(@NotNull String path, @NotNull Class<T> clazz, @Nullable T def) {
        return this.config.getObject(path, clazz, def);
    }

    public <T extends ConfigurationSerializable> @Nullable T getSerializable(@NotNull String path, @NotNull Class<T> clazz, @Nullable T def) {
        return this.config.getSerializable(path, clazz, def);
    }

    public @NotNull List<Map<?, ?>> getMapList(@NotNull String path) {
        return this.config.getMapList(path);
    }

    public <T extends ConfigurationSerializable> @Nullable T getSerializable(@NotNull String path, @NotNull Class<T> clazz) {
        return this.config.getSerializable(path, clazz);
    }

    public @Nullable Vector getVector(@NotNull String path) {
        return this.config.getVector(path);
    }

    public @Nullable Vector getVector(@NotNull String path, @Nullable Vector def) {
        return this.config.getVector(path, def);
    }

    public @Nullable OfflinePlayer getOfflinePlayer(@NotNull String path) {
        return this.config.getOfflinePlayer(path);
    }

    public @Nullable OfflinePlayer getOfflinePlayer(@NotNull String path, @Nullable OfflinePlayer def) {
        return this.config.getOfflinePlayer(path, def);
    }

    public boolean isOfflinePlayer(@NotNull String path) {
        return this.config.isOfflinePlayer(path);
    }

    public boolean isVector(@NotNull String path) {
        return this.config.isVector(path);
    }

    public @Nullable ItemStack getItemStack(@NotNull String path) {
        return this.config.getItemStack(path);
    }

    public @Nullable ItemStack getItemStack(@NotNull String path, @Nullable ItemStack def) {
        return this.config.getItemStack(path, def);
    }

    public @Nullable Color getColor(@NotNull String path) {
        return this.config.getColor(path);
    }

    public boolean isItemStack(@NotNull String path) {
        return this.config.isItemStack(path);
    }

    public @Nullable Color getColor(@NotNull String path, @Nullable Color def) {
        return this.config.getColor(path, def);
    }

    public @Nullable Location getLocation(@NotNull String path) {
        return this.config.getLocation(path);
    }

    public boolean isColor(@NotNull String path) {
        return this.config.isColor(path);
    }

    public @Nullable Location getLocation(@NotNull String path, @Nullable Location def) {
        return this.config.getLocation(path, def);
    }

    public boolean isLocation(@NotNull String path) {
        return this.config.isLocation(path);
    }

    public @Nullable ConfigurationSection getConfigurationSection(@NotNull String path) {
        return this.config.getConfigurationSection(path);
    }

    public boolean isConfigurationSection(@NotNull String path) {
        return this.config.isConfigurationSection(path);
    }

    public @NotNull List<String> getComments(@NotNull String path) {
        return this.config.getComments(path);
    }

    public @NotNull List<String> getInlineComments(@NotNull String path) {
        return this.config.getInlineComments(path);
    }

    public void setComments(@NotNull String path, @Nullable List<String> comments) {
        this.config.setComments(path, comments);
    }

    public void setInlineComments(@NotNull String path, @Nullable List<String> comments) {
        this.config.setInlineComments(path, comments);
    }

    public String toString() {
        return this.config.toString();
    }

    public @Nullable Component getRichMessage(@NotNull String path) {
        return this.config.getRichMessage(path);
    }

    public @Nullable Component getRichMessage(@NotNull String path, @Nullable Component fallback) {
        return this.config.getRichMessage(path, fallback);
    }

    public void setRichMessage(@NotNull String path, @Nullable Component value) {
        this.config.setRichMessage(path, value);
    }

    public <C extends Component> @Nullable C getComponent(@NotNull String path, @NotNull ComponentDecoder<? super String, C> decoder) {
        return this.config.getComponent(path, decoder);
    }

    public <C extends Component> @Nullable C getComponent(@NotNull String path, @NotNull ComponentDecoder<? super String, C> decoder, @Nullable C fallback) {
        return this.config.getComponent(path, decoder, fallback);
    }

    public <C extends Component> void setComponent(@NotNull String path, @NotNull ComponentEncoder<C, String> encoder, @Nullable C value) {
        this.config.setComponent(path, encoder, value);
    }
}
