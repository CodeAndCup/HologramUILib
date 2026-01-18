package fr.perrier.hologramuilib.client.menu;

import fr.perrier.hologramuilib.api.HologramMenuAPI;
import fr.perrier.hologramuilib.client.animation.Animation;
import fr.perrier.hologramuilib.client.animation.AnimationManager;
import fr.perrier.hologramuilib.client.animation.AnimationProperties;
import fr.perrier.hologramuilib.client.animation.Easing;
import fr.perrier.hologramuilib.client.menu.elements.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Contains all test and showcase menus for HologramUILib.
 * Separated from MainClient to keep the main initialization clean.
 */
public class TestMenus {

    private static final Logger LOGGER = LoggerFactory.getLogger("HologramUILib-TestMenus");

    /**
     * Creates a main navigation menu with buttons to open other menus.
     */
    public static void createMainMenu(MinecraftClient client) {
        if (client.player == null) return;

        LOGGER.info("Creating main navigation menu...");

        // Create menu at player's position + 3 blocks in front
        Vec3d menuPos = client.player.getEyePos()
                .add(client.player.getRotationVec(1.0F).multiply(3.0))
                .add(0, 0.5, 0);

        String menuId = "main_menu_" + System.currentTimeMillis();
        MenuManager manager = MenuManager.getInstance();
        HologramMenu menu = manager.createMenu(menuId, menuPos);

        menu.setYaw(client.player.getHeadYaw());
        menu.setWidth(220);
        menu.setHeight(-1);
        menu.setScale(0.01f);
        menu.setMaxRenderDistance(20.0);
        menu.setBackgroundColor(0xDD000000);
        menu.setBorderColor(0xFFFFD700);
        menu.setPadding(10);
        menu.setSpacing(5);

        float elementWidth = menu.getWidth() - menu.getPadding() * 2;

        // Header
        TextElement title = new TextElement("title", "§6§l✨ HologramUILib Menu ✨");
        title.setCentered(true);
        title.setWidth(elementWidth);
        menu.addElement(title);

        TextElement subtitle = new TextElement("subtitle", "§7Select a showcase menu");
        subtitle.setCentered(true);
        subtitle.setWidth(elementWidth);
        menu.addElement(subtitle);

        SeparatorElement sep1 = new SeparatorElement("sep1");
        sep1.setWidth(elementWidth);
        sep1.setColor(0xFFFFD700);
        menu.addElement(sep1);

        // Navigation buttons
        ButtonElement btnTest = new ButtonElement("btn_test", "§a📋 All Elements", "open_test_menu");
        btnTest.setWidth(elementWidth);
        btnTest.setTextColor(0xFF00FF00);
        btnTest.setHoverColor(0x4000FF00);
        menu.addElement(btnTest);

        ButtonElement btnAnimation = new ButtonElement("btn_animation", "§b✨ Animations", "open_animation_menu");
        btnAnimation.setWidth(elementWidth);
        btnAnimation.setTextColor(0xFF00FFFF);
        btnAnimation.setHoverColor(0x4000FFFF);
        menu.addElement(btnAnimation);

        ButtonElement btnSlider = new ButtonElement("btn_slider", "§d🎚 Sliders & Controls", "open_slider_menu");
        btnSlider.setWidth(elementWidth);
        btnSlider.setTextColor(0xFFFF00FF);
        btnSlider.setHoverColor(0x40FF00FF);
        menu.addElement(btnSlider);

        SeparatorElement sepPhase9 = new SeparatorElement("sep_phase9");
        sepPhase9.setWidth(elementWidth);
        sepPhase9.setColor(0x88FFFF00);
        menu.addElement(sepPhase9);

        TextElement phase9Title = new TextElement("phase9_title", "§e§lPhase 9 & 10: New API & Features");
        phase9Title.setCentered(true);
        phase9Title.setWidth(elementWidth);
        menu.addElement(phase9Title);

        ButtonElement btnAPIShowcase = new ButtonElement("btn_api_showcase", "§d✨ API Showcase", "open_api_showcase");
        btnAPIShowcase.setWidth(elementWidth);
        btnAPIShowcase.setTextColor(0xFFFF00FF);
        btnAPIShowcase.setHoverColor(0x40FF00FF);
        menu.addElement(btnAPIShowcase);

        ButtonElement btnShop = new ButtonElement("btn_shop", "§6🛒 Shop Demo", "open_shop_demo");
        btnShop.setWidth(elementWidth);
        btnShop.setTextColor(0xFFFFAA00);
        btnShop.setHoverColor(0x40FFAA00);
        menu.addElement(btnShop);

        ButtonElement btnSettings = new ButtonElement("btn_settings", "§b⚙ Settings Demo", "open_settings_demo");
        btnSettings.setWidth(elementWidth);
        btnSettings.setTextColor(0xFF00AAFF);
        btnSettings.setHoverColor(0x4000AAFF);
        menu.addElement(btnSettings);

        ButtonElement btnStats = new ButtonElement("btn_stats", "§e★ Stats Demo", "open_stats_demo");
        btnStats.setWidth(elementWidth);
        btnStats.setTextColor(0xFFFFFF00);
        btnStats.setHoverColor(0x40FFFF00);
        menu.addElement(btnStats);

        ButtonElement btnActivity = new ButtonElement("btn_activity", "§b👁 Activity Monitor", "open_activity_monitor");
        btnActivity.setWidth(elementWidth);
        btnActivity.setTextColor(0xFF00FFFF);
        btnActivity.setHoverColor(0x4000FFFF);
        menu.addElement(btnActivity);

        ButtonElement btnWeb = new ButtonElement("btn_web", "§a🌐 Web Resources", "open_web_resources");
        btnWeb.setWidth(elementWidth);
        btnWeb.setTextColor(0xFF00FF00);
        btnWeb.setHoverColor(0x4000FF00);
        menu.addElement(btnWeb);

        SeparatorElement sep2 = new SeparatorElement("sep2");
        sep2.setWidth(elementWidth);
        sep2.setColor(0x88FFFFFF);
        menu.addElement(sep2);

        ButtonElement btnClose = new ButtonElement("btn_close", "§c✖ Close", "close_menu");
        btnClose.setWidth(elementWidth);
        btnClose.setTextColor(0xFFFF0000);
        btnClose.setHoverColor(0x40FF0000);
        menu.addElement(btnClose);

        menu.recalculateAutoHeight();

        client.player.sendMessage(Text.literal("§6✨ Main menu created!"), false);
    }

