package chylex.hee.game.world.feature.stronghold.piece
import chylex.hee.game.world.Pos
import chylex.hee.game.world.feature.stronghold.StrongholdPieceType
import chylex.hee.game.world.feature.stronghold.StrongholdPieces
import chylex.hee.game.world.feature.stronghold.connection.StrongholdConnection
import chylex.hee.game.world.feature.stronghold.connection.StrongholdConnectionType.ROOM
import chylex.hee.game.world.structure.IStructureWorld
import chylex.hee.game.world.structure.piece.IStructurePieceConnection
import chylex.hee.game.world.structure.trigger.LootChestStructureTrigger
import chylex.hee.system.migration.Facing.EAST
import chylex.hee.system.migration.Facing.NORTH
import chylex.hee.system.migration.Facing.SOUTH
import chylex.hee.system.migration.Facing.WEST

class StrongholdRoom_Chest_Pool_DrainedOrEmbedded(file: String) : StrongholdAbstractPieceFromFile(file, StrongholdPieceType.ROOM){
	override val connections = arrayOf<IStructurePieceConnection>(
		StrongholdConnection(ROOM, Pos(centerX, 1, 0), NORTH),
		StrongholdConnection(ROOM, Pos(centerX, 1, maxZ), SOUTH),
		StrongholdConnection(ROOM, Pos(maxX, 1, centerZ), EAST),
		StrongholdConnection(ROOM, Pos(0, 1, centerZ), WEST)
	)
	
	override fun generate(world: IStructureWorld, instance: Instance){
		super.generate(world, instance)
		world.addTrigger(Pos(centerX, 2, centerZ), LootChestStructureTrigger(StrongholdPieces.LOOT_GENERIC, world.rand.nextLong()))
	}
}
