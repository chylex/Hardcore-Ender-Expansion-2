package chylex.hee.game.world.generation
import chylex.hee.game.world.generation.segments.SegmentFull
import chylex.hee.game.world.structure.IStructureWorld
import chylex.hee.game.world.util.Size
import chylex.hee.init.ModBlocks
import chylex.hee.system.migration.vanilla.MutableBlockPos
import chylex.hee.system.util.allInBoxMutable
import com.google.common.collect.Iterables
import net.minecraft.block.BlockState
import net.minecraft.util.math.BlockPos
import java.util.Random

class ScaffoldedWorld(rand: Random, size: Size) : SegmentedWorld(rand, size, size, SEGMENTER){
	private companion object{
		private val SCAFFOLDING: BlockState = ModBlocks.SCAFFOLDING.defaultState
		private val SEGMENTER = { size: Size -> SegmentFull(size, SCAFFOLDING) }
	}
	
	val allPositionsMutable
		get() = worldSize.minPos.allInBoxMutable(worldSize.maxPos)
	
	val usedPositionsMutable: Iterable<MutableBlockPos>
		get() = Iterables.filter(allPositionsMutable){ !isUnused(it!!) }
	
	override fun isAir(pos: BlockPos): Boolean{
		return super.isAir(pos) || isUnused(pos)
	}
	
	fun isUnused(pos: BlockPos): Boolean{
		return getState(pos) === SCAFFOLDING
	}
	
	fun markUnused(pos: BlockPos){
		setState(pos, SCAFFOLDING)
	}
	
	fun cloneInto(world: IStructureWorld, origin: BlockPos){
		for(offset in allPositionsMutable){
			val state = getState(offset)
			
			if (state !== SCAFFOLDING){
				world.setState(origin.add(offset), state)
			}
		}
		
		for((offset, trigger) in getTriggers()){
			world.addTrigger(origin.add(offset), trigger)
		}
	}
}
