package chylex.hee.game.world.generation.feature.stronghold.piece

import chylex.hee.game.block.util.withFacing
import chylex.hee.game.world.generation.feature.stronghold.StrongholdPieceType
import chylex.hee.game.world.generation.feature.stronghold.StrongholdPieces
import chylex.hee.game.world.generation.feature.stronghold.connection.StrongholdConnection
import chylex.hee.game.world.generation.feature.stronghold.connection.StrongholdConnectionType.ROOM
import chylex.hee.game.world.generation.structure.IStructureWorld
import chylex.hee.game.world.generation.structure.piece.IStructurePieceConnection
import chylex.hee.game.world.generation.trigger.LootChestStructureTrigger
import chylex.hee.game.world.util.Facing4
import chylex.hee.game.world.util.offsetUntil
import chylex.hee.init.ModBlocks
import chylex.hee.util.math.Pos
import chylex.hee.util.random.nextInt
import net.minecraft.block.Blocks
import net.minecraft.util.Direction.EAST
import net.minecraft.util.Direction.NORTH
import net.minecraft.util.Direction.SOUTH
import net.minecraft.util.Direction.UP
import net.minecraft.util.Direction.WEST

class StrongholdRoom_Main_Scriptorium(file: String) : StrongholdAbstractPieceFromFile(file, StrongholdPieceType.ROOM) {
	override val connections = arrayOf<IStructurePieceConnection>(
		StrongholdConnection(ROOM, Pos(centerX, 0, maxZ), SOUTH),
		StrongholdConnection(ROOM, Pos(maxX, 0, centerZ), EAST),
		StrongholdConnection(ROOM, Pos(0, 0, centerZ), WEST)
	)
	
	override fun generate(world: IStructureWorld, instance: Instance) {
		super.generate(world, instance)
		
		val rand = world.rand
		
		// Table
		
		world.setBlock(Pos(centerX, 2, 1), Blocks.POTTED_DEAD_BUSH)
		
		// Chest
		
		val chestPos = Pos(centerX + (if (rand.nextBoolean()) -4 else 4), 2, centerZ - 3)
		
		world.setState(chestPos, Blocks.CHEST.withFacing(NORTH))
		world.addTrigger(chestPos, LootChestStructureTrigger(StrongholdPieces.LOOT_GENERIC, rand.nextLong()))
		
		// Cobwebs
		
		repeat(rand.nextInt(7, 12)) {
			for (attempt in 1..50) {
				val testPos = Pos(rand.nextInt(1, maxX - 1), 1, rand.nextInt(1, maxZ - 1)).offsetUntil(UP, 0 until maxY, world::isAir)
				
				if (testPos == null || (testPos.y == 1 && rand.nextInt(4) != 0)) {
					continue
				}
				
				val isInAir = testPos.y > 1
				
				if (world.isAir(testPos) && (isInAir || Facing4.any { !world.isAir(testPos.offset(it)) })) {
					val below = world.getBlock(testPos.down())
					
					if (below === Blocks.BOOKSHELF || below === Blocks.STONE_BRICK_SLAB || StrongholdPieces.isStoneBrick(below)) {
						if (isInAir && rand.nextInt(4) == 0 && world.isAir(testPos.up())) {
							world.setBlock(testPos.up(), ModBlocks.ANCIENT_COBWEB)
						}
						else {
							world.setBlock(testPos, ModBlocks.ANCIENT_COBWEB)
						}
						
						break
					}
				}
			}
		}
	}
}
