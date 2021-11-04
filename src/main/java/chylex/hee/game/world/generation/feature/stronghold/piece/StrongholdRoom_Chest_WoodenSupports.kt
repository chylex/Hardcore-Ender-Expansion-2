package chylex.hee.game.world.generation.feature.stronghold.piece

import chylex.hee.game.block.util.withFacing
import chylex.hee.game.world.generation.feature.stronghold.StrongholdPieceType
import chylex.hee.game.world.generation.feature.stronghold.StrongholdPieces
import chylex.hee.game.world.generation.feature.stronghold.piece.StrongholdRoom_Chest_WoodenSupports.CornerContent.CHEST_AND_FLOWER_POT
import chylex.hee.game.world.generation.feature.stronghold.piece.StrongholdRoom_Chest_WoodenSupports.CornerContent.JUST_COBWEBS
import chylex.hee.game.world.generation.feature.stronghold.piece.StrongholdRoom_Chest_WoodenSupports.CornerContent.ONE_CHEST
import chylex.hee.game.world.generation.feature.stronghold.piece.StrongholdRoom_Chest_WoodenSupports.CornerContent.ONE_FLOWER_POT
import chylex.hee.game.world.generation.feature.stronghold.piece.StrongholdRoom_Chest_WoodenSupports.CornerContent.TWO_CHESTS
import chylex.hee.game.world.generation.feature.stronghold.piece.StrongholdRoom_Chest_WoodenSupports.CornerContent.TWO_FLOWER_POTS
import chylex.hee.game.world.generation.structure.IStructureWorld
import chylex.hee.game.world.generation.trigger.LootChestStructureTrigger
import chylex.hee.game.world.util.Facing4
import chylex.hee.init.ModBlocks
import chylex.hee.util.math.Pos
import chylex.hee.util.random.nextInt
import chylex.hee.util.random.nextItem
import chylex.hee.util.random.removeItem
import net.minecraft.block.Blocks
import net.minecraft.util.Direction
import net.minecraft.util.math.BlockPos
import java.util.Random

class StrongholdRoom_Chest_WoodenSupports(file: String) : StrongholdAbstractPieceFromFile(file, StrongholdPieceType.ROOM) {
	override fun generate(world: IStructureWorld, instance: Instance) {
		super.generate(world, instance)
		
		val rand = world.rand
		
		// Cobwebs
		
		var remainingCobwebs = rand.nextInt(6, 12)
		
		for (attempt in 1..18) {
			val cobwebSide = rand.nextItem(Facing4)
			
			val cobwebPos = Pos(centerX, maxY - rand.nextInt(1, 2), centerZ)
				.offset(cobwebSide, 1 + rand.nextInt(2 + rand.nextInt(3)))
				.offset(if (rand.nextBoolean()) cobwebSide.rotateY() else cobwebSide.rotateYCCW(), rand.nextInt(0, 2))
			
			if (world.isAir(cobwebPos)) {
				world.setBlock(cobwebPos, ModBlocks.ANCIENT_COBWEB)
				
				if (--remainingCobwebs == 0) {
					break
				}
			}
		}
		
		// Corners
		
		val cornerContents = generateCorners(rand)
		
		for (facing in Facing4) {
			val cornerPos = Pos(centerX, 3, centerZ).offset(facing.rotateYCCW(), 4).offset(facing, 4)
			val adjacentPos1 = cornerPos.offset(facing.opposite)
			val adjacentFacing1 = facing.rotateY()
			val adjacentPos2 = cornerPos.offset(facing.rotateY())
			val adjacentFacing2 = facing.opposite
			
			val availablePositions = arrayOf(cornerPos, adjacentPos1, adjacentPos2)
			val availableAdjacent = arrayOf(Pair(adjacentPos1, adjacentFacing1), Pair(adjacentPos2, adjacentFacing2))
			
			when (rand.removeItem(cornerContents)) {
				ONE_FLOWER_POT -> {
					placeFlowerPot(world, rand.nextItem(availablePositions))
				}
				
				TWO_FLOWER_POTS -> {
					placeFlowerPot(world, adjacentPos1)
					placeFlowerPot(world, adjacentPos2)
				}
				
				ONE_CHEST -> {
					val (chestPos, chestFacing) = rand.nextItem(availableAdjacent)
					placeChest(world, chestPos, chestFacing)
				}
				
				TWO_CHESTS -> {
					placeChest(world, adjacentPos1, adjacentFacing1)
					placeChest(world, adjacentPos2, adjacentFacing2)
				}
				
				CHEST_AND_FLOWER_POT -> {
					val (chestPos, chestFacing) = rand.nextItem(availableAdjacent)
					placeChest(world, chestPos, chestFacing)
					
					for (attempt in 1..3) {
						val potPos = rand.nextItem(availablePositions)
						
						if (world.isAir(potPos)) {
							placeFlowerPot(world, potPos)
							break
						}
					}
				}
				
				JUST_COBWEBS -> {}
			}
			
			repeat(2) {
				if (rand.nextInt(4) == 0) {
					val cobwebPos = rand.nextItem(availablePositions)
					
					if (world.isAir(cobwebPos)) {
						world.setBlock(cobwebPos, ModBlocks.ANCIENT_COBWEB)
					}
				}
			}
		}
	}
	
	private fun placeFlowerPot(world: IStructureWorld, pos: BlockPos) {
		world.setBlock(pos, world.rand.nextItem(FLOWER_POT_TYPES))
	}
	
	private fun placeChest(world: IStructureWorld, pos: BlockPos, facing: Direction) {
		world.setState(pos, Blocks.CHEST.withFacing(facing))
		world.addTrigger(pos, LootChestStructureTrigger(StrongholdPieces.LOOT_GENERIC, world.rand.nextLong()))
	}
	
	private enum class CornerContent {
		JUST_COBWEBS, ONE_FLOWER_POT, TWO_FLOWER_POTS, ONE_CHEST, TWO_CHESTS, CHEST_AND_FLOWER_POT
	}
	
	private companion object {
		private val FLOWER_POT_TYPES = arrayOf(
			Blocks.POTTED_BLUE_ORCHID,
			Blocks.POTTED_AZURE_BLUET,
			Blocks.POTTED_ALLIUM,
			Blocks.POTTED_POPPY,
			Blocks.POTTED_DANDELION,
			Blocks.POTTED_FERN,
			Blocks.POTTED_CACTUS
		)
		
		private fun generateCorners(rand: Random): MutableList<CornerContent> {
			val content = mutableListOf(
				ONE_FLOWER_POT
			)
			
			if (rand.nextInt(8) == 0) {
				content.add(TWO_CHESTS)
			}
			else {
				if (rand.nextBoolean()) {
					content.add(ONE_CHEST)
				}
				else {
					content.add(CHEST_AND_FLOWER_POT)
				}
				
				if (rand.nextInt(5) == 0) {
					content.add(ONE_CHEST)
				}
			}
			
			if (rand.nextInt(3) == 0) {
				content.add(TWO_FLOWER_POTS)
			}
			
			if (rand.nextBoolean()) {
				content.add(ONE_FLOWER_POT)
			}
			
			while (content.size < 4) {
				content.add(JUST_COBWEBS)
			}
			
			return content
		}
	}
}