    /**
     * Creates an animation showcase menu demonstrating Phase 5 features.
     */
    public static void createAnimationMenu(MinecraftClient client) {
        if (client.player == null) return;

        LOGGER.info("Creating animation showcase menu...");

        Vec3d menuPos = client.player.getEyePos()
                .add(client.player.getRotationVec(1.0F).multiply(3.0))
                .add(0, 0.5, 0);

        String menuId = "animation_menu_" + System.currentTimeMillis();
        MenuManager manager = MenuManager.getInstance();
        AnimationManager animManager = manager.getAnimationManager();
        HologramMenu menu = manager.createMenu(menuId, menuPos);

        menu.setYaw(client.player.getHeadYaw());
        menu.setWidth(240);
        menu.setHeight(-1);
        menu.setScale(0.01f);
        menu.setMaxRenderDistance(20.0);
        menu.setBackgroundColor(0xDD001020);
        menu.setBorderColor(0xFF00FFFF);
        menu.setPadding(10);
        menu.setSpacing(5);

        float elementWidth = menu.getWidth() - menu.getPadding() * 2;

        // Header
        TextElement title = new TextElement("title", "§b§l✨ Animation Showcase ✨");
        title.setCentered(true);
        title.setWidth(elementWidth);
        menu.addElement(title);

        TextElement subtitle = new TextElement("subtitle", "§7Phase 5: Animation System");
        subtitle.setCentered(true);
        subtitle.setWidth(elementWidth);
        menu.addElement(subtitle);

        SeparatorElement sep1 = new SeparatorElement("sep1");
        sep1.setWidth(elementWidth);
        sep1.setColor(0xFF00FFFF);
        menu.addElement(sep1);

        // Hover animations info
        TextElement hoverInfo = new TextElement("hover_info", "§e§lHover Effects");
        hoverInfo.setWidth(elementWidth);
        menu.addElement(hoverInfo);

        TextElement hoverDesc = new TextElement("hover_desc", "§7Hover these buttons to see animations:");
        hoverDesc.setWidth(elementWidth);
        menu.addElement(hoverDesc);

        // Buttons with hover animations (already built-in)
        ButtonElement btnHover1 = new ButtonElement("btn_hover_1", "§aHover Scale Effect");
        btnHover1.setWidth(elementWidth);
        btnHover1.setTextColor(0xFF00FF00);
        btnHover1.setHoverColor(0x4000FF00);
        menu.addElement(btnHover1);

        ButtonElement btnHover2 = new ButtonElement("btn_hover_2", "§bSmooth Hover Animation");
        btnHover2.setWidth(elementWidth);
        btnHover2.setTextColor(0xFF00FFFF);
        btnHover2.setHoverColor(0x4000FFFF);
        menu.addElement(btnHover2);

        SeparatorElement sep2 = new SeparatorElement("sep2");
        sep2.setWidth(elementWidth);
        sep2.setColor(0x8800FFFF);
        menu.addElement(sep2);

        // Click animations
        TextElement clickInfo = new TextElement("click_info", "§e§lClick Effects");
        clickInfo.setWidth(elementWidth);
        menu.addElement(clickInfo);

        ButtonElement btnBounce = new ButtonElement("btn_bounce", "§d🎪 Bounce on Click");
        btnBounce.setWidth(elementWidth);
        btnBounce.setTextColor(0xFFFF00FF);
        btnBounce.setHoverColor(0x40FF00FF);
        btnBounce.onClick(() -> {
            // Bounce animation
            Animation bounce = Animation.builder(AnimationProperties.SCALE)
                .from(1.0f).to(0.9f)
                .duration(100)
                .easing(Easing::easeInOutQuad)
                .onComplete(() -> {
                    Animation bounceBack = Animation.builder(AnimationProperties.SCALE)
                        .from(0.9f).to(1.0f)
                        .duration(200)
                        .easing(Easing::easeOutBounce)
                        .buildAndStart();
                    animManager.addAnimation(btnBounce.getId(), bounceBack);
                })
                .buildAndStart();
            animManager.addAnimation(btnBounce.getId(), bounce);
        });
        menu.addElement(btnBounce);

        ButtonElement btnElastic = new ButtonElement("btn_elastic", "§e🌀 Elastic Effect");
        btnElastic.setWidth(elementWidth);
        btnElastic.setTextColor(0xFFFFFF00);
        btnElastic.setHoverColor(0x40FFFF00);
        btnElastic.onClick(() -> {
            Animation elastic = Animation.builder(AnimationProperties.SCALE)
                .from(1.0f).to(1.3f)
                .duration(600)
                .easing(Easing::easeOutElastic)
                .buildAndStart();
            animManager.addAnimation(btnElastic.getId(), elastic);
        });
        menu.addElement(btnElastic);

        ButtonElement btnBack = new ButtonElement("btn_back", "§c🎯 Back Overshoot");
        btnBack.setWidth(elementWidth);
        btnBack.setTextColor(0xFFFF5555);
        btnBack.setHoverColor(0x40FF5555);
        btnBack.onClick(() -> {
            Animation back = Animation.builder(AnimationProperties.SCALE)
                .from(1.0f).to(1.2f)
                .duration(400)
                .easing(Easing::easeOutBack)
                .buildAndStart();
            animManager.addAnimation(btnBack.getId(), back);
        });
        menu.addElement(btnBack);

        SeparatorElement sep3 = new SeparatorElement("sep3");
        sep3.setWidth(elementWidth);
        sep3.setColor(0x8800FFFF);
        menu.addElement(sep3);

        // Animated progress bar
        TextElement progressInfo = new TextElement("progress_info", "§e§lAnimated Progress");
        progressInfo.setWidth(elementWidth);
        menu.addElement(progressInfo);

        ProgressBarElement animProgress = new ProgressBarElement("anim_progress");
        animProgress.setWidth(elementWidth);
        animProgress.setProgress(0);
        animProgress.setLabel("Loading");
        animProgress.setForegroundColor(0xFF00FFFF);
        animProgress.setShowPercentage(true);
        menu.addElement(animProgress);

        // Button to animate progress
        ButtonElement btnAnimateProgress = new ButtonElement("btn_animate_progress", "§a▶ Animate Progress");
        btnAnimateProgress.setWidth(elementWidth);
        btnAnimateProgress.setTextColor(0xFF00FF00);
        btnAnimateProgress.setHoverColor(0x4000FF00);
        btnAnimateProgress.onClick(() -> {
            animProgress.setProgress(0);
            Animation progressAnim = Animation.builder(AnimationProperties.PROGRESS)
                .from(0).to(1)
                .duration(2000)
                .easing(Easing::easeInOutQuad)
                .onComplete(() -> animProgress.setLabel("Complete!"))
                .buildAndStart();
            animManager.addAnimation(animProgress.getId(), progressAnim);
        });
        menu.addElement(btnAnimateProgress);

        SeparatorElement sep4 = new SeparatorElement("sep4");
        sep4.setWidth(elementWidth);
        sep4.setColor(0x8800FFFF);
        menu.addElement(sep4);

        // Navigation buttons
        ButtonElement btnBack2 = new ButtonElement("btn_back_menu", "§7← Back to Main", "open_main_menu");
        btnBack2.setWidth(elementWidth);
        btnBack2.setTextColor(0xFF888888);
        btnBack2.setHoverColor(0x40888888);
        menu.addElement(btnBack2);

        ButtonElement btnClose = new ButtonElement("btn_close", "§c✖ Close", "close_menu");
        btnClose.setWidth(elementWidth);
        btnClose.setTextColor(0xFFFF0000);
        btnClose.setHoverColor(0x40FF0000);
        menu.addElement(btnClose);

        // Staggered entrance animation for elements
        for (int i = 0; i < menu.getElements().size(); i++) {
            MenuElement element = menu.getElements().get(i);
            Animation fadeIn = Animation.builder(AnimationProperties.OPACITY)
                .from(0).to(1)
                .duration(300)
                .delay(i * 50L)
                .easing(Easing::easeOutQuad)
                .buildAndStart();
            animManager.addAnimation(element.getId(), fadeIn);
        }

        menu.recalculateAutoHeight();

        client.player.sendMessage(Text.literal("§b✨ Animation menu created!"), false);
    }

