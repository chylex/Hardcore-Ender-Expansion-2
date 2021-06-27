package chylex.hee.mixin;

import chylex.hee.client.render.TerritoryRenderer;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.client.world.ClientWorld.ClientWorldInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(FogRenderer.class)
public abstract class HookFogDistance {
	@Redirect(
		method = "updateFogColor",
		at = @At(value = "INVOKE", target = "Lnet/minecraft/client/world/ClientWorld$ClientWorldInfo;getFogDistance()D")
	)
	private static double replaceFogDistance(final ClientWorldInfo info) {
		return TerritoryRenderer.isActive() ? 1.0 : info.getFogDistance();
	}
}
