package chylex.hee.mixin;

import chylex.hee.init.ModItems;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import java.util.function.Consumer;

@Mixin(PlayerInventory.class)
public abstract class HookPlayerInventoryArmorBreaking{
	@Redirect(
		method = "damageArmor",
		at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;damageItem(ILnet/minecraft/entity/LivingEntity;Ljava/util/function/Consumer;)V")
	)
	public void damageItem(final ItemStack stack, final int amount, final LivingEntity entity, final Consumer<LivingEntity> onBroken){
		ModItems.RING_OF_PRESERVATION.handleArmorDamage(entity, stack, amount, onBroken);
	}
}
