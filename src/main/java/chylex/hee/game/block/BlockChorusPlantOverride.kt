package chylex.hee.game.block
import chylex.hee.game.block.info.BlockBuilder
import chylex.hee.system.migration.vanilla.BlockChorusPlant

class BlockChorusPlantOverride(builder: BlockBuilder) : BlockChorusPlant(builder.p){
	/* UPDATE coremod
	override fun canSurviveAt(world: World, pos: BlockPos): Boolean{
		val supportPos = pos.down()
		val isAirAboveAndBelow = pos.up().isAir(world) && supportPos.isAir(world)
		
		for(adjacentPlantPos in Facing4.map(pos::offset)){
			if (adjacentPlantPos.getBlock(world) === this){
				if (!isAirAboveAndBelow){
					return false
				}
				
				val blockBelowAdjacent = adjacentPlantPos.down().getBlock(world)
				
				if (blockBelowAdjacent === this || blockBelowAdjacent === ModBlocks.HUMUS){
					return true
				}
			}
		}
		
		val supportBlock = supportPos.getBlock(world)
		return supportBlock === this || supportBlock === ModBlocks.HUMUS
	}
	
	override fun getActualState(state: BlockState, world: IBlockReader, pos: BlockPos): BlockState{
		return super.getActualState(state, world, pos).let {
			if (it[DOWN]) it else it.with(DOWN, pos.down().getBlock(world) === ModBlocks.HUMUS)
		}
	}*/
}
