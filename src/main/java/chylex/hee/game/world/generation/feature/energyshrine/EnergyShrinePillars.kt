package chylex.hee.game.world.generation.feature.energyshrine

import chylex.hee.game.block.util.withFacing
import chylex.hee.game.world.util.Facing4
import chylex.hee.game.world.util.blocksMovement
import chylex.hee.game.world.util.getState
import chylex.hee.game.world.util.isAir
import chylex.hee.game.world.util.offsetUntil
import chylex.hee.game.world.util.setBlock
import chylex.hee.game.world.util.setState
import chylex.hee.init.ModBlocks
import chylex.hee.util.math.PosXZ
import chylex.hee.util.random.nextInt
import chylex.hee.util.random.nextItem
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.tags.BlockTags
import net.minecraft.util.Direction.DOWN
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import java.util.Random
import kotlin.math.max

object EnergyShrinePillars {
	fun isReplaceable(world: World, pos: BlockPos): Boolean {
		val state = pos.getState(world)
		return !state.material.blocksMovement() || state.isIn(BlockTags.LEAVES)
	}
	
	fun tryGenerate(world: World, rand: Random, surfacePos: BlockPos): Boolean {
		val surfaceXZ = PosXZ(surfacePos)
		
		val amount = if (rand.nextBoolean()) 3 else 4
		val heights = if (rand.nextBoolean()) intArrayOf(3, 2, 2, 1) else intArrayOf(3, 2, 1, 1)
		
		val usedXZ = mutableSetOf<PosXZ>()
		val bottomLocations = mutableListOf<BlockPos>()
		
		for (attempt in 1..50) {
			val testXZ = surfaceXZ.add( // starting room has even dimensions
				rand.nextInt(-4, 3),
				rand.nextInt(-4, 3)
			)
			
			if (usedXZ.contains(testXZ)) {
				continue
			}
			
			val height = heights[bottomLocations.size]
			val testPos = testXZ.withY(surfacePos.y + 3).offsetUntil(DOWN, 0..7) { !isReplaceable(world, it) }
			
			if (testPos == null || (1 until height).any { testPos.up(it).blocksMovement(world) }) {
				continue
			}
			
			bottomLocations.add(testPos.up())
			
			if (bottomLocations.size == amount) {
				break
			}
			
			usedXZ.add(testXZ)
			
			for (facing in Facing4) {
				usedXZ.add(testXZ.offset(facing))
			}
		}
		
		if (bottomLocations.size != amount) {
			return false
		}
		
		for ((index, bottomPos) in bottomLocations.withIndex()) {
			val height = heights[index]
			val fullBlock = if (rand.nextBoolean()) ModBlocks.GLOOMROCK_SMOOTH else ModBlocks.GLOOMROCK_BRICKS
			
			for (yOffset in 0..max(0, height - 2)) {
				bottomPos.up(yOffset).setBlock(world, fullBlock)
			}
			
			if (height > 1) {
				val topPos = bottomPos.up(height - 1)
				
				if (topPos.isAir(world)) {
					topPos.setState(world, pickPillarTopBlock(rand, fullBlock))
				}
			}
		}
		
		return true
	}
	
	private fun pickPillarTopBlock(rand: Random, fullBlock: Block): BlockState {
		if (rand.nextBoolean()) {
			val topBlock = if (fullBlock === ModBlocks.GLOOMROCK_SMOOTH)
				ModBlocks.GLOOMROCK_SMOOTH_SLAB
			else
				ModBlocks.GLOOMROCK_BRICK_SLAB
			
			return topBlock.defaultState
		}
		else {
			val topBlock = if (fullBlock === ModBlocks.GLOOMROCK_SMOOTH)
				ModBlocks.GLOOMROCK_SMOOTH_STAIRS
			else
				ModBlocks.GLOOMROCK_BRICK_STAIRS
			
			return topBlock.withFacing(rand.nextItem(Facing4))
		}
	}
}
