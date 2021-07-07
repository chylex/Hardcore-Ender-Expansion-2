package chylex.hee.mixin;

import chylex.hee.game.item.ItemEnergyOracle;
import net.minecraft.client.gui.screen.inventory.InventoryScreen;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InventoryScreen.class)
public abstract class HookInventoryScreenEntity {
	@Inject(method = "drawEntityOnScreen", at = @At("HEAD"))
	private static void onStartedRenderingEntity(final int posX, final int posY, final int scale, final float mouseX, final float mouseY, final LivingEntity livingEntity, final CallbackInfo ci) {
		ItemEnergyOracle.Color.isRenderingInventoryEntity = true;
	}
	
	@Inject(method = "drawEntityOnScreen", at = @At("RETURN"))
	private static void onFinishedRenderingEntity(final int posX, final int posY, final int scale, final float mouseX, final float mouseY, final LivingEntity livingEntity, final CallbackInfo ci) {
		ItemEnergyOracle.Color.isRenderingInventoryEntity = false;
	}
}
