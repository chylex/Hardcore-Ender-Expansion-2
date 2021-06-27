package chylex.hee.mixin;

import chylex.hee.client.MC;
import chylex.hee.system.Debug;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Minecraft.WorldSelectionType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public abstract class HookUselessExperimentalWorldWarning { // UPDATE remove when https://github.com/MinecraftForge/MinecraftForge/pull/7275 is pulled
	@Inject(method = "deleteWorld", at = @At("HEAD"), cancellable = true)
	private void ignoreUselessWarning(final WorldSelectionType selectionType, final String worldName, final boolean customized, final Runnable runnable, final CallbackInfo ci) {
		if (Debug.enabled) {
			ci.cancel();
			MC.instance.enqueue(runnable);
		}
	}
}
