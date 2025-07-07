package com.pritam.bingocraft.plugin.diagnostic;

import com.google.common.cache.CacheStats;
import com.pritam.bingocraft.api.persistence.SaveServiceReturnCode;
import com.pritam.bingocraft.api.persistence.SaveableObject;
import com.pritam.bingocraft.plugin.BingocraftCore;
import com.pritam.bingocraft.plugin.persistence.SaveService;
import com.pritam.bingocraft.plugin.persistence.SaveServiceMeta;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Logger;

/**
 * Diagnostic command for testing and troubleshooting SaveService functionality.
 */
public class SaveServiceDiagnosticCommand implements CommandExecutor, TabCompleter {
    
    private final Logger logger;
    private final SaveService saveService;
    
    public SaveServiceDiagnosticCommand() {
        this.logger = BingocraftCore.getPlugin().getLogger();
        this.saveService = BingocraftCore.getSaveService();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Check permissions
        if (!sender.hasPermission("bingocraft.admin")) {
            sender.sendMessage(Component.text("You don't have permission to use this command.", NamedTextColor.RED));
            return true;
        }
        
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        String subCommand = args[0].toLowerCase();
        
        switch (subCommand) {
            case "status":
                runStatusCheck(sender);
                break;
            case "config":
                runConfigCheck(sender);
                break;
            case "database":
                runDatabaseCheck(sender);
                break;
            case "serialization":
                runSerializationTest(sender);
                break;
            case "operations":
                runOperationsTest(sender);
                break;
            case "cache":
                runCacheTest(sender);
                break;
            case "stress":
                int count = args.length > 1 ? parseCount(args[1]) : 100;
                runStressTest(sender, count);
                break;
            case "full":
                runFullDiagnostic(sender);
                break;
            default:
                sendHelp(sender);
                break;
        }
        
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return List.of("status", "config", "database", "serialization", "operations", "cache", "stress", "full");
        } else if (args.length == 2 && "stress".equals(args[0])) {
            return List.of("10", "50", "100", "500", "1000");
        }
        return new ArrayList<>();
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(Component.text("=== SaveService Diagnostic Commands ===", NamedTextColor.YELLOW));
        sender.sendMessage(Component.text("/saveservice-diagnostic status - Check service enablement", NamedTextColor.GRAY));
        sender.sendMessage(Component.text("/saveservice-diagnostic config - Check configuration", NamedTextColor.GRAY));
        sender.sendMessage(Component.text("/saveservice-diagnostic database - Test database connectivity", NamedTextColor.GRAY));
        sender.sendMessage(Component.text("/saveservice-diagnostic serialization - Test object serialization", NamedTextColor.GRAY));
        sender.sendMessage(Component.text("/saveservice-diagnostic operations - Test CRUD operations", NamedTextColor.GRAY));
        sender.sendMessage(Component.text("/saveservice-diagnostic cache - Test cache functionality", NamedTextColor.GRAY));
        sender.sendMessage(Component.text("/saveservice-diagnostic stress [count] - Run stress test", NamedTextColor.GRAY));
        sender.sendMessage(Component.text("/saveservice-diagnostic full - Run all diagnostics", NamedTextColor.GRAY));
    }

    private void runStatusCheck(CommandSender sender) {
        sender.sendMessage(Component.text("=== SaveService Status Check ===", NamedTextColor.YELLOW));
        
        try {
            boolean enabled = saveService.isEnabled();
            sender.sendMessage(Component.text("Service Enabled: " + enabled, 
                enabled ? NamedTextColor.GREEN : NamedTextColor.RED));
            
            if (!enabled) {
                sender.sendMessage(Component.text("❌ SaveService is disabled - check configuration", NamedTextColor.RED));
                return;
            }
            
            sender.sendMessage(Component.text("✅ SaveService is enabled and operational", NamedTextColor.GREEN));
            
        } catch (Exception e) {
            sender.sendMessage(Component.text("❌ Error checking status: " + e.getMessage(), NamedTextColor.RED));
            logger.severe("Status check error: " + e.getMessage());
        }
    }

    private void runConfigCheck(CommandSender sender) {
        sender.sendMessage(Component.text("=== Configuration Check ===", NamedTextColor.YELLOW));
        
        try {
            SaveServiceMeta meta = BingocraftCore.getMainConfig().getSaveServiceMeta();
            
            sender.sendMessage(Component.text("Enabled: " + meta.isEnabled(), 
                meta.isEnabled() ? NamedTextColor.GREEN : NamedTextColor.RED));
            sender.sendMessage(Component.text("Save Interval: " + meta.getSaveInterval() + " seconds", NamedTextColor.GRAY));
            sender.sendMessage(Component.text("Cache Duration: " + meta.getCacheDuration() + " seconds", NamedTextColor.GRAY));
            sender.sendMessage(Component.text("Cache Size: " + meta.getCacheSize(), NamedTextColor.GRAY));
            
            // Validate configuration values
            boolean configValid = true;
            if (meta.getSaveInterval() <= 0) {
                sender.sendMessage(Component.text("❌ Invalid save interval: " + meta.getSaveInterval(), NamedTextColor.RED));
                configValid = false;
            }
            if (meta.getCacheDuration() <= 0) {
                sender.sendMessage(Component.text("❌ Invalid cache duration: " + meta.getCacheDuration(), NamedTextColor.RED));
                configValid = false;
            }
            if (meta.getCacheSize() <= 0) {
                sender.sendMessage(Component.text("❌ Invalid cache size: " + meta.getCacheSize(), NamedTextColor.RED));
                configValid = false;
            }
            
            if (configValid) {
                sender.sendMessage(Component.text("✅ Configuration is valid", NamedTextColor.GREEN));
            }
            
        } catch (Exception e) {
            sender.sendMessage(Component.text("❌ Error checking configuration: " + e.getMessage(), NamedTextColor.RED));
            logger.severe("Config check error: " + e.getMessage());
        }
    }

    private void runDatabaseCheck(CommandSender sender) {
        sender.sendMessage(Component.text("=== Database Connectivity Check ===", NamedTextColor.YELLOW));
        
        try {
            // Check if database file exists
            File dbFile = new File(BingocraftCore.getPlugin().getDataFolder(), "bingocraft.db");
            sender.sendMessage(Component.text("Database file exists: " + dbFile.exists(), 
                dbFile.exists() ? NamedTextColor.GREEN : NamedTextColor.YELLOW));
            
            if (dbFile.exists()) {
                sender.sendMessage(Component.text("Database size: " + dbFile.length() + " bytes", NamedTextColor.GRAY));
            }
            
            // Test database connection
            String dbUrl = "jdbc:sqlite:" + BingocraftCore.getPlugin().getDataFolder() + "/bingocraft.db?busy_timeout=5000";
            
            try (Connection conn = DriverManager.getConnection(dbUrl)) {
                sender.sendMessage(Component.text("✅ Database connection successful", NamedTextColor.GREEN));
                
                // Check if table exists
                try (PreparedStatement stmt = conn.prepareStatement(
                    "SELECT name FROM sqlite_master WHERE type='table' AND name='saved_objects'")) {
                    try (ResultSet rs = stmt.executeQuery()) {
                        if (rs.next()) {
                            sender.sendMessage(Component.text("✅ Table 'saved_objects' exists", NamedTextColor.GREEN));
                            
                            // Check table structure
                            try (PreparedStatement structStmt = conn.prepareStatement("PRAGMA table_info(saved_objects)")) {
                                try (ResultSet structRs = structStmt.executeQuery()) {
                                    sender.sendMessage(Component.text("Table structure:", NamedTextColor.GRAY));
                                    while (structRs.next()) {
                                        String columnName = structRs.getString("name");
                                        String columnType = structRs.getString("type");
                                        sender.sendMessage(Component.text("  " + columnName + " (" + columnType + ")", NamedTextColor.GRAY));
                                    }
                                }
                            }
                            
                            // Check record count
                            try (PreparedStatement countStmt = conn.prepareStatement("SELECT COUNT(*) FROM saved_objects")) {
                                try (ResultSet countRs = countStmt.executeQuery()) {
                                    if (countRs.next()) {
                                        int count = countRs.getInt(1);
                                        sender.sendMessage(Component.text("Records in database: " + count, NamedTextColor.GRAY));
                                    }
                                }
                            }
                            
                        } else {
                            sender.sendMessage(Component.text("❌ Table 'saved_objects' does not exist", NamedTextColor.RED));
                        }
                    }
                }
                
            } catch (SQLException e) {
                sender.sendMessage(Component.text("❌ Database connection failed: " + e.getMessage(), NamedTextColor.RED));
            }
            
        } catch (Exception e) {
            sender.sendMessage(Component.text("❌ Error during database check: " + e.getMessage(), NamedTextColor.RED));
            logger.severe("Database check error: " + e.getMessage());
        }
    }

    private void runSerializationTest(CommandSender sender) {
        sender.sendMessage(Component.text("=== Serialization Test ===", NamedTextColor.YELLOW));
        
        try {
            // Test basic serialization
            TestSaveableObject original = new TestSaveableObject("test_data", System.currentTimeMillis(), 42);
            String serialized = original.toString();
            sender.sendMessage(Component.text("Serialized: " + serialized, NamedTextColor.GRAY));
            
            // Test deserialization
            TestSaveableObject deserialized = new TestSaveableObject();
            deserialized.fromString(serialized);
            
            // Validate
            boolean isEqual = original.equals(deserialized);
            sender.sendMessage(Component.text("Deserialization successful: " + isEqual, 
                isEqual ? NamedTextColor.GREEN : NamedTextColor.RED));
            
            if (!isEqual) {
                sender.sendMessage(Component.text("❌ Serialization/Deserialization mismatch", NamedTextColor.RED));
                sender.sendMessage(Component.text("Original: " + original.getData() + "|" + original.getTimestamp() + "|" + original.getCounter(), NamedTextColor.RED));
                sender.sendMessage(Component.text("Deserialized: " + deserialized.getData() + "|" + deserialized.getTimestamp() + "|" + deserialized.getCounter(), NamedTextColor.RED));
            } else {
                sender.sendMessage(Component.text("✅ Serialization working correctly", NamedTextColor.GREEN));
            }
            
            // Test edge cases
            TestSaveableObject empty = new TestSaveableObject();
            empty.fromString("");
            sender.sendMessage(Component.text("Empty string handling: OK", NamedTextColor.GREEN));
            
            empty.fromString(null);
            sender.sendMessage(Component.text("Null string handling: OK", NamedTextColor.GREEN));
            
        } catch (Exception e) {
            sender.sendMessage(Component.text("❌ Serialization test failed: " + e.getMessage(), NamedTextColor.RED));
            logger.severe("Serialization test error: " + e.getMessage());
        }
    }

    private void runOperationsTest(CommandSender sender) {
        sender.sendMessage(Component.text("=== Operations Test ===", NamedTextColor.YELLOW));
        
        if (!saveService.isEnabled()) {
            sender.sendMessage(Component.text("❌ SaveService is disabled", NamedTextColor.RED));
            return;
        }
        
        String testKey = "diagnostic_test_" + System.currentTimeMillis();
        TestSaveableObject testObject = new TestSaveableObject("diagnostic_data", System.currentTimeMillis(), 123);
        
        try {
            // Test save operation
            SaveServiceReturnCode saveResult = saveService.save(testKey, testObject);
            sender.sendMessage(Component.text("Save operation: " + saveResult, 
                saveResult == SaveServiceReturnCode.SUCCESS ? NamedTextColor.GREEN : NamedTextColor.RED));
            
            if (saveResult != SaveServiceReturnCode.SUCCESS) {
                sender.sendMessage(Component.text("❌ Save operation failed", NamedTextColor.RED));
                return;
            }
            
            // Test exists operation
            CompletableFuture<SaveServiceReturnCode> existsFuture = saveService.exists(testKey);
            SaveServiceReturnCode existsResult = existsFuture.get(5, TimeUnit.SECONDS);
            sender.sendMessage(Component.text("Exists operation: " + existsResult, 
                existsResult == SaveServiceReturnCode.EXISTS ? NamedTextColor.GREEN : NamedTextColor.RED));
            
            // Test load operation
            TestSaveableObject loadedObject = new TestSaveableObject();
            CompletableFuture<SaveServiceReturnCode> loadFuture = saveService.load(testKey, loadedObject);
            SaveServiceReturnCode loadResult = loadFuture.get(5, TimeUnit.SECONDS);
            sender.sendMessage(Component.text("Load operation: " + loadResult, 
                loadResult == SaveServiceReturnCode.SUCCESS ? NamedTextColor.GREEN : NamedTextColor.RED));
            
            if (loadResult == SaveServiceReturnCode.SUCCESS) {
                boolean dataMatches = testObject.equals(loadedObject);
                sender.sendMessage(Component.text("Data integrity: " + dataMatches, 
                    dataMatches ? NamedTextColor.GREEN : NamedTextColor.RED));
            }
            
            // Test delete operation
            CompletableFuture<SaveServiceReturnCode> deleteFuture = saveService.delete(testKey);
            SaveServiceReturnCode deleteResult = deleteFuture.get(5, TimeUnit.SECONDS);
            sender.sendMessage(Component.text("Delete operation: " + deleteResult, 
                deleteResult == SaveServiceReturnCode.SUCCESS ? NamedTextColor.GREEN : NamedTextColor.RED));
            
            // Verify deletion
            CompletableFuture<SaveServiceReturnCode> verifyFuture = saveService.exists(testKey);
            SaveServiceReturnCode verifyResult = verifyFuture.get(5, TimeUnit.SECONDS);
            sender.sendMessage(Component.text("Delete verification: " + verifyResult, 
                verifyResult == SaveServiceReturnCode.NOT_EXISTS ? NamedTextColor.GREEN : NamedTextColor.RED));
            
            if (saveResult == SaveServiceReturnCode.SUCCESS && 
                existsResult == SaveServiceReturnCode.EXISTS &&
                loadResult == SaveServiceReturnCode.SUCCESS &&
                deleteResult == SaveServiceReturnCode.SUCCESS &&
                verifyResult == SaveServiceReturnCode.NOT_EXISTS) {
                sender.sendMessage(Component.text("✅ All operations completed successfully", NamedTextColor.GREEN));
            } else {
                sender.sendMessage(Component.text("❌ Some operations failed", NamedTextColor.RED));
            }
            
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            sender.sendMessage(Component.text("❌ Operations test failed: " + e.getMessage(), NamedTextColor.RED));
            logger.severe("Operations test error: " + e.getMessage());
        }
    }

    private void runCacheTest(CommandSender sender) {
        sender.sendMessage(Component.text("=== Cache Test ===", NamedTextColor.YELLOW));
        
        if (!saveService.isEnabled()) {
            sender.sendMessage(Component.text("❌ SaveService is disabled", NamedTextColor.RED));
            return;
        }
        
        try {
            // We'll use reflection to access cache stats since they're not exposed
            // For now, we'll do basic cache testing through the service interface
            
            String cacheTestKey = "cache_test_" + System.currentTimeMillis();
            TestSaveableObject testObject = new TestSaveableObject("cache_data", System.currentTimeMillis(), 456);
            
            // Save object to populate cache
            SaveServiceReturnCode saveResult = saveService.save(cacheTestKey, testObject);
            sender.sendMessage(Component.text("Cache population: " + saveResult, 
                saveResult == SaveServiceReturnCode.SUCCESS ? NamedTextColor.GREEN : NamedTextColor.RED));
            
            // Load immediately (should hit cache)
            TestSaveableObject loadedObject = new TestSaveableObject();
            long startTime = System.nanoTime();
            CompletableFuture<SaveServiceReturnCode> loadFuture = saveService.load(cacheTestKey, loadedObject);
            SaveServiceReturnCode loadResult = loadFuture.get(5, TimeUnit.SECONDS);
            long endTime = System.nanoTime();
            
            sender.sendMessage(Component.text("Cache hit load: " + loadResult, 
                loadResult == SaveServiceReturnCode.SUCCESS ? NamedTextColor.GREEN : NamedTextColor.RED));
            sender.sendMessage(Component.text("Load time: " + ((endTime - startTime) / 1000000) + "ms", NamedTextColor.GRAY));
            
            if (loadResult == SaveServiceReturnCode.SUCCESS) {
                boolean dataMatches = testObject.equals(loadedObject);
                sender.sendMessage(Component.text("Cache data integrity: " + dataMatches, 
                    dataMatches ? NamedTextColor.GREEN : NamedTextColor.RED));
            }
            
            // Clean up
            saveService.delete(cacheTestKey).get(5, TimeUnit.SECONDS);
            
            sender.sendMessage(Component.text("✅ Cache test completed", NamedTextColor.GREEN));
            
        } catch (Exception e) {
            sender.sendMessage(Component.text("❌ Cache test failed: " + e.getMessage(), NamedTextColor.RED));
            logger.severe("Cache test error: " + e.getMessage());
        }
    }

    private void runStressTest(CommandSender sender, int count) {
        sender.sendMessage(Component.text("=== Stress Test (n=" + count + ") ===", NamedTextColor.YELLOW));
        
        if (!saveService.isEnabled()) {
            sender.sendMessage(Component.text("❌ SaveService is disabled", NamedTextColor.RED));
            return;
        }
        
        int successCount = 0;
        int failureCount = 0;
        long totalTime = 0;
        
        try {
            long overallStart = System.currentTimeMillis();
            
            for (int i = 0; i < count; i++) {
                String key = "stress_test_" + i + "_" + System.currentTimeMillis();
                TestSaveableObject obj = new TestSaveableObject("stress_data_" + i, System.currentTimeMillis(), i);
                
                try {
                    long start = System.nanoTime();
                    
                    // Save
                    SaveServiceReturnCode saveResult = saveService.save(key, obj);
                    if (saveResult != SaveServiceReturnCode.SUCCESS) {
                        failureCount++;
                        continue;
                    }
                    
                    // Load
                    TestSaveableObject loaded = new TestSaveableObject();
                    CompletableFuture<SaveServiceReturnCode> loadFuture = saveService.load(key, loaded);
                    SaveServiceReturnCode loadResult = loadFuture.get(1, TimeUnit.SECONDS);
                    if (loadResult != SaveServiceReturnCode.SUCCESS || !obj.equals(loaded)) {
                        failureCount++;
                        continue;
                    }
                    
                    // Delete
                    CompletableFuture<SaveServiceReturnCode> deleteFuture = saveService.delete(key);
                    SaveServiceReturnCode deleteResult = deleteFuture.get(1, TimeUnit.SECONDS);
                    if (deleteResult != SaveServiceReturnCode.SUCCESS) {
                        failureCount++;
                        continue;
                    }
                    
                    long end = System.nanoTime();
                    totalTime += (end - start);
                    successCount++;
                    
                } catch (Exception e) {
                    failureCount++;
                    if (i < 5) { // Only log first few errors to avoid spam
                        logger.warning("Stress test iteration " + i + " failed: " + e.getMessage());
                    }
                }
                
                // Progress update every 10% for larger tests
                if (count >= 100 && (i + 1) % (count / 10) == 0) {
                    sender.sendMessage(Component.text("Progress: " + (i + 1) + "/" + count, NamedTextColor.GRAY));
                }
            }
            
            long overallEnd = System.currentTimeMillis();
            
            double successRate = (double) successCount / count * 100;
            double avgTime = successCount > 0 ? (double) totalTime / successCount / 1000000 : 0; // Convert to ms
            
            sender.sendMessage(Component.text("Results:", NamedTextColor.YELLOW));
            sender.sendMessage(Component.text("Success: " + successCount + "/" + count + " (" + String.format("%.1f", successRate) + "%)", 
                successRate > 90 ? NamedTextColor.GREEN : successRate > 50 ? NamedTextColor.YELLOW : NamedTextColor.RED));
            sender.sendMessage(Component.text("Failures: " + failureCount, 
                failureCount == 0 ? NamedTextColor.GREEN : NamedTextColor.RED));
            sender.sendMessage(Component.text("Average operation time: " + String.format("%.2f", avgTime) + "ms", NamedTextColor.GRAY));
            sender.sendMessage(Component.text("Total test time: " + (overallEnd - overallStart) + "ms", NamedTextColor.GRAY));
            
            if (successRate == 0) {
                sender.sendMessage(Component.text("❌ CRITICAL: 0% success rate indicates fundamental service failure", NamedTextColor.RED));
            } else if (successRate < 50) {
                sender.sendMessage(Component.text("❌ WARNING: Low success rate indicates service issues", NamedTextColor.RED));
            } else if (successRate < 90) {
                sender.sendMessage(Component.text("⚠ NOTICE: Moderate success rate, investigate further", NamedTextColor.YELLOW));
            } else {
                sender.sendMessage(Component.text("✅ Good success rate", NamedTextColor.GREEN));
            }
            
        } catch (Exception e) {
            sender.sendMessage(Component.text("❌ Stress test failed: " + e.getMessage(), NamedTextColor.RED));
            logger.severe("Stress test error: " + e.getMessage());
        }
    }

    private void runFullDiagnostic(CommandSender sender) {
        sender.sendMessage(Component.text("=== Full SaveService Diagnostic ===", NamedTextColor.YELLOW));
        
        runStatusCheck(sender);
        sender.sendMessage(Component.text("", NamedTextColor.WHITE));
        
        runConfigCheck(sender);
        sender.sendMessage(Component.text("", NamedTextColor.WHITE));
        
        runDatabaseCheck(sender);
        sender.sendMessage(Component.text("", NamedTextColor.WHITE));
        
        runSerializationTest(sender);
        sender.sendMessage(Component.text("", NamedTextColor.WHITE));
        
        runOperationsTest(sender);
        sender.sendMessage(Component.text("", NamedTextColor.WHITE));
        
        runCacheTest(sender);
        sender.sendMessage(Component.text("", NamedTextColor.WHITE));
        
        runStressTest(sender, 50); // Smaller stress test for full diagnostic
        
        sender.sendMessage(Component.text("=== Diagnostic Complete ===", NamedTextColor.YELLOW));
    }

    private int parseCount(String countStr) {
        try {
            int count = Integer.parseInt(countStr);
            return Math.max(1, Math.min(10000, count)); // Limit between 1 and 10000
        } catch (NumberFormatException e) {
            return 100; // Default
        }
    }
}