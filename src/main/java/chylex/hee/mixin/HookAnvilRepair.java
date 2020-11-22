package chylex.hee.mixin;
import chylex.hee.game.item.repair.RepairHandler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.RepairContainer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RepairContainer.class)
public abstract class HookAnvilRepair{
	@Shadow
	@Final
	private PlayerEntity player;
	
	@Inject(
		method = "updateRepairOutput",
		at = @At(value = "INVOKE", target = "Lnet/minecraftforge/common/ForgeHooks;onAnvilChange(Lnet/minecraft/inventory/container/RepairContainer;Lnet/minecraft/item/ItemStack;Lnet/minecraft/item/ItemStack;Lnet/minecraft/inventory/IInventory;Ljava/lang/String;I)Z")
	)
	private void beforeAnvilChangeEvent(final CallbackInfo ci){
		RepairHandler.isPlayerCreative.set(Boolean.valueOf(player.abilities.isCreativeMode));
	}
}
