package fr.perrier.hologramuilib.mixin.client;

import fr.perrier.hologramuilib.client.interaction.MenuInteractionTracker;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Mixin to intercept player interactions and suppress them when interacting with menus.
 * Only intercepts critical actions: breaking blocks and attacking entities.
 */
@Mixin(ClientPlayerInteractionManager.class)
public class ClientPlayerInteractionManagerMixin {

    private static final Logger LOGGER = LoggerFactory.getLogger("HologramUILib/InteractionMixin");

    /**
     * Intercepts block breaking attempts.
     * Cancels if the player is interacting with a menu.
     */
    @Inject(method = "attackBlock", at = @At("HEAD"), cancellable = true)
    private void hologramuilib$onAttackBlock(BlockPos pos, Direction direction, CallbackInfoReturnable<Boolean> cir) {
        MenuInteractionTracker tracker = MenuInteractionTracker.getInstance();
        if (tracker.shouldSuppressInteraction(MenuInteractionTracker.InteractionType.BREAK_BLOCK)) {
            LOGGER.info("🛡️ BLOCKED block breaking at {} - player is using menu", pos);
            cir.setReturnValue(false);
        }
    }

    /**
     * Intercepts entity attacks.
     * Cancels if the player is interacting with a menu.
     */
    @Inject(method = "attackEntity", at = @At("HEAD"), cancellable = true)
    private void hologramuilib$onAttackEntity(PlayerEntity player, Entity target, CallbackInfo ci) {
        MenuInteractionTracker tracker = MenuInteractionTracker.getInstance();
        if (tracker.shouldSuppressInteraction(MenuInteractionTracker.InteractionType.ATTACK_ENTITY)) {
            LOGGER.info("🛡️ BLOCKED entity attack on {} - player is using menu", target.getName().getString());
            ci.cancel();
        }
    }
}
