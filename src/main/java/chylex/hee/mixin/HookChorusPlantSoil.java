package chylex.hee.mixin;
import chylex.hee.init.ModBlocks;
import net.minecraft.block.Block;
import net.minecraft.block.ChorusPlantBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ChorusPlantBlock.class)
@SuppressWarnings("MethodMayBeStatic")
public abstract class HookChorusPlantSoil {
	@Redirect(
		method = "makeConnections",
		at = @At(value = "FIELD", target = "Lnet/minecraft/block/Blocks;END_STONE:Lnet/minecraft/block/Block;"),
		require = 1
	)
	private Block replaceInMakeConnections() {
		return ModBlocks.HUMUS;
	}
	
	@Redirect(
		method = "updatePostPlacement",
		at = @At(value = "FIELD", target = "Lnet/minecraft/block/Blocks;END_STONE:Lnet/minecraft/block/Block;"),
		require = 1
	)
	private Block replaceInUpdatePostPlacement() {
		return ModBlocks.HUMUS;
	}
	
	@Redirect(
		method = "isValidPosition",
		at = @At(value = "FIELD", target = "Lnet/minecraft/block/Blocks;END_STONE:Lnet/minecraft/block/Block;"),
		require = 2
	)
	private Block replaceInIsValidPosition() {
		return ModBlocks.HUMUS;
	}
}
