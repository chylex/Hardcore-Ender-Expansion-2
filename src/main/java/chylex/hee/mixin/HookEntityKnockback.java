package chylex.hee.mixin;

import chylex.hee.game.entity.living.IKnockbackMultiplier;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.DamageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(LivingEntity.class)
public abstract class HookEntityKnockback {
	@Redirect(method = "attackEntityFrom", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;applyKnockback(FDD)V"), require = 1)
	private void replaceKnockbackStrength(final LivingEntity target, final float strength, final double ratioX, final double ratioZ, final DamageSource source, final float amount) {
		final Entity immediateSource = source.getImmediateSource();
		
		if (immediateSource instanceof IKnockbackMultiplier) {
			final float mp = ((IKnockbackMultiplier)immediateSource).getLastHitKnockbackMultiplier();
			if (mp != 0.0F) {
				target.applyKnockback(strength * mp, ratioX, ratioZ);
			}
		}
		else {
			target.applyKnockback(strength, ratioX, ratioZ);
		}
	}
}
