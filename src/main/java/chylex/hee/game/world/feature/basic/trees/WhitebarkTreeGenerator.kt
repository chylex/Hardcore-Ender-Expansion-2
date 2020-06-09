package chylex.hee.game.world.feature.basic.trees
import chylex.hee.game.block.BlockWhitebarkLeaves
import chylex.hee.game.block.BlockWhitebarkSapling
import chylex.hee.game.world.generation.ScaffoldedWorld
import chylex.hee.game.world.generation.SegmentedWorld
import chylex.hee.game.world.structure.IStructureWorld
import chylex.hee.game.world.util.Size
import chylex.hee.game.world.util.Size.Alignment.CENTER
import chylex.hee.game.world.util.Size.Alignment.MIN
import net.minecraft.util.math.BlockPos
import java.util.Random

abstract class WhitebarkTreeGenerator<T>{
	protected abstract val size: Size
	
	protected open val root
		get() = size.getPos(CENTER, MIN, CENTER)
	
	protected abstract fun place(world: SegmentedWorld, rand: Random, root: BlockPos, parameter: T?)
	
	fun generate(world: IStructureWorld, root: BlockPos, parameter: T? = null): Boolean{
		val bottomCenter = size.getPos(CENTER, MIN, CENTER)
		val origin = root.subtract(bottomCenter)
		
		val rand = world.rand
		val treeWorld = ScaffoldedWorld(rand, size)
		
		place(treeWorld, rand, bottomCenter, parameter)
		
		for(treePos in treeWorld.usedPositionsMutable){
			val realPos = origin.add(treePos)
			
			if (!world.isAir(realPos)){
				val realBlock = world.getBlock(realPos)
				
				if (!(realBlock is BlockWhitebarkSapling || (realBlock is BlockWhitebarkLeaves && treeWorld.getBlock(treePos) is BlockWhitebarkLeaves))){
					return false
				}
			}
		}
		
		treeWorld.cloneInto(world, origin)
		return true
	}
}
