package chylex.hee.game.world.feature.basic.trees.types
import chylex.hee.game.block.BlockWhitebarkLeaves
import chylex.hee.game.block.with
import chylex.hee.game.world.allInCenteredBoxMutable
import chylex.hee.game.world.feature.basic.trees.WhitebarkTreeGenerator
import chylex.hee.game.world.generation.SegmentedWorld
import chylex.hee.game.world.math.Size
import chylex.hee.init.ModBlocks
import chylex.hee.system.facades.Facing4
import chylex.hee.system.migration.BlockLeaves
import chylex.hee.system.random.nextInt
import net.minecraft.util.math.BlockPos
import java.util.Random
import kotlin.math.abs

abstract class AutumnTreeGenerator : WhitebarkTreeGenerator<IntRange>(){
	private companion object{
		private val LEAVES_TALL = intArrayOf(1, 1, 2, 2).withIndex().toList()
		private val LEAVES_SHORT = intArrayOf(1, 1, 2).withIndex().toList()
	}
	
	abstract val leafBlock: BlockWhitebarkLeaves
	
	final override val size = Size(5, 12, 5) // trees in Lost Garden can be up to 12 blocks tall
	
	private fun pickRandomHeight(rand: Random): Int{
		return if (rand.nextBoolean())
			rand.nextInt(6, 8)
		else
			rand.nextInt(7, 10)
	}
	
	final override fun place(world: SegmentedWorld, rand: Random, root: BlockPos, parameter: IntRange?){
		val leaf = leafBlock.with(BlockLeaves.DISTANCE, 1)
		val rootHeight = parameter?.let(rand::nextInt) ?: pickRandomHeight(rand)
		
		if (rootHeight >= size.y){
			throw IllegalArgumentException("autumn tree root must be at most ${size.y - 1} block tall")
		}
		
		for(y in 0 until rootHeight){
			world.setBlock(root.up(y), ModBlocks.WHITEBARK_LOG)
		}
		
		val leafArrangement = when(rootHeight){
			6 -> LEAVES_SHORT
			7 -> if (rand.nextBoolean()) LEAVES_TALL else LEAVES_SHORT
			else -> LEAVES_TALL
		}
		
		world.setState(root.up(rootHeight), leaf)
		
		if (rand.nextInt(5) > 0){
			world.setState(root.up(rootHeight - 1), leaf)
		}
		
		for((y, size) in leafArrangement){
			for(pos in root.up(rootHeight - 1 - y).allInCenteredBoxMutable(size, 0, size)){
				if (pos.x == root.x && pos.z == root.z){
					continue
				}
				
				if (y % 2 == 0 && abs(pos.x - root.x) == size && abs(pos.z - root.z) == size){
					continue
				}
				
				world.setState(pos, leaf)
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
					world.setState(pos, leaf)
					break
				}
			}
		}
	}
	
	object Red : AutumnTreeGenerator(){
		override val leafBlock
			get() = ModBlocks.WHITEBARK_LEAVES_AUTUMN_RED
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
