package chylex.hee.mixin;

import chylex.hee.client.render.TerritoryRenderer;
import net.minecraft.world.DimensionType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(DimensionType.class)
public abstract class HookDimensionTypeClient {
	@Inject(method = "getCelestrialAngleByTime", at = @At("HEAD"), cancellable = true)
	private void replaceCelestialAngle(final long dayTime, final CallbackInfoReturnable<Float> ci) {
		final Float overrideAngle = TerritoryRenderer.getCelestialAngle();
		if (overrideAngle != null) {
			ci.setReturnValue(overrideAngle);
		}
	}
	
	@Inject(method = "getAmbientLight", at = @At("HEAD"), cancellable = true)
	private void replaceAmbientLight(final int light, final CallbackInfoReturnable<Float> ci) {
		final float[] lightTable = TerritoryRenderer.getLightTable();
		if (lightTable != null) {
			ci.setReturnValue(Float.valueOf(lightTable[light]));
		}
	}
}
