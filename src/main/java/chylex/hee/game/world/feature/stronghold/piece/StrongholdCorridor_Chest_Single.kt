package chylex.hee.game.world.feature.stronghold.piece
import chylex.hee.game.world.Pos
import chylex.hee.game.world.feature.stronghold.StrongholdPieceType
import chylex.hee.game.world.feature.stronghold.StrongholdPieces
import chylex.hee.game.world.feature.stronghold.connection.StrongholdConnection
import chylex.hee.game.world.feature.stronghold.connection.StrongholdConnectionType.CORRIDOR
import chylex.hee.game.world.structure.IStructureWorld
import chylex.hee.game.world.structure.piece.IStructurePieceConnection
import chylex.hee.game.world.structure.trigger.LootChestStructureTrigger
import chylex.hee.system.migration.Facing.EAST
import chylex.hee.system.migration.Facing.WEST

class StrongholdCorridor_Chest_Single(file: String) : StrongholdAbstractPieceFromFile(file, StrongholdPieceType.CORRIDOR){
	override val connections = arrayOf<IStructurePieceConnection>(
		StrongholdConnection(CORRIDOR, Pos(maxX, 0, centerZ), EAST),
		StrongholdConnection(CORRIDOR, Pos(0, 0, centerZ), WEST)
	)
	
	override fun generate(world: IStructureWorld, instance: Instance){
		super.generate(world, instance)
		world.addTrigger(Pos(centerX, 2, centerZ - 2), LootChestStructureTrigger(StrongholdPieces.LOOT_GENERIC, world.rand.nextLong()))
	}
}
