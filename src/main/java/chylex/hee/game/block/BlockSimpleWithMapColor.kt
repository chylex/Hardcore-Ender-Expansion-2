package chylex.hee.game.block
import chylex.hee.game.block.info.BlockBuilder
import net.minecraft.block.BlockState
import net.minecraft.block.material.MaterialColor
import net.minecraft.util.math.BlockPos
import net.minecraft.world.IBlockReader

open class BlockSimpleWithMapColor(builder: BlockBuilder, private val color: MaterialColor) : BlockSimple(builder){
	override fun getMaterialColor(state: BlockState, world: IBlockReader, pos: BlockPos): MaterialColor{
		return color
	}
}
