package chylex.hee.game.world.feature.basic.trees.types
import chylex.hee.game.block.BlockWhitebarkLeaves
import chylex.hee.game.world.feature.basic.trees.WhitebarkTreeGenerator
import chylex.hee.game.world.generation.SegmentedWorld
import chylex.hee.game.world.util.Size
import chylex.hee.init.ModBlocks
import chylex.hee.system.util.allInCenteredBoxMutable
import chylex.hee.system.util.facades.Facing4
import chylex.hee.system.util.nextInt
import net.minecraft.util.math.BlockPos
import java.util.Random
import kotlin.math.abs

abstract class AutumnTreeGenerator : WhitebarkTreeGenerator(){
	private companion object{
		private val LEAVES_TALL = intArrayOf(1, 1, 2, 2).withIndex().toList()
		private val LEAVES_SHORT = intArrayOf(1, 1, 2).withIndex().toList()
	}
	
	protected abstract val leafBlock: BlockWhitebarkLeaves
	
	final override val size = Size(5, 10, 5)
	
	final override fun place(world: SegmentedWorld, rand: Random, root: BlockPos){
		val rootHeight = if (rand.nextBoolean())
			rand.nextInt(6, 7)
		else
			rand.nextInt(6, 9)
		
		for(y in 0 until rootHeight){
			world.setBlock(root.up(y), ModBlocks.WHITEBARK_LOG)
		}
		
		val leafArrangement = when(rootHeight){
			6 -> LEAVES_SHORT
			7 -> if (rand.nextBoolean()) LEAVES_TALL else LEAVES_SHORT
			else -> LEAVES_TALL
		}
		
		world.setBlock(root.up(rootHeight), leafBlock)
		
		for((y, size) in leafArrangement){
			for(pos in root.up(rootHeight - 1 - y).allInCenteredBoxMutable(size, 0, size)){
				if (pos.x == root.x && pos.z == root.z){
					continue
				}
				
				if (y % 2 == 0 && abs(pos.x - root.x) == size && abs(pos.z - root.z) == size){
					continue
				}
				
				world.setBlock(pos, leafBlock)
			}
		}
		
		val leafBottomY = rootHeight - 1 - leafArrangement.size
		
		repeat(rand.nextInt(rootHeight - 3, rootHeight + 1)){
			for(attempt in 1..4){
				val pos = root.add(
					rand.nextInt(-2, 2),
					leafBottomY,
					rand.nextInt(-2, 2)
				)
				
				if (world.isAir(pos) && world.getBlock(pos.up()) === leafBlock && Facing4.all { facing -> pos.offset(facing).let { !world.isInside(it) || world.getBlock(it) !== leafBlock } }){
					world.setBlock(pos, leafBlock)
					break
				}
			}
		}
	}
	
	object Brown : AutumnTreeGenerator(){
		override val leafBlock
			get() = ModBlocks.WHITEBARK_LEAVES_AUTUMN_BROWN
	}
	
	object Orange : AutumnTreeGenerator(){
		override val leafBlock
			get() = ModBlocks.WHITEBARK_LEAVES_AUTUMN_ORANGE
	}
	
	object YellowGreen : AutumnTreeGenerator(){
		override val leafBlock
			get() = ModBlocks.WHITEBARK_LEAVES_AUTUMN_YELLOWGREEN
	}
}
