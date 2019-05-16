package chylex.hee.game.block
import chylex.hee.game.block.BlockSimple.Builder.Companion.setHarvestTool
import chylex.hee.game.item.util.Tool.Level.WOOD
import chylex.hee.game.item.util.Tool.Type.AXE
import net.minecraft.block.BlockLog
import net.minecraft.block.BlockLog.EnumAxis.Y
import net.minecraft.block.material.MapColor
import net.minecraft.block.state.BlockStateContainer
import net.minecraft.block.state.IBlockState
import net.minecraft.util.math.BlockPos
import net.minecraft.world.IBlockAccess

class BlockWhitebarkLog : BlockLog(){
	init{
		setHarvestTool(Pair(WOOD, AXE))
		defaultState = blockState.baseState.withProperty(LOG_AXIS, Y) // UPDATE figure out what happens to the bark variant
	}
	
	override fun createBlockState(): BlockStateContainer = BlockStateContainer(this, LOG_AXIS)
	
	override fun getMetaFromState(state: IBlockState): Int = state.getValue(LOG_AXIS).ordinal
	override fun getStateFromMeta(meta: Int): IBlockState = defaultState.withProperty(LOG_AXIS, EnumAxis.values()[meta])
	
	override fun getMapColor(state: IBlockState, world: IBlockAccess, pos: BlockPos): MapColor{
		return MapColor.SNOW
	}
}
