package chylex.hee.mixin;
import chylex.hee.game.potion.PotionCorruption;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.AttributeModifierManager;
import net.minecraft.potion.Effect;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Effect.class)
public abstract class HookEffectCorruption{
	@Inject(method = "affectEntity", at = @At("HEAD"), cancellable = true)
	public void beforeAffectEntity(final Entity directSource, final Entity indirectSource, final LivingEntity affectedEntity, final int amplifier, final double multiplier, final CallbackInfo ci){
		if (PotionCorruption.shouldCorrupt((Effect)(Object)this, affectedEntity)){
			ci.cancel();
		}
	}
	
	@Inject(method = "applyAttributesModifiersToEntity", at = @At("HEAD"), cancellable = true)
	public void beforeApplyAttributesModifiersToEntity(final LivingEntity affectedEntity, final AttributeModifierManager attributeMap, final int amplifier, final CallbackInfo ci){
		if (PotionCorruption.shouldSkipAttributeChange((Effect)(Object)this, affectedEntity)){
			ci.cancel();
		}
	}
	
	@Inject(method = "removeAttributesModifiersFromEntity", at = @At("HEAD"), cancellable = true)
	public void beforeRemoveAttributesModifiersFromEntity(final LivingEntity affectedEntity, final AttributeModifierManager attributeMap, final int amplifier, final CallbackInfo ci){
		if (PotionCorruption.shouldSkipAttributeChange((Effect)(Object)this, affectedEntity)){
			ci.cancel();
		}
	}
}
