package chylex.hee.game.world.generation.feature.stronghold.piece

import chylex.hee.game.block.util.withFacing
import chylex.hee.game.world.generation.IBlockPicker.Single
import chylex.hee.game.world.generation.feature.stronghold.StrongholdPieceType
import chylex.hee.game.world.generation.feature.stronghold.StrongholdPieces
import chylex.hee.game.world.generation.feature.stronghold.connection.StrongholdConnection
import chylex.hee.game.world.generation.feature.stronghold.connection.StrongholdConnectionType.ROOM
import chylex.hee.game.world.generation.feature.stronghold.piece.StrongholdRoom_Main_Library.FirstFloorChestPosition.INSIDE_BOOKSHELVES
import chylex.hee.game.world.generation.feature.stronghold.piece.StrongholdRoom_Main_Library.FirstFloorChestPosition.WORK_TABLE
import chylex.hee.game.world.generation.structure.IStructureWorld
import chylex.hee.game.world.generation.structure.piece.IStructurePieceConnection
import chylex.hee.game.world.generation.trigger.LootChestStructureTrigger
import chylex.hee.game.world.util.Facing4
import chylex.hee.system.random.nextItem
import chylex.hee.util.math.Pos
import net.minecraft.block.Blocks
import net.minecraft.util.Direction.EAST
import net.minecraft.util.Direction.NORTH
import net.minecraft.util.Direction.SOUTH
import net.minecraft.util.Direction.WEST

class StrongholdRoom_Main_Library(file: String) : StrongholdAbstractPieceFromFile(file, StrongholdPieceType.ROOM) {
	override val extraWeightMultiplier = 4
	
	override val connections = arrayOf<IStructurePieceConnection>(
		StrongholdConnection(ROOM, Pos(centerX, 6, 0), NORTH),
		StrongholdConnection(ROOM, Pos(centerX, 6, maxZ), SOUTH),
		StrongholdConnection(ROOM, Pos(maxX, 0, centerZ), EAST),
		StrongholdConnection(ROOM, Pos(maxX, 11, centerZ), EAST),
		StrongholdConnection(ROOM, Pos(0, 0, centerZ), WEST),
		StrongholdConnection(ROOM, Pos(0, 11, centerZ), WEST)
	)
	
	override fun generate(world: IStructureWorld, instance: Instance) {
		super.generate(world, instance)
		
		placeFirstFloorDecorations(world)
		placeSecondFloorDecorations(world)
		placeThirdFloorDecorations(world)
	}
	
	private fun placeFirstFloorDecorations(world: IStructureWorld) {
		val rand = world.rand
		val chestPosition = rand.nextItem<FirstFloorChestPosition>()
		
		// Center
		
		val centerFillHoles = rand.nextBoolean()
		val centerExtraBookshelves = rand.nextBoolean()
		
		if (centerFillHoles || centerExtraBookshelves) {
			for (facing in Facing4) {
				if (centerFillHoles) {
					world.setBlock(Pos(centerX, 2, centerZ).offset(facing), Blocks.BOOKSHELF)
				}
				
				if (centerExtraBookshelves) {
					val bookshelfPos = Pos(centerX, 1, centerZ).offset(facing).offset(facing.rotateY())
					world.setBlock(bookshelfPos, Blocks.BOOKSHELF)
					
					if (centerFillHoles) {
						world.setBlock(bookshelfPos.up(), Blocks.SPRUCE_SLAB)
					}
				}
			}
		}
		
		// Group table
		
		for (x in 5..6) for (z in 5..6) {
			if (rand.nextInt(4) == 0) {
				world.setBlock(Pos(centerX + x, 2, centerZ + z), Blocks.FLOWER_POT)
			}
		}
		
		// Work table
		
		if (chestPosition == WORK_TABLE) {
			val chestPos = Pos(centerX + 6, 2, centerZ - 8)
			
			world.setState(chestPos, Blocks.CHEST.withFacing(SOUTH))
			world.addTrigger(chestPos, LootChestStructureTrigger(StrongholdPieces.LOOT_LIBRARY_SECOND, rand.nextLong()))
		}
		
		val flowerPotTypes = arrayOf(
			Blocks.POTTED_WHITE_TULIP,
			Blocks.POTTED_DANDELION
		)
		
		world.setBlock(Pos(centerX + 8, 2, centerZ - 8), rand.nextItem(flowerPotTypes))
		
		// Bookshelf section
		
		if (chestPosition == INSIDE_BOOKSHELVES) {
			val chestPos = Pos(centerX - 8, 4, centerZ - 6)
			
			world.setState(chestPos, Blocks.CHEST.withFacing(NORTH))
			world.addTrigger(chestPos, LootChestStructureTrigger(StrongholdPieces.LOOT_LIBRARY_SECOND, rand.nextLong()))
		}
	}
	
