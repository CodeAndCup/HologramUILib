# HologramUILib - Mod Overview

HologramUILib is a client-side library for Minecraft 1.21.4 that allows you to create floating, interactive holographic menus in the 3D world, fully configurable with an API. It is a powerful tool to enhance user experience on multiplayer servers.

## Main Features

### UI Elements

The mod supports **8 element types** for building rich interfaces:

| Element      | Description                                 |
|--------------|---------------------------------------------|
| **Button**   | Clickable button with visual feedback        |
| **Text**     | Text with Minecraft color code support       |
| **Image**    | Display local or URL images                  |
| **Item**     | Display Minecraft items                      |
| **Separator**| Visual separator                             |
| **ProgressBar** | Animated progress bar                    |
| **Slider**   | Interactive slider for value selection       |
| **Container**| Grouping container for elements              |

### Interaction System

- **Accurate 3D raycasting**: Detects which menu the player is aiming at
- **Hover effects**: Visual feedback on hover
- **Multi-button support**: Left, right, and middle click
- **Drag & Drop**: Full slider support
- **Anti-spam cooldown**: 300ms between interactions
- **Interaction sounds**: Automatic audio feedback

### Advanced Animation System

- **30+ easing functions**: Quad, Cubic, Quart, Quint, Sine, Expo, Circ, Back, Elastic, Bounce
- **Animatable properties**:
  - Scale
  - Translation
  - Rotation
  - Opacity
  - Color
- **Presets**: FadeIn, Bounce, Rotate360, Slide, etc.
- **Timeline**: Sequence multiple animations
- **Callbacks**: `onComplete` for chaining animations

### Network Protocol

Client-server synchronization via custom packets:

- **MenuData**: Send menu data to the client
- **MenuClose**: Close menu
- **MenuClick**: Notify server of interactions

This enables bidirectional communication between clients and the Spigot server.

### Advanced Menu Manager

- **Template cache**: Fast reuse of menu configurations
- **Display conditions**: Min/max distance, custom predicates
- **Lifecycle management**: Create, update, destroy
- **Statistics**: Track created/destroyed/active menus
- **Smart filtering**: Menus visible only under certain conditions

### Flexible Configuration

- **JSON format**: Fully configurable menus
- **Automatic validation**: Clear error messages
- **Automatic layout**: Vertical layout with padding/spacing

### Performance & Optimizations

- **Billboard rotation**: Menus always face the player
- **Distance-based rendering**: Only render menus within range
- **Client-only**: No server modifications required
- **Debug mode**: Visualize hitboxes, raycast, and orientation

## Architecture

### Data Flow

```
Spigot Plugin
    ↓
MenuData Packet (Server → Client)
    ↓
HologramMenuAPI.builder()
    ↓
HologramMenu (3D Rendering)
    ↓
Raycasting + Interaction
    ↓
MenuClick Packet (Client → Server)
    ↓
Spigot Plugin (Event Handler)
```

## Installation

### Requirements

- **Minecraft**: 1.21.4
- **Fabric Loader**: 0.16.9+
- **Fabric API**: 0.110.5+

### Installation Steps

1. **Download the mod**
   - `HologramUILib-1.0-SNAPSHOT.jar`
2. **Place in your mods folder**
   - `.minecraft/mods/HologramUILib-1.0-SNAPSHOT.jar`
3. **Launch Minecraft with Fabric**
   - The mod will load automatically at startup


## Debug Mode

Enable debug mode to visualize:

- **Hitboxes**: Clickable zones for each element (blue)
- **Raycast**: Player's aiming direction (red)
- **Orientation**: Menu rotation (yellow arrows)
- **HUD Info**: Display of active menus

Useful for:
- Debugging positions
- Testing interaction zones
- Optimizing element placement

## Use Cases

HologramUILib is ideal for:

- **Shop menus**: Display items for purchase
- **Quest interfaces**: Show objectives
- **Information panels**: Server info, rules, etc.
- **Voting**: Let players vote on options
- **Configuration**: Let users adjust settings

---

**Next step**: See the [Spigot Integration Guide](../02-spigot-integration/) to integrate this mod with your plugins.
