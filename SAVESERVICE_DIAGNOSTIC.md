# SaveService Diagnostic Tool

This diagnostic tool helps identify issues with the SaveService persistence layer that may be causing 0% success rates in stress tests.

## Root Cause Analysis

The diagnostic tool has identified that **SaveService is disabled by default** in the configuration (`save-service.enabled: false`). This is likely the primary cause of 0% success rates in stress tests.

## Usage

Use the following command to run diagnostics:

```
/saveservice-diagnostic <subcommand>
```

### Available Subcommands

#### `status`
Checks if SaveService is enabled and operational.
- **Purpose**: Verify basic service state
- **Key Check**: Service enablement status

#### `config`
Validates all SaveService configuration values.
- **Purpose**: Ensure configuration is valid and reasonable
- **Checks**: 
  - Enabled status
  - Save interval (must be > 0)
  - Cache duration (must be > 0) 
  - Cache size (must be > 0)

#### `database`
Tests database connectivity and structure.
- **Purpose**: Verify SQLite database is accessible and properly configured
- **Checks**:
  - Database file existence and size
  - Connection establishment
  - Table structure validation
  - Record count

#### `serialization`
Tests SaveableObject implementation.
- **Purpose**: Verify object serialization/deserialization works correctly
- **Checks**:
  - Basic serialization round-trip
  - Edge case handling (null, empty strings)
  - Data integrity validation

#### `operations`
Tests all CRUD operations individually.
- **Purpose**: Verify each SaveService operation works independently  
- **Checks**:
  - Save operation success
  - Exists check accuracy
  - Load operation and data integrity
  - Delete operation and verification

#### `cache`
Tests cache functionality and performance.
- **Purpose**: Verify cache behavior and performance
- **Checks**:
  - Cache population
  - Cache hit performance
  - Data integrity through cache

#### `stress [count]`
Runs stress test with specified number of operations (default 100).
- **Purpose**: Identify failure patterns under load
- **Checks**:
  - Success/failure rates
  - Average operation performance
  - Error pattern analysis
- **Usage**: `/saveservice-diagnostic stress 500`

#### `full`
Runs complete diagnostic suite.
- **Purpose**: Comprehensive system health check
- **Includes**: All above tests in sequence

## Expected Findings

Based on the current configuration:

1. **Status Check**: Will show service is DISABLED ❌
2. **Config Check**: Will show `enabled: false` ❌
3. **Other Tests**: Will fail due to service being offline

## Recommended Actions

1. **Enable SaveService**: Set `save-service.enabled: true` in config.yml
2. **Verify Configuration**: Ensure reasonable values for intervals and cache settings
3. **Test After Enable**: Run `/saveservice-diagnostic full` after enabling
4. **Monitor Performance**: Use stress tests to validate under load

## Troubleshooting Guide

### 0% Success Rate Issues
- **Primary Cause**: Service disabled in configuration
- **Secondary Causes**: Database connectivity, invalid configuration values
- **Solution**: Enable service and validate configuration

### Database Issues
- **Symptoms**: SQL_ERROR return codes, connection failures
- **Causes**: File permissions, disk space, SQLite driver issues
- **Solution**: Check file permissions and disk space

### Performance Issues  
- **Symptoms**: High operation times, timeouts
- **Causes**: Large cache size, frequent disk flushes, database locks
- **Solution**: Tune cache settings and save intervals

### Configuration Issues
- **Symptoms**: Service fails to start, invalid parameter errors
- **Causes**: Negative values, missing configuration entries
- **Solution**: Validate all configuration values are positive

## Permissions

Requires `bingocraft.admin` permission to use diagnostic commands.