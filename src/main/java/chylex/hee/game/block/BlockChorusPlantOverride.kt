package chylex.hee.game.block
import chylex.hee.init.ModBlocks
import chylex.hee.init.ModLoot
import chylex.hee.system.migration.vanilla.Blocks
import chylex.hee.system.util.facades.Facing4
import chylex.hee.system.util.get
import chylex.hee.system.util.getBlock
import chylex.hee.system.util.isAir
import chylex.hee.system.util.translationKeyOriginal
import chylex.hee.system.util.with
import net.minecraft.block.BlockChorusPlant
import net.minecraft.block.state.IBlockState
import net.minecraft.item.ItemStack
import net.minecraft.util.NonNullList
import net.minecraft.util.math.BlockPos
import net.minecraft.world.IBlockAccess
import net.minecraft.world.World

class BlockChorusPlantOverride : BlockChorusPlant(){
	init{
		val source = Blocks.CHORUS_PLANT
		
		setHardness(source.blockHardness)
		soundType = source.blockSoundType
		translationKey = source.translationKeyOriginal
	}
	
	override fun getDrops(drops: NonNullList<ItemStack>, world: IBlockAccess, pos: BlockPos, state: IBlockState, fortune: Int){
		ModLoot.CHORUS_PLANT.generateDrops(drops, world, fortune)
	}
	
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
	
	override fun getActualState(state: IBlockState, world: IBlockAccess, pos: BlockPos): IBlockState{
		return super.getActualState(state, world, pos).let {
			if (it[DOWN]) it else it.with(DOWN, pos.down().getBlock(world) === ModBlocks.HUMUS)
		}
	}
}
