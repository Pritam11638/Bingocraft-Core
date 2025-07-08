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

    protected Config(String name, File folder, JavaPlugin plugin) {
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

    protected @NotNull String saveToString() {
        return this.config.saveToString();
    }

    protected @NotNull YamlConfigurationOptions options() {
        return this.config.options();
    }

    protected void save() throws IOException {
        this.config.save(file);
    }

    protected void addDefault(@NotNull String path, @Nullable Object value) {
        this.config.addDefault(path, value);
    }

    protected void addDefaults(@NotNull Map<String, Object> defaults) {
        this.config.addDefaults(defaults);
    }

    protected void addDefaults(@NotNull Configuration defaults) {
        this.config.addDefaults(defaults);
    }

    protected @Nullable Configuration getDefaults() {
        return this.config.getDefaults();
    }

    protected void setDefaults(@NotNull Configuration defaults) {
        this.config.setDefaults(defaults);
    }

    protected @Nullable ConfigurationSection getParent() {
        return this.config.getParent();
    }

    protected @NotNull Set<String> getKeys(boolean deep) {
        return this.config.getKeys(deep);
    }

    protected @NotNull Map<String, Object> getValues(boolean deep) {
        return this.config.getValues(deep);
    }

    protected boolean contains(@NotNull String path) {
        return this.config.contains(path);
    }

    protected boolean isSet(@NotNull String path) {
        return this.config.isSet(path);
    }

    protected boolean contains(@NotNull String path, boolean ignoreDefault) {
        return this.config.contains(path, ignoreDefault);
    }

    protected @NotNull String getCurrentPath() {
        return this.config.getCurrentPath();
    }

    protected @Nullable Configuration getRoot() {
        return this.config.getRoot();
    }

    protected @NotNull String getName() {
        return this.config.getName();
    }

    protected @Nullable ConfigurationSection getDefaultSection() {
        return this.config.getDefaultSection();
    }

    protected void set(@NotNull String path, @Nullable Object value) {
        this.config.set(path, value);
    }

    protected @Nullable Object get(@NotNull String path) {
        return this.config.get(path);
    }

    protected @Nullable Object get(@NotNull String path, @Nullable Object def) {
        return this.config.get(path, def);
    }

    protected @NotNull ConfigurationSection createSection(@NotNull String path) {
        return this.config.createSection(path);
    }

    protected @NotNull ConfigurationSection createSection(@NotNull String path, @NotNull Map<?, ?> map) {
        return this.config.createSection(path, map);
    }

    protected @Nullable String getString(@NotNull String path) {
        return this.config.getString(path);
    }

    protected boolean isString(@NotNull String path) {
        return this.config.isString(path);
    }

    protected int getInt(@NotNull String path) {
        return this.config.getInt(path);
    }

    protected @Nullable String getString(@NotNull String path, @Nullable String def) {
        return this.config.getString(path, def);
    }

    protected boolean isInt(@NotNull String path) {
        return this.config.isInt(path);
    }

    protected boolean getBoolean(@NotNull String path) {
        return this.config.getBoolean(path);
    }

    protected int getInt(@NotNull String path, int def) {
        return this.config.getInt(path, def);
    }

    protected boolean getBoolean(@NotNull String path, boolean def) {
        return this.config.getBoolean(path, def);
    }

    protected boolean isBoolean(@NotNull String path) {
        return this.config.isBoolean(path);
    }

    protected double getDouble(@NotNull String path) {
        return this.config.getDouble(path);
    }

    protected double getDouble(@NotNull String path, double def) {
        return this.config.getDouble(path, def);
    }

    protected boolean isDouble(@NotNull String path) {
        return this.config.isDouble(path);
    }

    protected long getLong(@NotNull String path, long def) {
        return this.config.getLong(path, def);
    }

    protected long getLong(@NotNull String path) {
        return this.config.getLong(path);
    }

    protected boolean isLong(@NotNull String path) {
        return this.config.isLong(path);
    }

    protected @Nullable List<?> getList(@NotNull String path, @Nullable List<?> def) {
        return this.config.getList(path, def);
    }

    protected @Nullable List<?> getList(@NotNull String path) {
        return this.config.getList(path);
    }

    protected boolean isList(@NotNull String path) {
        return this.config.isList(path);
    }

    protected @NotNull List<String> getStringList(@NotNull String path) {
        return this.config.getStringList(path);
    }

    protected @NotNull List<Integer> getIntegerList(@NotNull String path) {
        return this.config.getIntegerList(path);
    }

    protected @NotNull List<Boolean> getBooleanList(@NotNull String path) {
        return this.config.getBooleanList(path);
    }

    protected @NotNull List<Double> getDoubleList(@NotNull String path) {
        return this.config.getDoubleList(path);
    }

    protected @NotNull List<Float> getFloatList(@NotNull String path) {
        return this.config.getFloatList(path);
    }

    protected @NotNull List<Long> getLongList(@NotNull String path) {
        return this.config.getLongList(path);
    }

    protected @NotNull List<Byte> getByteList(@NotNull String path) {
        return this.config.getByteList(path);
    }

    protected @NotNull List<Character> getCharacterList(@NotNull String path) {
        return this.config.getCharacterList(path);
    }

    protected @NotNull List<Short> getShortList(@NotNull String path) {
        return this.config.getShortList(path);
    }

    protected <T> @Nullable T getObject(@NotNull String path, @NotNull Class<T> clazz) {
        return this.config.getObject(path, clazz);
    }

    protected <T> @Nullable T getObject(@NotNull String path, @NotNull Class<T> clazz, @Nullable T def) {
        return this.config.getObject(path, clazz, def);
    }

    protected <T extends ConfigurationSerializable> @Nullable T getSerializable(@NotNull String path, @NotNull Class<T> clazz, @Nullable T def) {
        return this.config.getSerializable(path, clazz, def);
    }

    protected @NotNull List<Map<?, ?>> getMapList(@NotNull String path) {
        return this.config.getMapList(path);
    }

    protected <T extends ConfigurationSerializable> @Nullable T getSerializable(@NotNull String path, @NotNull Class<T> clazz) {
        return this.config.getSerializable(path, clazz);
    }

    protected @Nullable Vector getVector(@NotNull String path) {
        return this.config.getVector(path);
    }

    protected @Nullable Vector getVector(@NotNull String path, @Nullable Vector def) {
        return this.config.getVector(path, def);
    }

    protected @Nullable OfflinePlayer getOfflinePlayer(@NotNull String path) {
        return this.config.getOfflinePlayer(path);
    }

    protected @Nullable OfflinePlayer getOfflinePlayer(@NotNull String path, @Nullable OfflinePlayer def) {
        return this.config.getOfflinePlayer(path, def);
    }

    protected boolean isOfflinePlayer(@NotNull String path) {
        return this.config.isOfflinePlayer(path);
    }

    protected boolean isVector(@NotNull String path) {
        return this.config.isVector(path);
    }

    protected @Nullable ItemStack getItemStack(@NotNull String path) {
        return this.config.getItemStack(path);
    }

    protected @Nullable ItemStack getItemStack(@NotNull String path, @Nullable ItemStack def) {
        return this.config.getItemStack(path, def);
    }

    protected @Nullable Color getColor(@NotNull String path) {
        return this.config.getColor(path);
    }

    protected boolean isItemStack(@NotNull String path) {
        return this.config.isItemStack(path);
    }

    protected @Nullable Color getColor(@NotNull String path, @Nullable Color def) {
        return this.config.getColor(path, def);
    }

    protected @Nullable Location getLocation(@NotNull String path) {
        return this.config.getLocation(path);
    }

    protected boolean isColor(@NotNull String path) {
        return this.config.isColor(path);
    }

    protected @Nullable Location getLocation(@NotNull String path, @Nullable Location def) {
        return this.config.getLocation(path, def);
    }

    protected boolean isLocation(@NotNull String path) {
        return this.config.isLocation(path);
    }

    protected @Nullable ConfigurationSection getConfigurationSection(@NotNull String path) {
        return this.config.getConfigurationSection(path);
    }

    protected boolean isConfigurationSection(@NotNull String path) {
        return this.config.isConfigurationSection(path);
    }

    protected @NotNull List<String> getComments(@NotNull String path) {
        return this.config.getComments(path);
    }

    protected @NotNull List<String> getInlineComments(@NotNull String path) {
        return this.config.getInlineComments(path);
    }

    protected void setComments(@NotNull String path, @Nullable List<String> comments) {
        this.config.setComments(path, comments);
    }

    protected void setInlineComments(@NotNull String path, @Nullable List<String> comments) {
        this.config.setInlineComments(path, comments);
    }

    public String toString() {
        return this.config.toString();
    }

    protected @Nullable Component getRichMessage(@NotNull String path) {
        return this.config.getRichMessage(path);
    }

    protected @Nullable Component getRichMessage(@NotNull String path, @Nullable Component fallback) {
        return this.config.getRichMessage(path, fallback);
    }

    protected void setRichMessage(@NotNull String path, @Nullable Component value) {
        this.config.setRichMessage(path, value);
    }

    protected <C extends Component> @Nullable C getComponent(@NotNull String path, @NotNull ComponentDecoder<? super String, C> decoder) {
        return this.config.getComponent(path, decoder);
    }

    protected <C extends Component> @Nullable C getComponent(@NotNull String path, @NotNull ComponentDecoder<? super String, C> decoder, @Nullable C fallback) {
        return this.config.getComponent(path, decoder, fallback);
    }

    protected <C extends Component> void setComponent(@NotNull String path, @NotNull ComponentEncoder<C, String> encoder, @Nullable C value) {
        this.config.setComponent(path, encoder, value);
    }
}
