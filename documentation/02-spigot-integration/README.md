# Spigot Integration Guide

This guide explains how to use the **Spigot API** of HologramUILib to display interactive holographic menus to your players from a Spigot plugin. This API is completely independent from the Fabric mod API and is designed for use on the server side only.

## Prerequisites

- Minecraft server 1.21.4 or compatible
- Java 21 or higher
- The latest `Spigot-HologramUILib` plugin JAR in your server's `plugins/` folder
- Players must have the [Fabric HologramUILib client mod](https://github.com/perrier1034/HologramUILib) installed to see menus

## Installation

1. **Download the Spigot plugin**
   - Build or download `Spigot-HologramUILib-1.0-SNAPSHOT.jar` from the [releases](https://github.com/perrier1034/HologramUILib/releases) or your local build.
   - Place it in your server's `plugins/` directory.

2. **Restart your server**
   - The plugin will register its messaging channel and be ready to communicate with Fabric clients.

## Basic Usage (Spigot API)

You can use the Spigot API in your plugin to create and show a menu. **Do not use Fabric API classes in your Spigot plugin.**

### Example: Showing a Menu to a Player

```java
import fr.perrier.hologramuilib.api.MenuBuilder;
import org.bukkit.entity.Player;

// Example: Show a simple menu to a player
public void showSimpleMenu(Player player) {
    new MenuBuilder("simple_menu")
        .withTitle("Welcome!")
        .addText("desc", "This is a holographic menu.")
        .addButton("ok", "OK", (p, event) -> p.sendMessage("You clicked OK!"))
        .forPlayers(player)
        .show();
}
```

### Menu Features (Spigot API)
- Text, button, slider, progress bar, and image elements
- Customizable size, position, colors, and more
- Automatic height calculation (set height to -1)
- Callbacks for button clicks and slider changes

## Troubleshooting

- **Menus not showing?**
  - Make sure the client has the Fabric mod installed and enabled.
  - Check that the plugin messaging channel is registered (see server logs).
  - Ensure you are using compatible versions of the mod and plugin.

- **Elements missing or cut off?**
  - Use `.withHeight(-1)` for automatic height, or set a sufficient height manually.
  - Always add elements before showing the menu.

- **Menu appears in the ground?**
  - The client mod automatically adjusts menu position to avoid clipping into blocks.

## Further Reading

- [Mod API Reference](../03-api/README.md) (for Fabric mod developers)
- [Mod Overview](../01-presentation/README.md)

---

**Last updated:** January 2026
