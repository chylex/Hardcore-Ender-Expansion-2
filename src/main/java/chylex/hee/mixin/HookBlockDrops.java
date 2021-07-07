package chylex.hee.mixin;
import chylex.hee.game.block.logic.IBlockHarvestDropsOverride;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Block.class)
public abstract class HookBlockDrops {
	@Inject(
		method = "spawnDrops(Lnet/minecraft/block/BlockState;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/tileentity/TileEntity;Lnet/minecraft/entity/Entity;Lnet/minecraft/item/ItemStack;)V",
		at = @At("HEAD"),
		cancellable = true
	)
	private static void beforeCheckHarvest(final BlockState state, final World world, final BlockPos pos, final TileEntity tileEntity, final Entity entity, final ItemStack stack, final CallbackInfo ci) {
		final Item item = stack.getItem();
		
		if (item instanceof IBlockHarvestDropsOverride) {
			((IBlockHarvestDropsOverride)item).onHarvestDrops(state, world, pos);
			ci.cancel();
		}
	}
}
