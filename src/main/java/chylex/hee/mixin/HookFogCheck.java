package chylex.hee.mixin;

import chylex.hee.client.render.TerritoryRenderer;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.world.DimensionRenderInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(WorldRenderer.class)
public abstract class HookFogCheck {
	@Redirect(
		method = "updateCameraAndRender",
		at = @At(value = "INVOKE", target = "Lnet/minecraft/client/world/DimensionRenderInfo;func_230493_a_(II)Z")
	)
	private boolean redirectDoesXZShowFog(final DimensionRenderInfo info, final int x, final int z) {
		return info.func_230493_a_(x, z) || TerritoryRenderer.isActive();
	}
}
