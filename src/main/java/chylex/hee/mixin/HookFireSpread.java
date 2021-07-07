package chylex.hee.mixin;
import chylex.hee.game.block.logic.IBlockFireCatchOverride;
import net.minecraft.block.Block;
import net.minecraft.block.FireBlock;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import java.util.Random;

@Mixin(FireBlock.class)
@SuppressWarnings("MethodMayBeStatic")
public abstract class HookFireSpread {
	@Inject(method = "tryCatchFire", at = @At("HEAD"), cancellable = true, remap = false)
	private void beforeCatchFire(final World world, final BlockPos pos, final int chance, final Random rand, final int age, final Direction face, final CallbackInfo ci) {
		final Block block = world.getBlockState(pos).getBlock();
		
		if (block instanceof IBlockFireCatchOverride) {
			((IBlockFireCatchOverride)block).tryCatchFire(world, pos, chance, rand);
			ci.cancel();
		}
	}
}
