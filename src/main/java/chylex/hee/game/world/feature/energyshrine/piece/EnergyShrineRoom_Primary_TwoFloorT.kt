package chylex.hee.game.world.feature.energyshrine.piece

import chylex.hee.game.block.withFacing
import chylex.hee.game.world.Pos
import chylex.hee.game.world.feature.energyshrine.EnergyShrinePieces
import chylex.hee.game.world.feature.energyshrine.connection.EnergyShrineConnection
import chylex.hee.game.world.feature.energyshrine.connection.EnergyShrineConnectionType.ROOM
import chylex.hee.game.world.structure.IStructureWorld
import chylex.hee.game.world.structure.piece.IStructurePieceConnection
import chylex.hee.game.world.structure.trigger.LootChestStructureTrigger
import chylex.hee.init.ModBlocks
import chylex.hee.system.migration.Facing.EAST
import chylex.hee.system.migration.Facing.NORTH
import chylex.hee.system.migration.Facing.SOUTH
import chylex.hee.system.migration.Facing.WEST

class EnergyShrineRoom_Primary_TwoFloorT(file: String) : EnergyShrineRoom_Generic(file) {
	override val connections = arrayOf<IStructurePieceConnection>(
		EnergyShrineConnection(ROOM, Pos(centerX, 0, maxZ), SOUTH),
		EnergyShrineConnection(ROOM, Pos(maxX, 5, 2), EAST),
		EnergyShrineConnection(ROOM, Pos(0, 5, 3), WEST)
	)
	
	override fun generate(world: IStructureWorld, instance: Instance) {
		super.generate(world, instance)
		
		val cornerBlock = getContext(instance).cornerBlock
		world.setBlock(Pos(2, 0, 1), cornerBlock)
		world.setBlock(Pos(maxX - 2, 0, 1), cornerBlock)
		world.setBlock(Pos(1, 5, 1), cornerBlock)
		world.setBlock(Pos(maxX - 1, 5, 1), cornerBlock)
		
		placeWallBanner(world, instance, Pos(6, 5, maxZ - 3), EAST)
		placeWallBanner(world, instance, Pos(maxX - 6, 5, maxZ - 3), WEST)
		
		val rand = world.rand
		val chestPos = Pos(if (rand.nextBoolean()) 4 else maxX - 4, 6, maxZ - 1)
		
		world.setState(chestPos, ModBlocks.DARK_CHEST.withFacing(NORTH))
		world.addTrigger(chestPos, LootChestStructureTrigger(EnergyShrinePieces.LOOT_PICK(rand), rand.nextLong()))
	}
}
