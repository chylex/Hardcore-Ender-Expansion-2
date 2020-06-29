package chylex.hee.game.block
import chylex.hee.game.block.info.BlockBuilder
import chylex.hee.game.block.util.CustomPlantType
import chylex.hee.system.migration.vanilla.BlockBush
import chylex.hee.system.migration.vanilla.Blocks
import net.minecraft.block.BlockState
import net.minecraft.util.math.BlockPos
import net.minecraft.world.IBlockReader
import net.minecraftforge.common.PlantType
import net.minecraftforge.common.Tags

open class BlockEndPlant(builder: BlockBuilder) : BlockBush(builder.p){
	override fun isValidGround(state: BlockState, world: IBlockReader, pos: BlockPos): Boolean{
		return state.block.let { it === Blocks.END_STONE || it === Blocks.GRASS_BLOCK || Tags.Blocks.DIRT.contains(it) }
	}
	
	override fun getPlantType(world: IBlockReader, pos: BlockPos): PlantType{
		return CustomPlantType.END
	}
}