    /**
     * Creates a slider showcase menu demonstrating interactive controls.
     */
    public static void createSliderMenu(MinecraftClient client) {
        if (client.player == null) return;

        LOGGER.info("Creating slider showcase menu...");

        Vec3d menuPos = client.player.getEyePos()
                .add(client.player.getRotationVec(1.0F).multiply(3.0))
                .add(0, 0.5, 0);

        String menuId = "slider_menu_" + System.currentTimeMillis();
        MenuManager manager = MenuManager.getInstance();
        HologramMenu menu = manager.createMenu(menuId, menuPos);

        menu.setYaw(client.player.getHeadYaw());
        menu.setWidth(260);
        menu.setHeight(-1);
        menu.setScale(0.01f);
        menu.setMaxRenderDistance(20.0);
        menu.setBackgroundColor(0xDD200010);
        menu.setBorderColor(0xFFFF00FF);
        menu.setPadding(10);
        menu.setSpacing(6);

        float elementWidth = menu.getWidth() - menu.getPadding() * 2;

        // Header
        TextElement title = new TextElement("title", "§d§l🎚 Slider Showcase 🎚");
        title.setCentered(true);
        title.setWidth(elementWidth);
        menu.addElement(title);

        TextElement subtitle = new TextElement("subtitle", "§7Phase 5: Interactive Controls");
        subtitle.setCentered(true);
        subtitle.setWidth(elementWidth);
        menu.addElement(subtitle);

        SeparatorElement sep1 = new SeparatorElement("sep1");
        sep1.setWidth(elementWidth);
        sep1.setColor(0xFFFF00FF);
        menu.addElement(sep1);

        TextElement info = new TextElement("info", "§7Click and drag the sliders:");
        info.setWidth(elementWidth);
        menu.addElement(info);

        // Volume slider
        SliderElement volumeSlider = new SliderElement("volume_slider");
        volumeSlider.setLabel("Volume");
        volumeSlider.setRange(0, 100);
        volumeSlider.setValue(0.75f);
        volumeSlider.setUnit("%");
        volumeSlider.setDecimals(0);
        volumeSlider.setWidth(elementWidth);
        volumeSlider.setFillColor(0xFF00FF00);
        volumeSlider.onValueChange(value -> {
            if (client.player != null) {
                client.player.sendMessage(Text.literal("§aVolume: " + value.intValue() + "%"), true);
            }
        });
        menu.addElement(volumeSlider);

        // Brightness slider
        SliderElement brightnessSlider = new SliderElement("brightness_slider");
        brightnessSlider.setLabel("Brightness");
        brightnessSlider.setRange(0, 1);
        brightnessSlider.setValue(0.5f);
        brightnessSlider.setDecimals(2);
        brightnessSlider.setWidth(elementWidth);
        brightnessSlider.setFillColor(0xFFFFFF00);
        brightnessSlider.onValueChange(value -> {
            if (client.player != null) {
                client.player.sendMessage(Text.literal("§eBrightness: " + String.format("%.2f", value)), true);
            }
        });
        menu.addElement(brightnessSlider);

        // Distance slider
        SliderElement distanceSlider = new SliderElement("distance_slider");
        distanceSlider.setLabel("Distance");
        distanceSlider.setRange(0, 100);
        distanceSlider.setValue(0.3f);
        distanceSlider.setUnit("m");
        distanceSlider.setDecimals(1);
        distanceSlider.setWidth(elementWidth);
        distanceSlider.setFillColor(0xFF00FFFF);
        distanceSlider.onValueChange(value -> {
            if (client.player != null) {
                client.player.sendMessage(Text.literal("§bDistance: " + String.format("%.1f", value) + "m"), true);
            }
        });
        menu.addElement(distanceSlider);

        // Speed slider
        SliderElement speedSlider = new SliderElement("speed_slider");
        speedSlider.setLabel("Speed");
        speedSlider.setRange(0, 200);
        speedSlider.setValue(0.5f);
        speedSlider.setUnit("%");
        speedSlider.setDecimals(0);
        speedSlider.setWidth(elementWidth);
        speedSlider.setFillColor(0xFFFF00FF);
        speedSlider.onValueChange(value -> {
            if (client.player != null) {
                client.player.sendMessage(Text.literal("§dSpeed: " + value.intValue() + "%"), true);
            }
        });
        menu.addElement(speedSlider);

        SeparatorElement sep2 = new SeparatorElement("sep2");
        sep2.setWidth(elementWidth);
        sep2.setColor(0x88FF00FF);
        menu.addElement(sep2);

        // Combined progress bars showing slider values
        TextElement progressTitle = new TextElement("progress_title", "§e§lLinked Progress Bars");
        progressTitle.setWidth(elementWidth);
        menu.addElement(progressTitle);

        ProgressBarElement linkedProgress1 = new ProgressBarElement("linked_1");
        linkedProgress1.setWidth(elementWidth);
        linkedProgress1.setProgress(0.75f);
        linkedProgress1.setLabel("Volume Mirror");
        linkedProgress1.setForegroundColor(0xFF00FF00);
        linkedProgress1.setShowPercentage(true);
        menu.addElement(linkedProgress1);

        ProgressBarElement linkedProgress2 = new ProgressBarElement("linked_2");
        linkedProgress2.setWidth(elementWidth);
        linkedProgress2.setProgress(0.5f);
        linkedProgress2.setLabel("Speed Mirror");
        linkedProgress2.setForegroundColor(0xFFFF00FF);
        linkedProgress2.setShowPercentage(true);
        menu.addElement(linkedProgress2);

        // Link sliders to progress bars
        volumeSlider.onValueChange(value -> {
            linkedProgress1.setProgress(value / 100f);
        });

        speedSlider.onValueChange(value -> {
            linkedProgress2.setProgress(value / 200f);
        });

        SeparatorElement sep3 = new SeparatorElement("sep3");
        sep3.setWidth(elementWidth);
        sep3.setColor(0x88FF00FF);
        menu.addElement(sep3);

        // Navigation buttons
        ButtonElement btnBack = new ButtonElement("btn_back_menu", "§7← Back to Main", "open_main_menu");
        btnBack.setWidth(elementWidth);
        btnBack.setTextColor(0xFF888888);
        btnBack.setHoverColor(0x40888888);
        menu.addElement(btnBack);

        ButtonElement btnClose = new ButtonElement("btn_close", "§c✖ Close", "close_menu");
        btnClose.setWidth(elementWidth);
        btnClose.setTextColor(0xFFFF0000);
        btnClose.setHoverColor(0x40FF0000);
        menu.addElement(btnClose);

        menu.recalculateAutoHeight();

        client.player.sendMessage(Text.literal("§d🎚 Slider menu created!"), false);
    }

