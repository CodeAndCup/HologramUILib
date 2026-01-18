# HologramUILib Fabric API Reference

This section documents the **public API of HologramUILib for Fabric mod developers**. This API is intended for use in other Fabric mods only. If you are developing a Spigot plugin, refer to the [Spigot Integration Guide](../02-spigot-integration/README.md).

---

## Overview

The `HologramMenuAPI` provides a fluent, high-level interface for creating and managing interactive holographic menus in your Fabric mods.

### Main Entry Point

```java
import fr.perrier.hologramuilib.api.HologramMenuAPI;
import fr.perrier.hologramuilib.client.menu.HologramMenu;
import net.minecraft.util.math.Vec3d;
```

---

## Quick Start

### Minimal Example

```java
import fr.perrier.hologramuilib.api.HologramMenuAPI;
import net.minecraft.util.math.Vec3d;

// Create a simple menu
HologramMenuAPI.builder("my_shop")
    .at(new Vec3d(100, 65, 200))
    .withTitle("§6My Shop")
    .addButton("buy_diamond", "Buy Diamonds", player -> {
        player.sendMessage("Thank you for your purchase!");
    })
    .show();
```

### Complete Example

```java
HologramMenuAPI.builder("advanced_menu")
    .at(playerPos)
    .withTitle("§b§lMain Menu")
    .addText("welcome", "Welcome " + player.getName().getString())
    .addSeparator()
    .addButton("shop", "Open Shop", p -> openShop(p))
    .addButton("inventory", "Inventory", p -> openInventory(p))
    .addButton("settings", "Settings", p -> openSettings(p))
    .addSeparator()
    .addSlider("volume", 0, 100, 50, value -> {
        player.sendMessage("Volume: " + value);
    })
    .show();
```

---

## API Reference

### HologramMenuAPI

#### Static Methods

```java
// Create a new menu
MenuBuilder builder(String menuId)

// Get an existing menu
HologramMenu getMenu(String menuId)

// Close a menu
void closeMenu(String menuId)

// Check if a menu exists
boolean hasMenu(String menuId)

// Close all menus
void closeAllMenus()
```

### MenuBuilder (Fluent API)

#### Menu Configuration

```java
MenuBuilder at(Vec3d position)
MenuBuilder withTitle(String title)
MenuBuilder withWidth(int width)
MenuBuilder withHeight(int height) // -1 = auto
MenuBuilder withScale(float scale)
MenuBuilder withBackgroundColor(String color)
MenuBuilder withBackground(boolean enabled)
MenuBuilder withCondition(Predicate<ServerPlayer> condition)
MenuBuilder withMaxDistance(double distance)
void show()
```

#### Adding Elements

```java
MenuBuilder addText(String id, String content)
MenuBuilder addButton(String id, String label, Consumer<ServerPlayer> onClick)
MenuBuilder addButton(String id, String label, String color, Consumer<ServerPlayer> onClick)
MenuBuilder addSeparator()
MenuBuilder addImageURL(String id, String url, int width, int height)
MenuBuilder addImage(String id, String resourcePath, int width, int height)
MenuBuilder addItem(String id, ItemStack itemStack)
MenuBuilder addProgressBar(String id, int value, int max)
MenuBuilder addSlider(String id, int min, int max, int value, Consumer<Integer> onValueChange)
MenuBuilder addContainer(String id, List<MenuElement> elements)
MenuBuilder addSpacing(int height)
```

---

## Supported Element Types

- **Button**: Clickable, supports left/right/middle click and hover
- **Text**: Supports Minecraft color codes and formatting
- **Slider**: Drag & drop, min/max, visual feedback
- **ProgressBar**: Visual progress indicator
- **Image**: URL or local resource
- **Item**: Minecraft item display
- **Separator**: Visual divider

---

## Animations

You can animate menu elements using the built-in animation API:

```java
menu.getElement("welcome")
    .animate()
    .fadeIn(1.0) // 1 second
    .scale(1.0f, 1.2f)
    .play();
```

Supported properties: scale, translation, rotation, opacity, color, duration, delay, easing, onComplete.

---

## Menu Management

```java
// Get a menu
HologramMenu menu = HologramMenuAPI.getMenu("menu_id");

// Check existence
if (HologramMenuAPI.hasMenu("shop_menu")) { ... }

// Close a menu
HologramMenuAPI.closeMenu("menu_id");

// Close all menus
HologramMenuAPI.closeAllMenus();

// Update menu after changes
menu.setScale(0.025f);
menu.update();
```

---

## Best Practices

- Always check if a menu exists before modifying it
- Use unique menu IDs per player/session
- Call `menu.update()` after making changes
- Clean up menus on mod disable/unload

---

## Troubleshooting

- **Menu not showing?**
  - Ensure the mod is installed and enabled
  - Check the menu position (Vec3d)
  - Make sure the player is in the correct dimension
  - Enable debug mode for visualization

- **Interactions not working?**
  - Check that callbacks are set
  - Check server logs

- **Performance issues?**
  - Limit the number of active menus
  - Use `withMaxDistance()`
  - Reduce element complexity

---

**Need help?** See the [code examples](./examples/) or the [mod overview](../01-presentation/).

**Last updated:** January 2026
