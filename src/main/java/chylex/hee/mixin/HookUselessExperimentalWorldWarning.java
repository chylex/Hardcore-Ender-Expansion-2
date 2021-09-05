package chylex.hee.mixin;

import chylex.hee.HEE;
import chylex.hee.client.util.MC;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Minecraft.WorldSelectionType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
@SuppressWarnings("MethodMayBeStatic")
public abstract class HookUselessExperimentalWorldWarning { // UPDATE remove when https://github.com/MinecraftForge/MinecraftForge/pull/7275 is pulled
	@Inject(method = "deleteWorld", at = @At("HEAD"), cancellable = true)
	private void ignoreUselessWarning(final WorldSelectionType selectionType, final String worldName, final boolean customized, final Runnable runnable, final CallbackInfo ci) {
		if (HEE.debug) {
			ci.cancel();
			MC.instance.enqueue(runnable);
		}
	}
}