    /**
     * Creates a comprehensive showcase menu displaying all available element types.
     */
    public static void createSimpleTestMenu(MinecraftClient client) {
        if (client.player == null) return;

        LOGGER.info("Creating comprehensive showcase hologram menu...");

        // Create menu at player's position + 3 blocks in front
        Vec3d menuPos = client.player.getEyePos()
                .add(client.player.getRotationVec(1.0F).multiply(3.0))
                .add(0, 0.5, 0);

        // Generate unique ID
        String menuId = "showcase_" + System.currentTimeMillis();

        // Create menu using manager
        MenuManager manager = MenuManager.getInstance();
        HologramMenu menu = manager.createMenu(menuId, menuPos);

        // Set yaw directly
        menu.setYaw(client.player.getHeadYaw());

        // Configure the menu
        menu.setWidth(220);
        menu.setHeight(-1); // Auto height
        menu.setScale(0.01f);
        menu.setMaxRenderDistance(20.0);
        menu.setBackgroundColor(0xDD000000); // Semi-transparent black
        menu.setBorderColor(0xFF00FFFF); // Cyan border
        menu.setPadding(10);
        menu.setSpacing(5);

        // Calculate element width
        float elementWidth = menu.getWidth() - menu.getPadding() * 2;

        // ===== HEADER SECTION =====
        TextElement title = new TextElement("title", "§b§l⚡ HologramUILib Showcase ⚡");
        title.setCentered(true);
        title.setWidth(elementWidth);
        menu.addElement(title);

        TextElement subtitle = new TextElement("subtitle", "§7All Available Elements");
        subtitle.setCentered(true);
        subtitle.setWidth(elementWidth);
        menu.addElement(subtitle);

        SeparatorElement headerSep = new SeparatorElement("header_sep");
        headerSep.setWidth(elementWidth);
        headerSep.setColor(0xFF00FFFF);
        menu.addElement(headerSep);

        // ===== TEXT ELEMENTS SECTION =====
        TextElement textTitle = new TextElement("text_title", "§e§l1. Text Elements");
        textTitle.setWidth(elementWidth);
        menu.addElement(textTitle);

        TextElement textLeft = new TextElement("text_left", "§7Left-aligned text");
        textLeft.setWidth(elementWidth);
        menu.addElement(textLeft);

        TextElement textCenter = new TextElement("text_center", "§7Centered text");
        textCenter.setCentered(true);
        textCenter.setWidth(elementWidth);
        menu.addElement(textCenter);

        TextElement textColored = new TextElement("text_colored", "§aGreen §bCyan §cRed §dMagenta §eYellow");
        textColored.setCentered(true);
        textColored.setWidth(elementWidth);
        menu.addElement(textColored);

        SeparatorElement sep1 = new SeparatorElement("sep1");
        sep1.setWidth(elementWidth);
        sep1.setColor(0x8800FFFF);
        menu.addElement(sep1);

        // ===== BUTTON ELEMENTS SECTION =====
        TextElement buttonTitle = new TextElement("button_title", "§e§l2. Button Elements");
        buttonTitle.setWidth(elementWidth);
        menu.addElement(buttonTitle);

        // Button with action registry
        ButtonElement btnRegistry = new ButtonElement("btn_registry", "§a✓ Button with Action", "test_action");
        btnRegistry.setWidth(elementWidth);
        btnRegistry.setTextColor(0xFF00FF00);
        btnRegistry.setHoverColor(0x4000FF00);
        menu.addElement(btnRegistry);

        // Button with callback demo (using action registry)
        ButtonElement btnCallback = new ButtonElement("btn_callback", "§b⚡ Button with Callback", "callback_demo");
        btnCallback.setWidth(elementWidth);
        btnCallback.setTextColor(0xFF00FFFF);
        btnCallback.setHoverColor(0x4000FFFF);
        menu.addElement(btnCallback);

        // Toggle button (using action registry)
        ButtonElement btnToggle = new ButtonElement("btn_toggle", "§cOFF", "toggle_action");
        btnToggle.setWidth(elementWidth);
        btnToggle.setTextColor(0xFFFF0000);
        btnToggle.setHoverColor(0x40FF0000);
        menu.addElement(btnToggle);

        SeparatorElement sep2 = new SeparatorElement("sep2");
        sep2.setWidth(elementWidth);
        sep2.setColor(0x8800FFFF);
        menu.addElement(sep2);

        // ===== SEPARATOR ELEMENTS SECTION =====
        TextElement sepTitle = new TextElement("sep_title", "§e§l3. Separator Elements");
        sepTitle.setWidth(elementWidth);
        menu.addElement(sepTitle);

        SeparatorElement sepThin = new SeparatorElement("sep_thin");
        sepThin.setWidth(elementWidth);
        sepThin.setColor(0xFFFFFFFF);
        menu.addElement(sepThin);

        TextElement sepLabel1 = new TextElement("sep_label1", "§7Colored separators:");
        sepLabel1.setWidth(elementWidth);
        menu.addElement(sepLabel1);

        SeparatorElement sepRed = new SeparatorElement("sep_red");
        sepRed.setWidth(elementWidth);
        sepRed.setColor(0xFFFF0000);
        menu.addElement(sepRed);

        SeparatorElement sepGreen = new SeparatorElement("sep_green");
        sepGreen.setWidth(elementWidth);
        sepGreen.setColor(0xFF00FF00);
        menu.addElement(sepGreen);

        SeparatorElement sepBlue = new SeparatorElement("sep_blue");
        sepBlue.setWidth(elementWidth);
        sepBlue.setColor(0xFF0000FF);
        menu.addElement(sepBlue);

        SeparatorElement sep3 = new SeparatorElement("sep3");
        sep3.setWidth(elementWidth);
        sep3.setColor(0x8800FFFF);
        menu.addElement(sep3);

        // ===== PROGRESS BAR SECTION =====
        TextElement progressTitle = new TextElement("progress_title", "§e§l4. Progress Bar Elements");
        progressTitle.setWidth(elementWidth);
        menu.addElement(progressTitle);

        // Progress bar examples
        ProgressBarElement progress1 = new ProgressBarElement("progress_25");
        progress1.setWidth(elementWidth);
        progress1.setProgress(0.25f);
        progress1.setLabel("HP");
        progress1.setForegroundColor(0xFFFF0000);
        progress1.setShowPercentage(true);
        menu.addElement(progress1);

        ProgressBarElement progress2 = new ProgressBarElement("progress_50");
        progress2.setWidth(elementWidth);
        progress2.setProgress(0.50f);
        progress2.setLabel("Mana");
        progress2.setForegroundColor(0xFF0000FF);
        progress2.setShowPercentage(true);
        menu.addElement(progress2);

        ProgressBarElement progress3 = new ProgressBarElement("progress_75");
        progress3.setWidth(elementWidth);
        progress3.setProgress(0.75f);
        progress3.setLabel("XP");
        progress3.setForegroundColor(0xFF00FF00);
        progress3.setShowPercentage(true);
        menu.addElement(progress3);

        ProgressBarElement progress4 = new ProgressBarElement("progress_100");
        progress4.setWidth(elementWidth);
        progress4.setProgress(1.0f);
        progress4.setLabel("Complete");
        progress4.setForegroundColor(0xFFFFD700);
        progress4.setShowPercentage(true);
        menu.addElement(progress4);

        SeparatorElement sep4 = new SeparatorElement("sep4");
        sep4.setWidth(elementWidth);
        sep4.setColor(0x8800FFFF);
        menu.addElement(sep4);

        // ===== ITEM ELEMENTS SECTION =====
        TextElement itemTitle = new TextElement("item_title", "§e§l5. Item Elements");
        itemTitle.setWidth(elementWidth);
        menu.addElement(itemTitle);

        TextElement itemNote = new TextElement("item_note", "§7(Placeholder - API in progress)");
        itemNote.setCentered(true);
        itemNote.setWidth(elementWidth);
        menu.addElement(itemNote);

        ItemElement itemDiamond = new ItemElement("item_diamond", net.minecraft.item.Items.DIAMOND);
        itemDiamond.setWidth(elementWidth);
        menu.addElement(itemDiamond);

        SeparatorElement sep5 = new SeparatorElement("sep5");
        sep5.setWidth(elementWidth);
        sep5.setColor(0x8800FFFF);
        menu.addElement(sep5);

        // ===== NAVIGATION SECTION =====
        TextElement navTitle = new TextElement("nav_title", "§e§l6. Navigation");
        navTitle.setWidth(elementWidth);
        menu.addElement(navTitle);

        ButtonElement btnBackMain = new ButtonElement("btn_back_main", "§7← Back to Main Menu", "open_main_menu");
        btnBackMain.setWidth(elementWidth);
        btnBackMain.setTextColor(0xFF888888);
        btnBackMain.setHoverColor(0x40888888);
        menu.addElement(btnBackMain);

        ButtonElement btnClose = new ButtonElement("btn_close", "§c✖ Close", "close_menu");
        btnClose.setWidth(elementWidth);
        btnClose.setTextColor(0xFFFF0000);
        btnClose.setHoverColor(0x40FF0000);
        menu.addElement(btnClose);

        SeparatorElement sep6 = new SeparatorElement("sep6");
        sep6.setWidth(elementWidth);
        sep6.setColor(0x8800FFFF);
        menu.addElement(sep6);

        // ===== FOOTER SECTION =====
        TextElement footer1 = new TextElement("footer1", "§7Total: 7 Element Types Available");
        footer1.setCentered(true);
        footer1.setWidth(elementWidth);
        menu.addElement(footer1);

        TextElement footer2 = new TextElement("footer2", "§8HologramUILib v1.0");
        footer2.setCentered(true);
        footer2.setWidth(elementWidth);
        menu.addElement(footer2);

        // Recalculate height
        menu.recalculateAutoHeight();

        LOGGER.info("=== Showcase Menu Created ===");
        LOGGER.info("Total elements: {}", menu.getElements().size());
        LOGGER.info("Menu dimensions: {}x{}", menu.getWidth(), menu.getHeight());
        LOGGER.info("Position: {}", menuPos);
        LOGGER.info("============================");

        client.player.sendMessage(Text.literal("§b⚡ Showcase menu created! §7(" + menu.getElements().size() + " elements)"), false);
    }

