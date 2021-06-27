package chylex.hee.mixin;

import chylex.hee.client.render.TerritoryRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.vector.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(LightTexture.class)
public abstract class HookLightTexture {
	@Inject(
		method = "updateLightmap",
		locals = LocalCapture.CAPTURE_FAILHARD,
		at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/vector/Vector3f;clamp(FF)V"),
		slice = @Slice(
			from = @At(value = "INVOKE", target = "Lnet/minecraft/client/world/DimensionRenderInfo;func_241684_d_()Z"),
			to = @At(value = "FIELD", target = "Lnet/minecraft/client/GameSettings;gamma:D")
		)
	)
	private void updateLightmapColors(final float partialTicks, final CallbackInfo ci, final ClientWorld clientworld, final float f, final float f1, final float f3, final float f2, final Vector3f vector3f, final float f4, final Vector3f vector3f1, final int i, final int j, final float f5, final float f6) {
		if (TerritoryRenderer.isActive()) {
			TerritoryRenderer.updateLightmap(partialTicks, clientworld.getSunBrightness(1.0F), f6, f5, vector3f1);
		}
	}
}
