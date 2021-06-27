package chylex.hee.mixin;

import chylex.hee.client.render.TerritoryRenderer;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientWorld.class)
public abstract class HookSkyColor {
	@Inject(method = "getSkyColor", at = @At("HEAD"), cancellable = true)
	private void replaceSkyColor(final BlockPos pos, final float partialTicks, final CallbackInfoReturnable<Vector3d> ci) {
		final Vector3d skyColor = TerritoryRenderer.getSkyColor();
		if (skyColor != null) {
			ci.setReturnValue(skyColor);
		}
	}
}