    // ========================================
    // PHASE 9 & 10 - NEW API EXAMPLES
    // ========================================

    /**
     * Creates a simple shop menu using the new HologramMenuAPI (Phase 9).
     */
    public static void createAPIShopExample(MinecraftClient client) {
        if (client.player == null) return;

        LOGGER.info("Creating API Shop Example...");

        // Position: 3 blocks in front + at eye level
        Vec3d menuPos = client.player.getEyePos()
                .add(client.player.getRotationVec(1.0F).multiply(3.0));

        HologramMenuAPI.builder("api_shop_menu")
            .at(menuPos)
            .withTitle("§6§l⚔ Item Shop ⚔")
            .width(220)
            .addSeparator()
            .addText("§7Welcome! What would you like to buy?")
            .addSpacing(5)
            .addButton("sword", "§a💎 Diamond Sword - §e100$", btn -> {
                client.player.sendMessage(Text.literal("§a✔ You bought a Diamond Sword!"), false);
            })
            .addButton("armor", "§9🛡 Diamond Armor - §e250$", btn -> {
                client.player.sendMessage(Text.literal("§9✔ You bought Diamond Armor!"), false);
            })
            .addButton("potion", "§d🧪 Health Potion - §e50$", btn -> {
                client.player.sendMessage(Text.literal("§d✔ You bought a Health Potion!"), false);
            })
            .addButton("food", "§6🍖 Golden Apple - §e75$", btn -> {
                client.player.sendMessage(Text.literal("§6✔ You bought a Golden Apple!"), false);
            })
            .addSeparator()
            .addCenteredText("§7Your Balance: §e1000$")
            .addSpacing(5)
            .addButton("close", "§c✖ Close Shop", btn -> {
                fr.perrier.hologramuilib.api.HologramMenuAPI.closeMenu("api_shop_menu");
            })
            .show();

        client.player.sendMessage(Text.literal("§6✨ Shop opened using new API!"), false);
    }

