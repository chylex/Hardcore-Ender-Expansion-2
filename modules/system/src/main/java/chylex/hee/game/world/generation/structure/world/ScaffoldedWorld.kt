package chylex.hee.game.world.generation.structure.world

import chylex.hee.game.world.generation.structure.IStructureWorld
import chylex.hee.game.world.generation.structure.world.segments.SegmentFull
import chylex.hee.game.world.util.allInBoxMutable
import chylex.hee.util.math.Size
import com.google.common.collect.Iterables
import net.minecraft.block.AbstractBlock
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.block.material.Material
import net.minecraft.util.math.BlockPos
import java.util.Random

class ScaffoldedWorld(rand: Random, size: Size) : SegmentedWorld(rand, size, size, { segmentSize -> SegmentFull(segmentSize, SCAFFOLDING) }) {
	private companion object {
		private val SCAFFOLDING: BlockState = Block(AbstractBlock.Properties.create(Material.AIR)).defaultState
	}
	
	val allPositionsMutable
		get() = worldSize.minPos.allInBoxMutable(worldSize.maxPos)
	
	val usedPositionsMutable: Iterable<BlockPos.Mutable>
		get() = Iterables.filter(allPositionsMutable) { !isUnused(it!!) }
	
	override fun isAir(pos: BlockPos): Boolean {
		return super.isAir(pos) || isUnused(pos)
	}
	
	fun isUnused(pos: BlockPos): Boolean {
		return getState(pos) === SCAFFOLDING
	}
	
	fun markUnused(pos: BlockPos) {
		setState(pos, SCAFFOLDING)
	}
	
	fun cloneInto(world: IStructureWorld, origin: BlockPos) {
		for (offset in allPositionsMutable) {
			val state = getState(offset)
			
			if (state !== SCAFFOLDING) {
				world.setState(origin.add(offset), state)
			}
		}
		
		for ((offset, trigger) in getTriggers()) {
			world.addTrigger(origin.add(offset), trigger)
		}
	}
}
