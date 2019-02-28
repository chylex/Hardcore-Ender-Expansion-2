package chylex.hee.game.world.structure.file
import chylex.hee.game.world.structure.IBlockPicker
import chylex.hee.game.world.structure.IBlockPicker.Single
import com.google.common.collect.ImmutableBiMap
import net.minecraft.block.state.IBlockState

class Palette(
	private val forGeneration: Map<String, IBlockPicker>,
	private val forDevelopment: ImmutableBiMap<String, IBlockState>
){
	val mappingForGeneration
		get() = forGeneration
	
	val mappingForDevelopment
		get() = forDevelopment.mapValues { Single(it.value) }
	
	val lookupForDevelopment: ImmutableBiMap<IBlockState, String>
		get() = forDevelopment.inverse()
}