    /**
     * Creates a settings menu with sliders using the new API (Phase 9).
     */
    public static void createAPISettingsExample(MinecraftClient client) {
        if (client.player == null) return;

        LOGGER.info("Creating API Settings Example...");

        // Position: 3 blocks in front + at eye level
        Vec3d menuPos = client.player.getEyePos()
                .add(client.player.getRotationVec(1.0F).multiply(3.0));

        HologramMenuAPI.builder("api_settings_menu")
            .at(menuPos)
            .withTitle("§b§l⚙ Settings ⚙")
            .width(240)
            .addSeparator()
            .addText("§7Audio Settings:")
            .addSlider("master_volume", 0, 100, 75, value -> {
                client.player.sendMessage(Text.literal("§aMaster Volume: " + Math.round(value) + "%"), true);
                LOGGER.info("Master volume changed to: {}", value);
            })
            .addSlider("music_volume", 0, 100, 50, value -> {
                client.player.sendMessage(Text.literal("§dMusic Volume: " + Math.round(value) + "%"), true);
                LOGGER.info("Music volume changed to: {}", value);
            })
            .addSpacing(10)
            .addText("§7Graphics Quality:")
            .addProgressBar("quality", 0.8f)
            .addText("§780% - Good")
            .addSeparator()
            .addButton("save", "§a§l💾 Save Settings", btn -> {
                client.player.sendMessage(Text.literal("§a✔ Settings saved successfully!"), false);
                fr.perrier.hologramuilib.api.HologramMenuAPI.closeMenu("api_settings_menu");
            })
            .addButton("cancel", "§c✖ Cancel", btn -> {
                fr.perrier.hologramuilib.api.HologramMenuAPI.closeMenu("api_settings_menu");
            })
            .show();

        client.player.sendMessage(Text.literal("§b⚙ Settings menu opened!"), false);
    }

