package chylex.hee.mixin;
import chylex.hee.game.potion.CorruptionEffect;
import net.minecraft.entity.LivingEntity;
import net.minecraft.potion.EffectInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EffectInstance.class)
public abstract class HookEffectInstanceCorruption {
	@Inject(method = "performEffect", at = @At("HEAD"), cancellable = true)
	public void beforePerformEffect(final LivingEntity affectedEntity, final CallbackInfo ci) {
		if (CorruptionEffect.shouldCorrupt((EffectInstance)(Object)this, affectedEntity)) {
			ci.cancel();
		}
	}
}
