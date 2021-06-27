package chylex.hee.mixin;

import chylex.hee.game.block.logic.IBlockDynamicHardness;
import chylex.hee.game.world.PosExtKt;
import net.minecraft.block.AbstractBlock.AbstractBlockState;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractBlockState.class)
public abstract class HookBlockHardness {
	@Inject(method = "getBlockHardness", at = @At("RETURN"), cancellable = true)
	private void getBlockHardness(final IBlockReader world, final BlockPos pos, final CallbackInfoReturnable<Float> ci) {
		final BlockState state = PosExtKt.getState(pos, world);
		final Block block = state.getBlock();
		
		if (block instanceof IBlockDynamicHardness) {
			ci.setReturnValue(Float.valueOf(((IBlockDynamicHardness)block).getBlockHardness(world, pos, state, ci.getReturnValueF())));
		}
	}
}