    /**
     * Creates a player stats menu with dynamic data (Phase 9 API).
     */
    public static void createAPIStatsExample(MinecraftClient client) {
        if (client.player == null) return;

        LOGGER.info("Creating API Stats Example...");

        String playerName = client.player.getName().getString();
        int health = (int) client.player.getHealth();
        int maxHealth = (int) client.player.getMaxHealth();
        float healthPercent = health / (float) maxHealth;
        int foodLevel = client.player.getHungerManager().getFoodLevel();
        float foodPercent = foodLevel / 20f;

        // Position: 3 blocks in front + at eye level
        Vec3d menuPos = client.player.getEyePos()
                .add(client.player.getRotationVec(1.0F).multiply(3.0));

        fr.perrier.hologramuilib.api.HologramMenuAPI.builder("api_stats_menu")
            .at(menuPos)
            .withTitle("§e§l★ Player Stats ★")
            .width(240)
            .addSeparator()
            .addText("§7Player: §f" + playerName)
            .addSpacing(5)
            .addText("§c❤ Health:")
            .addProgressBar("health", healthPercent)
            .addText("§7" + health + " / " + maxHealth + " HP")
            .addSpacing(5)
            .addText("§6🍖 Hunger:")
            .addProgressBar("hunger", foodPercent)
            .addText("§7" + foodLevel + " / 20")
            .addSpacing(5)
            .addText("§bLevel: §a" + client.player.experienceLevel)
            .addProgressBar("xp", client.player.experienceProgress)
            .addSeparator()
            .addCenteredText("§7Position: §f" +
                (int)client.player.getX() + ", " +
                (int)client.player.getY() + ", " +
                (int)client.player.getZ())
            .addButton("close", "§c✖ Close", btn -> {
                fr.perrier.hologramuilib.api.HologramMenuAPI.closeMenu("api_stats_menu");
            })
            .show();

        client.player.sendMessage(Text.literal("§e★ Stats menu opened!"), false);
    }

    /**
     * Creates an activity monitor menu showing what nearby players are doing (Phase 10.1).
     */
    public static void createActivityMonitorExample(MinecraftClient client) {
        if (client.player == null || client.world == null) return;

        LOGGER.info("Creating Activity Monitor Example...");

        Vec3d menuPos = client.player.getEyePos()
                .add(client.player.getRotationVec(1.0F).multiply(3.0))
                .add(0, 0.5, 0);

        String menuId = "activity_monitor_" + System.currentTimeMillis();
        MenuManager manager = MenuManager.getInstance();
        HologramMenu menu = manager.createMenu(menuId, menuPos);

        menu.setYaw(client.player.getHeadYaw());
        menu.setWidth(250);
        menu.setScale(0.01f);
        menu.setBackgroundColor(0xDD000000);
        menu.setBorderColor(0xFF00FFFF);
        menu.setPadding(10);

        float elementWidth = menu.getWidth() - menu.getPadding() * 2;

        // Title
        TextElement title = new TextElement("title", "§b§l👁 Activity Monitor 👁");
        title.setCentered(true);
        title.setWidth(elementWidth);
        menu.addElement(title);

        SeparatorElement sep1 = new SeparatorElement("sep1");
        sep1.setWidth(elementWidth);
        sep1.setColor(0xFF00FFFF);
        menu.addElement(sep1);

        // Placeholder for activities (will be updated by auto-update)
        TextElement activitiesPlaceholder = new TextElement("activities", "§7Loading activities...");
        activitiesPlaceholder.setWidth(elementWidth);
        menu.addElement(activitiesPlaceholder);

        SeparatorElement sep2 = new SeparatorElement("sep2");
        sep2.setWidth(elementWidth);
        sep2.setColor(0xFF00FFFF);
        menu.addElement(sep2);

        TextElement info = new TextElement("info", "§7Updates every 500ms");
        info.setCentered(true);
        info.setWidth(elementWidth);
        info.setTextColor(0xFF888888);
        menu.addElement(info);

        ButtonElement btnClose = new ButtonElement("btn_close", "§c✖ Close", "close_menu");
        btnClose.setWidth(elementWidth);
        btnClose.setTextColor(0xFFFF0000);
        btnClose.setHoverColor(0x40FF0000);
        menu.addElement(btnClose);

        menu.recalculateAutoHeight();

        // Setup auto-update to refresh activities every 500ms
        menu.setAutoUpdate(500, () -> {
            // Get current activities
            var tracker = fr.perrier.hologramuilib.client.activity.ActivityTracker.getInstance();
            var activities = tracker.getAllActivities();

            // Build activity text
            StringBuilder activityText = new StringBuilder();
            if (activities.isEmpty()) {
                activityText.append("§7No other players nearby");
            } else {
                int count = 0;
                for (var activity : activities) {
                    if (count >= 5) break;

                    String playerName = activity.getPlayer().getName().getString();
                    String activityIcon = activity.getType().getIcon();
                    String activityName = activity.getType().getDisplayName();
                    String duration = activity.getFormattedDuration();

                    if (count > 0) activityText.append("\n");
                    activityText.append(String.format("§f%s §8- %s %s §7(%s)",
                        playerName, activityIcon, activityName, duration));

                    count++;
                }
            }

            // Update the placeholder element
            TextElement placeholder = (TextElement) menu.getElementById("activities");
            if (placeholder != null) {
                placeholder.setContent(activityText.toString());
            }
        });

        client.player.sendMessage(Text.literal("§b👁 Activity Monitor opened!"), false);
    }

