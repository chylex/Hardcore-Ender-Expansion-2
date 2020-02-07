package chylex.hee.game.world.feature.basic.blobs.impl
import chylex.hee.game.world.feature.basic.blobs.BlobGenerator
import chylex.hee.game.world.feature.basic.blobs.IBlobPopulator
import chylex.hee.game.world.generation.SegmentedWorld
import chylex.hee.game.world.structure.IBlockPicker
import chylex.hee.game.world.structure.IBlockPicker.Single
import chylex.hee.system.util.allInBox
import chylex.hee.system.util.facades.Facing6
import net.minecraft.block.Block
import java.util.Random

class BlobPopulatorFill(
	private val picker: IBlockPicker,
	private val base: Block = BlobGenerator.BASE
) : IBlobPopulator{
	constructor(block: Block) : this(Single(block))
	
	override fun generate(world: SegmentedWorld, rand: Random){
		val size = world.worldSize
		
		size.minPos
			.allInBox(size.maxPos)
			.filter { pos -> world.getBlock(pos) === base && Facing6.all { facing -> pos.offset(facing).let { world.isInside(it) && world.getBlock(it) === base } } }
			.forEach { world.placeBlock(it, picker) }
	}
}
