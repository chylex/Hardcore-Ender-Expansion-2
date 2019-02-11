package chylex.hee.game.block
import chylex.hee.game.block.util.Property
import net.minecraft.block.state.BlockStateContainer
import net.minecraft.block.state.IBlockState

class BlockDeathFlowerDecaying : BlockEndPlant(){
	companion object{
		const val MIN_LEVEL = 1
		const val MAX_LEVEL = 14
		
		val LEVEL = Property.int("level", MIN_LEVEL..MAX_LEVEL)
	}
	
	override fun createBlockState(): BlockStateContainer = BlockStateContainer(this, LEVEL)
	
	// General
	
	override fun getMetaFromState(state: IBlockState): Int = state.getValue(LEVEL) - MIN_LEVEL
	override fun getStateFromMeta(meta: Int): IBlockState = defaultState.withProperty(LEVEL, meta + MIN_LEVEL)
	
	override fun damageDropped(state: IBlockState): Int{
		return state.getValue(LEVEL) - MIN_LEVEL
	}
}
