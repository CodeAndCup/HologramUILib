#!/bin/bash
# Build and Package Script for Spigot-HologramUILib

echo "======================================"
echo "Spigot-HologramUILib Build Script"
echo "======================================"

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Check Java
echo -e "${YELLOW}Checking Java version...${NC}"
java -version 2>&1 | head -1
if ! command -v java &> /dev/null; then
    echo -e "${RED}Java not found! Please install Java 17+${NC}"
    exit 1
fi

# Build with Gradle
echo -e "${YELLOW}Building with Gradle...${NC}"
./gradlew clean build

if [ $? -eq 0 ]; then
    echo -e "${GREEN}✓ Build successful!${NC}"
    echo -e "${GREEN}JAR location: build/libs/spigot-hologramuilib-1.0.0.jar${NC}"
else
    echo -e "${RED}✗ Build failed!${NC}"
    exit 1
fi

# Copy to local server (optional)
if [ -d "../minecraft-server/plugins" ]; then
    echo -e "${YELLOW}Copying to local server...${NC}"
    cp build/libs/spigot-hologramuilib-1.0.0.jar ../minecraft-server/plugins/
    echo -e "${GREEN}✓ Copied to server!${NC}"
fi

echo -e "${GREEN}======================================"
echo "Build Complete!"
echo "======================================"
