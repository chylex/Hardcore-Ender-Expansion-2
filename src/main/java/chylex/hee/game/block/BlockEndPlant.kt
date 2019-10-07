package chylex.hee.game.block
import chylex.hee.game.block.util.CustomPlantType
import chylex.hee.system.migration.vanilla.Blocks
import net.minecraft.block.BlockBush
import net.minecraft.block.state.IBlockState
import net.minecraft.util.math.BlockPos
import net.minecraft.world.IBlockAccess
import net.minecraftforge.common.EnumPlantType

open class BlockEndPlant : BlockBush(){
	init{
		needsRandomTick = false
	}
	
	override fun canSustainBush(state: IBlockState): Boolean{
		return state.block.let { it === Blocks.DIRT || it === Blocks.GRASS || it === Blocks.END_STONE }
	}
	
	override fun getPlantType(world: IBlockAccess, pos: BlockPos): EnumPlantType{
		return CustomPlantType.END
	}
}
