# HologramUILib Workspace

[![Build Status](https://img.shields.io/badge/Build-Passing-brightgreen)]()
[![Java Version](https://img.shields.io/badge/Java-21+-brightgreen)]()
[![Gradle](https://img.shields.io/badge/Gradle-8.5+-brightgreen)]()
[![License: CC BY-NC-ND](https://img.shields.io/badge/License-Creative_Common-yellow.svg)](LICENSE.txt)

This repository is a **multi-project Gradle workspace** for developing and building both the **Fabric HologramUILib client mod** and the **Spigot-HologramUILib server API**.

## Project Structure

- `Fabric-HologramUILib/` : Minecraft client mod (Fabric)
- `Spigot-HologramUILib/` : Spigot server API and plugin
- `documentation/` : Centralized documentation (GitBook format)

## Documentation

Full technical and user documentation is available in the [`documentation/`](./documentation/README.md) folder, organized into three main sections:

- [Mod Overview](./documentation/01-presentation/README.md) — General features and concepts
- [Spigot Integration Guide](./documentation/02-spigot-integration/README.md) — **For Spigot plugin developers** (server-side API)
- [Fabric Mod API Reference](./documentation/03-api/README.md) — **For Fabric mod developers** (client-side API)

> [!IMPORTANT]
> - If you are developing a **Spigot plugin**, use the [Spigot Integration Guide](./documentation/02-spigot-integration/).
> - If you are developing a **Fabric mod**, use the [Fabric Mod API Reference](./documentation/03-api/).

For any questions, please refer to the [documentation](./documentation/README.md) or open an issue on GitHub.

## Quick Start

### 1. Installation

```bash
# Clone the workspace
git clone <repository>
cd HologramUILib-Workspace

# Check the setup
./gradlew projectInfo
```

### 2. Building Both Projects

```bash
# Full build (recommended)
./gradlew build

# Or build individually
./gradlew :Fabric-HologramUILib:build
./gradlew :Spigot-HologramUILib:build
```

### 3. Build Artifacts

Compiled JARs are available in:
- `Fabric-HologramUILib/build/libs/HologramUILib-1.0-SNAPSHOT.jar`
- `Spigot-HologramUILib/build/libs/spigot-hologramuilib-1.0-SNAPSHOT.jar`

## Notes

- All technical and user documentation is centralized in the `documentation/` folder.
- Please read the documentation and development guides before contributing.

---

**Version**: 1.0  
**Last updated**: January 2026
