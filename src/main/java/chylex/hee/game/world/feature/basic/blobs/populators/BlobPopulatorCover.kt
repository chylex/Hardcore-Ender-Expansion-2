package chylex.hee.game.world.feature.basic.blobs.populators
import chylex.hee.game.world.feature.basic.blobs.BlobGenerator
import chylex.hee.game.world.feature.basic.blobs.IBlobPopulator
import chylex.hee.game.world.generation.ScaffoldedWorld
import chylex.hee.game.world.structure.IBlockPicker
import chylex.hee.game.world.structure.IBlockPicker.Single
import chylex.hee.system.util.Pos
import chylex.hee.system.util.offsetUntil
import net.minecraft.block.Block
import net.minecraft.util.Direction.DOWN
import java.util.Random

class BlobPopulatorCover(
	private val picker: IBlockPicker,
	private val replace: Boolean
) : IBlobPopulator{
	constructor(block: Block, replace: Boolean) : this(Single(block), replace)
	
	override fun generate(world: ScaffoldedWorld, rand: Random, generator: BlobGenerator){
		val size = world.worldSize
		val base = generator.base
		
		for(x in 0..size.maxX) for(z in 0..size.maxZ){
			if (replace){
				Pos(x, size.maxY, z).offsetUntil(DOWN, 0..size.maxY){ !world.isAir(it) }?.takeIf { world.getBlock(it) === base }?.let { world.placeBlock(it, picker) }
			}
			else{
				Pos(x, size.maxY, z).offsetUntil(DOWN, 1..size.maxY){ !world.isAir(it) }?.takeIf { world.getBlock(it) === base }?.let { world.placeBlock(it.up(), picker) }
			}
		}
	}
}
