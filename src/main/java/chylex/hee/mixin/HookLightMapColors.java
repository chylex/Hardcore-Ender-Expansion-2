package chylex.hee.mixin;
import chylex.hee.game.world.WorldProviderEndCustom;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;

@Mixin(LightTexture.class)
public abstract class HookLightMapColors{
	@Redirect(
		method = "updateLightmap",
		at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/Vector3f;set(FFF)V"),
		slice = @Slice(
			from = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/LightTexture;getLightBrightness(Lnet/minecraft/world/World;I)F"),
			to = @At(value = "FIELD", target = "Lnet/minecraft/world/dimension/DimensionType;THE_END:Lnet/minecraft/world/dimension/DimensionType;")
		)
	)
	public void onInitialColorsSet(final Vector3f colors, final float red, final float green, final float blue){
		WorldProviderEndCustom.setBlockLight(red); // UPDATE 1.15 first parameter is always block light
		colors.set(red, green, blue);
	}
}