	private fun placeSecondFloorDecorations(world: IStructureWorld) {
		val rand = world.rand
		
		// Center
		
		if (rand.nextBoolean()) {
			for (facing in Facing4) {
				world.setBlock(Pos(centerX, 8, centerZ).offset(facing), Blocks.BOOKSHELF)
			}
		}
		
		// Entrances
		
		for (facing in arrayOf(NORTH, SOUTH)) {
			if (rand.nextBoolean()) {
				for (xMp in intArrayOf(-1, 1)) {
					for (pos in arrayOf(
						Pos(centerX + (xMp * 2), 7, centerZ + (5 * facing.zOffset)),
						Pos(centerX + (xMp * 4), 7, centerZ + (3 * facing.zOffset))
					)) {
						world.setBlock(pos, Blocks.BOOKSHELF)
						world.setBlock(pos.up(), Blocks.SPRUCE_SLAB)
					}
					
					for (pos in arrayOf(
						Pos(centerX + (xMp * 2), 7, centerZ + (4 * facing.zOffset)),
						Pos(centerX + (xMp * 3), 7, centerZ + (3 * facing.zOffset))
					)) {
						world.setBlock(pos, Blocks.BOOKSHELF)
						world.setBlock(pos.up(), Blocks.BOOKSHELF)
						world.setBlock(pos.up(2), Blocks.SPRUCE_SLAB)
					}
				}
			}
			else {
				for (xMp in intArrayOf(-1, 1)) {
					val pos1 = Pos(centerX + (xMp * 2), 7, centerZ + (4 * facing.zOffset))
					val pos2 = Pos(centerX + (xMp * 2), 7, centerZ + (5 * facing.zOffset))
					val pos3 = Pos(centerX + (xMp * 2), 7, centerZ + (6 * facing.zOffset))
					
					world.placeCube(pos1, pos3, Single(Blocks.BOOKSHELF))
					world.placeCube(pos2.up(), pos3.up(), Single(Blocks.BOOKSHELF))
					world.setState(pos1.up(), Blocks.SPRUCE_STAIRS.withFacing(facing))
				}
			}
		}
		
		// Stairs
		
		if (rand.nextBoolean()) {
			world.setAir(Pos(centerX - 8, 9, centerZ + 3))
			world.setBlock(Pos(centerX - 8, 8, centerZ + 3), Blocks.SPRUCE_SLAB)
		}
		
		// Chest
		
		val chestFacingOffset = if (rand.nextBoolean()) EAST else WEST
		val chestFacing = if (rand.nextBoolean()) chestFacingOffset.rotateY() else chestFacingOffset.rotateYCCW()
		
		val chestPos = Pos(centerX, 8, centerZ).offset(chestFacingOffset, 8)
		val chestBackPos = chestPos.offset(chestFacing.opposite)
		
		world.setState(chestPos, Blocks.CHEST.withFacing(chestFacing))
		world.addTrigger(chestPos, LootChestStructureTrigger(StrongholdPieces.LOOT_LIBRARY_MAIN, rand.nextLong()))
		
		world.setAir(chestPos.up())
		world.placeCube(chestBackPos.down(), chestBackPos.up(), Single(Blocks.BOOKSHELF))
		world.setState(chestBackPos.up(2), Blocks.SPRUCE_STAIRS.withFacing(chestFacing))
	}
	
	private fun placeThirdFloorDecorations(world: IStructureWorld) {
		val rand = world.rand
		
		// Entrances
		
		if (rand.nextBoolean()) {
			for (xMp in intArrayOf(-1, 1)) for (zMp in intArrayOf(-1, 1)) {
				val pos1 = Pos(centerX + (4 * xMp), 12, centerZ + (2 * zMp))
				val pos2 = Pos(centerX + (5 * xMp), 12, centerZ + (2 * zMp))
				
				world.placeCube(pos1, pos2, Single(Blocks.BOOKSHELF))
				world.placeCube(pos1.up(), pos2.up(), Single(Blocks.SPRUCE_SLAB))
			}
		}
		else {
			for (xMp in intArrayOf(-1, 1)) for (zMp in intArrayOf(-1, 1)) {
				val pos1 = Pos(centerX + (6 * xMp), 12, centerZ + (2 * zMp))
				val pos2 = Pos(centerX + (7 * xMp), 12, centerZ + (2 * zMp))
				
				world.placeCube(pos1, pos2.up(), Single(Blocks.BOOKSHELF))
				world.setState(pos1.up(2), Blocks.STONE_BRICK_STAIRS.withFacing(if (xMp > 0) EAST else WEST))
				world.setBlock(pos2.up(2), Blocks.STONE_BRICKS)
			}
		}
	}
	
	private enum class FirstFloorChestPosition {
		WORK_TABLE, INSIDE_BOOKSHELVES
	}
}
