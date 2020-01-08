package chylex.hee.game.world.structure.file
import chylex.hee.game.world.structure.IBlockPicker
import chylex.hee.game.world.structure.IBlockPicker.Single
import com.google.common.collect.ImmutableBiMap
import net.minecraft.block.BlockState

class Palette(
	private val forGeneration: Map<String, IBlockPicker>,
	private val forDevelopment: ImmutableBiMap<String, BlockState>
){
	val mappingForGeneration
		get() = forGeneration
	
	val mappingForDevelopment
		get() = forDevelopment.mapValues { Single(it.value) }
	
	val lookupForDevelopment: ImmutableBiMap<BlockState, String>
		get() = forDevelopment.inverse()
}