    /**
     * Creates a demonstration of the web resource loading system (Phase 10.2).
     */
    public static void createWebResourceExample(MinecraftClient client) {
        if (client.player == null) return;

        LOGGER.info("Creating Web Resource Example...");

        Vec3d menuPos = client.player.getEyePos()
                .add(client.player.getRotationVec(1.0F).multiply(3.0))
                .add(0, 0.5, 0);

        String menuId = "web_resource_" + System.currentTimeMillis();
        MenuManager manager = MenuManager.getInstance();
        HologramMenu menu = manager.createMenu(menuId, menuPos);

        menu.setYaw(client.player.getHeadYaw());
        menu.setWidth(280);
        menu.setHeight(-1);
        menu.setScale(0.01f);
        menu.setMaxRenderDistance(20.0);
        menu.setBackgroundColor(0xDD000000);
        menu.setBorderColor(0xFF00FF00);
        menu.setPadding(10);
        menu.setSpacing(5);

        float elementWidth = menu.getWidth() - menu.getPadding() * 2;

        // Title
        TextElement title = new TextElement("title", "§a§l🌐 Web Image Loading 🌐");
        title.setCentered(true);
        title.setWidth(elementWidth);
        menu.addElement(title);

        SeparatorElement sep1 = new SeparatorElement("sep1");
        sep1.setWidth(elementWidth);
        sep1.setColor(0xFF00FF00);
        menu.addElement(sep1);

        // Description
        TextElement desc = new TextElement("desc", "§7Loading image from URL...");
        desc.setCentered(true);
        desc.setWidth(elementWidth);
        menu.addElement(desc);

        // Add the actual image element
        ImageURLElement imageElement = new ImageURLElement("demo_image", "https://media4.giphy.com/media/v1.Y2lkPTc5MGI3NjExNDB3dGpyc3AwbWgzbWZhNGJ0bDR0cWoydXBnY2tqMXlyZHhua3c3eiZlcD12MV9pbnRlcm5hbF9naWZfYnlfaWQmY3Q9Zw/XabhIre57HUM8/giphy.gif");
        imageElement.setSize(200, 133); // Maintain 3:2 aspect ratio
        //imageElement.setMaintainAspectRatio(true);
        menu.addElement(imageElement);

        SeparatorElement sep2 = new SeparatorElement("sep2");
        sep2.setWidth(elementWidth);
        sep2.setColor(0xFF00FF00);
        menu.addElement(sep2);

        // Image info
        TextElement imageInfo = new TextElement("image_info", "§7URL: https://saominecraft.com/assets/SAOMC_icon.png");
        imageInfo.setCentered(true);
        imageInfo.setWidth(elementWidth);
        imageInfo.setTextColor(0xFF888888);
        menu.addElement(imageInfo);

        // Features list
        TextElement featuresTitle = new TextElement("features_title", "§e§lFeatures:");
        featuresTitle.setWidth(elementWidth);
        menu.addElement(featuresTitle);

        TextElement feature1 = new TextElement("feature1", "§a✔ §7Async loading");
        feature1.setWidth(elementWidth);
        menu.addElement(feature1);

        TextElement feature2 = new TextElement("feature2", "§a✔ §7HTTPS security");
        feature2.setWidth(elementWidth);
        menu.addElement(feature2);

        TextElement feature3 = new TextElement("feature3", "§a✔ §7Local cache (1h)");
        feature3.setWidth(elementWidth);
        menu.addElement(feature3);

        TextElement feature4 = new TextElement("feature4", "§a✔ §7Size limits (5MB)");
        feature4.setWidth(elementWidth);
        menu.addElement(feature4);

        TextElement feature5 = new TextElement("feature5", "§a✔ §7Timeout (10s)");
        feature5.setWidth(elementWidth);
        menu.addElement(feature5);

        SeparatorElement sep3 = new SeparatorElement("sep3");
        sep3.setWidth(elementWidth);
        sep3.setColor(0xFF00FF00);
        menu.addElement(sep3);

        // Use cases
        TextElement usage = new TextElement("usage", "§7Use Cases:");
        usage.setWidth(elementWidth);
        menu.addElement(usage);

        TextElement use1 = new TextElement("use1", "§8• §7Player avatars");
        use1.setWidth(elementWidth);
        menu.addElement(use1);

        TextElement use2 = new TextElement("use2", "§8• §7Server stats");
        use2.setWidth(elementWidth);
        menu.addElement(use2);

        TextElement use3 = new TextElement("use3", "§8• §7Dynamic content");
        use3.setWidth(elementWidth);
        menu.addElement(use3);

        SeparatorElement sep4 = new SeparatorElement("sep4");
        sep4.setWidth(elementWidth);
        sep4.setColor(0xFF00FF00);
        menu.addElement(sep4);

        ButtonElement btnClose = new ButtonElement("btn_close", "§c✖ Close", "close_menu");
        btnClose.setWidth(elementWidth);
        btnClose.setTextColor(0xFFFF0000);
        btnClose.setHoverColor(0x40FF0000);
        menu.addElement(btnClose);

        menu.recalculateAutoHeight();

        client.player.sendMessage(Text.literal("§a🌐 Web Resource demo with image!"), false);
    }

    /**
     * Creates a comprehensive API showcase menu with all Phase 9 features.
     */
    public static void createAPIShowcaseMenu(MinecraftClient client) {
        if (client.player == null) return;

        LOGGER.info("Creating API Showcase Menu...");

        // Position: 3 blocks in front + at eye level
        Vec3d menuPos = client.player.getEyePos()
                .add(client.player.getRotationVec(1.0F).multiply(3.0));

        HologramMenuAPI.builder("api_showcase")
            .at(menuPos)
            .withTitle("§d§l✨ API Showcase ✨")
            .width(260)
            .addSeparator()
            .addCenteredText("§7Phase 9: Developer API")
            .addText("§8Builder pattern for easy menu creation")
            .addSpacing(5)
            .addText("§6Features:")
            .addText("§e• §7Fluent API (chainable methods)")
            .addText("§e• §7Event system")
            .addText("§e• §7Flexible positioning")
            .addText("§e• §7Built-in callbacks")
            .addSpacing(5)
            .addSeparator()
            .addCenteredText("§7Try these examples:")
            .addSpacing(5)
            .addButton("shop", "§6🛒 Shop Menu", btn -> createAPIShopExample(client))
            .addButton("settings", "§b⚙ Settings Menu", btn -> createAPISettingsExample(client))
            .addButton("stats", "§e★ Stats Menu", btn -> createAPIStatsExample(client))
            .addButton("activity", "§b👁 Activity Monitor", btn -> createActivityMonitorExample(client))
            .addButton("web", "§a🌐 Web Resources", btn -> createWebResourceExample(client))
            .addSeparator()
            .addCenteredText("§8All menus created with 3-5 lines of code!")
            .addSpacing(5)
            .addButton("close", "§c✖ Close", btn -> {
                HologramMenuAPI.closeMenu("api_showcase");
            })
            .show();

        client.player.sendMessage(Text.literal("§d✨ API Showcase opened!"), false);
    }
}

