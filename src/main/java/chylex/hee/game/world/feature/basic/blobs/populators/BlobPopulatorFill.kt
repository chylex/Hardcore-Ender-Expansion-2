package chylex.hee.game.world.feature.basic.blobs.populators

import chylex.hee.game.world.allInBox
import chylex.hee.game.world.feature.basic.blobs.BlobGenerator
import chylex.hee.game.world.feature.basic.blobs.IBlobPopulator
import chylex.hee.game.world.generation.IBlockPicker
import chylex.hee.game.world.generation.IBlockPicker.Single
import chylex.hee.game.world.generation.ScaffoldedWorld
import chylex.hee.system.facades.Facing6
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
