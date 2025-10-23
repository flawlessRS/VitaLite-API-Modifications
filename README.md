# VitaLite API Modifications

This repository contains ONLY modifications to the Tonic/VitaLite API.

**DO NOT commit plugins, personal configurations, or full project files.**

## Allowed Files
- `src/main/java/com/tonic/**/*.java` - Core Tonic API modifications
- `api/src/main/java/com/tonic/**/*.java` - Extended Tonic API modifications

## Recent Changes

### JVM Memory Configuration (Latest)
- Added `--Xms` and `--Xmx` CLI arguments to VitaLiteOptions
- Implemented automatic GC optimization in JVMLauncher:
  - G1GC for heaps >1GB
  - SerialGC for heaps ≤1GB
  - Auto-calculates initial heap size
  - Default: 768MB SerialGC

## Usage
```bash
# Use custom memory settings
java -jar VitaLite.jar --Xmx2g --login user:pass
```
