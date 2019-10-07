package chylex.hee.game.world.feature.stronghold.piece
import chylex.hee.game.block.util.FutureBlocks
import chylex.hee.game.world.feature.stronghold.StrongholdPieceType
import chylex.hee.game.world.feature.stronghold.StrongholdPieces
import chylex.hee.game.world.feature.stronghold.connection.StrongholdConnection
import chylex.hee.game.world.feature.stronghold.connection.StrongholdConnectionType.DEAD_END
import chylex.hee.game.world.structure.IStructureWorld
import chylex.hee.game.world.structure.piece.IStructurePieceConnection
import chylex.hee.game.world.structure.trigger.FlowerPotStructureTrigger
import chylex.hee.game.world.structure.trigger.LootChestStructureTrigger
import chylex.hee.game.world.structure.trigger.TileEntityStructureTrigger
import chylex.hee.system.migration.Facing.EAST
import chylex.hee.system.migration.Facing.SOUTH
import chylex.hee.system.migration.Facing.WEST
import chylex.hee.system.migration.vanilla.Blocks
import chylex.hee.system.util.Pos
import chylex.hee.system.util.nextItem
import chylex.hee.system.util.withFacing
import net.minecraft.tileentity.TileEntitySkull

class StrongholdRoom_DeadEnd_Shelves(file: String) : StrongholdAbstractPieceFromFile(file, StrongholdPieceType.OTHER){
	override val connections = arrayOf<IStructurePieceConnection>(
		StrongholdConnection(DEAD_END, Pos(centerX, 0, maxZ), SOUTH)
	)
	
	override fun generate(world: IStructureWorld, instance: Instance){
		super.generate(world, instance)
		
		val rand = world.rand
		
		// chest
		
		val chestSide = if (rand.nextBoolean()) EAST else WEST
		val chestOffset = rand.nextItem(intArrayOf(2, 4, 5), 0)
		val chestPos = Pos(centerX + (chestSide.opposite.xOffset * 2), 2, chestOffset)
		
		world.setState(chestPos, Blocks.CHEST.withFacing(chestSide))
		world.addTrigger(chestPos, LootChestStructureTrigger(StrongholdPieces.LOOT_GENERIC, rand.nextLong()))
		
		// skull
		
		val skullSide = if (chestOffset == 2 || rand.nextBoolean()) chestSide.opposite else chestSide
		val skullType = if (rand.nextBoolean()) 0 else 2
		val skullRot = if (skullSide == EAST) 6 else 10
		val skullPos = Pos(centerX + (skullSide.opposite.xOffset * 2), 2, 1)
		
		world.addTrigger(skullPos, TileEntityStructureTrigger(FutureBlocks.SKULL_FLOOR, TileEntitySkull().apply { setType(skullType); skullRotation = skullRot }))
		
		// flower pots
		
		val flowerTypes = arrayOf(
			FutureBlocks.ORANGE_TULIP_STACK,
			FutureBlocks.PINK_TULIP_STACK,
			FutureBlocks.POPPY_STACK,
			FutureBlocks.DANDELION_STACK
		)
		
		repeat(rand.nextInt(3)){
			for(attempt in 1..2){
				val potSide = if (rand.nextBoolean()) EAST else WEST
				
				val potPos = when(rand.nextInt(3)){
					0 -> Pos(centerX + (potSide.xOffset * 2), 2, 4)
					1 -> Pos(centerX + (potSide.xOffset * 2), 2, 2)
					else -> Pos(centerX + potSide.xOffset, 2, 1)
				}
				
				if (world.isAir(potPos)){
					world.addTrigger(potPos, FlowerPotStructureTrigger(rand.nextItem(flowerTypes)))
					break
				}
			}
		}
	}
}
