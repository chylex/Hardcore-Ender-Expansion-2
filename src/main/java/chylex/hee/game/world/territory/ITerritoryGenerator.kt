package chylex.hee.game.world.territory
import chylex.hee.game.world.generation.SegmentedWorld
import chylex.hee.game.world.generation.TerritoryGenerationInfo
import chylex.hee.game.world.util.Size
import chylex.hee.system.migration.vanilla.Blocks
import net.minecraft.block.Block

interface ITerritoryGenerator{
	val segmentSize: Size
	
	@JvmDefault
	val defaultBlock: Block
		get() = Blocks.AIR
	
	@JvmDefault
	val groundBlock: Block
		get() = Blocks.END_STONE
	
	fun provide(world: SegmentedWorld): TerritoryGenerationInfo
}
