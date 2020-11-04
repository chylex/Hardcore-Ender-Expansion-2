package chylex.hee.mixin;
import chylex.hee.game.potion.PotionCorruption;
import net.minecraft.entity.LivingEntity;
import net.minecraft.potion.Effect;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class HookEntityPotionCorruption{
	@Inject(method = "isPotionActive", at = @At("HEAD"), cancellable = true)
	public void beforePerformEffect(final Effect effect, final CallbackInfoReturnable<Boolean> ci){
		if (PotionCorruption.shouldCorrupt(effect, (LivingEntity)(Object)this)){
			ci.setReturnValue(Boolean.FALSE);
		}
	}
}
