package chylex.hee.mixin;

import chylex.hee.game.item.ItemRingOfPreservation;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import java.util.function.Consumer;

@Mixin(PlayerInventory.class)
@SuppressWarnings("MethodMayBeStatic")
public abstract class HookPlayerInventoryArmorBreaking {
	@Redirect(
		method = "func_234563_a_", // RENAME damageArmor
		at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;damageItem(ILnet/minecraft/entity/LivingEntity;Ljava/util/function/Consumer;)V")
	)
	public void damageItem(final ItemStack stack, final int amount, final LivingEntity entity, final Consumer<LivingEntity> onBroken) {
		ItemRingOfPreservation.handleArmorDamage(entity, stack, amount, onBroken);
	}
}
