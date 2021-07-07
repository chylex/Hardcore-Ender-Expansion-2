package chylex.hee.mixin;

import chylex.hee.init.ModItems;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;
import java.util.function.Consumer;

@Mixin(LivingEntity.class)
@SuppressWarnings("MethodMayBeStatic")
public abstract class HookEntityHelmetBreaking {
	@Redirect(
		method = "attackEntityFrom",
		at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;damageItem(ILnet/minecraft/entity/LivingEntity;Ljava/util/function/Consumer;)V"),
		slice = @Slice(from = @At(value = "FIELD", target = "Lnet/minecraft/inventory/EquipmentSlotType;HEAD:Lnet/minecraft/inventory/EquipmentSlotType;"))
	)
	public void damageItem(final ItemStack stack, final int amount, final LivingEntity entity, final Consumer<LivingEntity> onBroken) {
		ModItems.RING_OF_PRESERVATION.handleArmorDamage(entity, stack, amount, onBroken);
	}
}
