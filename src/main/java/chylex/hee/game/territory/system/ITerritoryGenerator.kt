package chylex.hee.game.territory.system

import chylex.hee.game.world.generation.structure.world.SegmentedWorld
import chylex.hee.util.math.Size
import net.minecraft.block.Block
import net.minecraft.block.Blocks

interface ITerritoryGenerator {
	val segmentSize: Size
	
	val defaultBlock: Block
		get() = Blocks.AIR
	
	val groundBlock: Block
		get() = Blocks.END_STONE
	
	fun provide(world: SegmentedWorld): TerritoryGenerationInfo
}
