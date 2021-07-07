package chylex.hee.game.world.generation.blob.populators

import chylex.hee.game.world.generation.IBlockPicker
import chylex.hee.game.world.generation.IBlockPicker.Single
import chylex.hee.game.world.generation.blob.BlobGenerator
import chylex.hee.game.world.generation.blob.IBlobPopulator
import chylex.hee.game.world.generation.structure.world.ScaffoldedWorld
import chylex.hee.game.world.util.Facing6
import chylex.hee.game.world.util.allInBox
import net.minecraft.block.Block
import java.util.Random

class BlobPopulatorFill(
	private val picker: IBlockPicker,
) : IBlobPopulator {
	constructor(block: Block) : this(Single(block))
	
	override fun generate(world: ScaffoldedWorld, rand: Random, generator: BlobGenerator) {
		val size = world.worldSize
		val base = generator.base
		
		size.minPos
			.allInBox(size.maxPos)
			.filter { pos -> world.getBlock(pos) === base && Facing6.all { facing -> pos.offset(facing).let { world.isInside(it) && world.getBlock(it) === base } } }
			.forEach { world.placeBlock(it, picker) }
	}
}
