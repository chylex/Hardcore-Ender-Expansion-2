package chylex.hee.game.world.feature.energyshrine.piece
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
import chylex.hee.system.util.Pos
import chylex.hee.system.util.withFacing

class EnergyShrineRoom_Primary_TwoFloorOverhang(file: String) : EnergyShrineRoom_Generic(file){
	override val connections = arrayOf<IStructurePieceConnection>(
		EnergyShrineConnection(ROOM, Pos(2, 0, maxZ), SOUTH),
		EnergyShrineConnection(ROOM, Pos(maxX, maxY - 5, maxZ - 2), EAST),
		EnergyShrineConnection(ROOM, Pos(0, maxY - 5, maxZ - 1), WEST)
	)
	
	override fun generate(world: IStructureWorld, instance: Instance){
		super.generate(world, instance)
		
		placeWallBanner(world, instance, Pos(maxX - 3, maxY - 5, maxZ - 4), NORTH)
		placeWallBanner(world, instance, Pos(maxX - 7, maxY - 5, maxZ - 4), NORTH)
		
		val rand = world.rand
		
		world.setState(Pos(3, 2, 5), ModBlocks.DARK_CHEST.withFacing(EAST))
		world.addTrigger(Pos(3, 2, 5), LootChestStructureTrigger(EnergyShrinePieces.LOOT_PICK(rand), rand.nextLong()))
	}
}
