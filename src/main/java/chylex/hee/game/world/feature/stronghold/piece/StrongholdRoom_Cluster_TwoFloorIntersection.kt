package chylex.hee.game.world.feature.stronghold.piece
import chylex.hee.game.mechanics.energy.IClusterGenerator.Companion.STRONGHOLD
import chylex.hee.game.world.Pos
import chylex.hee.game.world.feature.stronghold.StrongholdPieceType
import chylex.hee.game.world.feature.stronghold.connection.StrongholdConnection
import chylex.hee.game.world.feature.stronghold.connection.StrongholdConnectionType.ROOM
import chylex.hee.game.world.structure.IStructureWorld
import chylex.hee.game.world.structure.piece.IStructurePieceConnection
import chylex.hee.game.world.structure.trigger.EnergyClusterStructureTrigger
import chylex.hee.system.migration.Facing.EAST
import chylex.hee.system.migration.Facing.NORTH
import chylex.hee.system.migration.Facing.SOUTH
import chylex.hee.system.migration.Facing.WEST

class StrongholdRoom_Cluster_TwoFloorIntersection(file: String) : StrongholdAbstractPieceFromFile(file, StrongholdPieceType.ROOM){
	override val connections = arrayOf<IStructurePieceConnection>(
		StrongholdConnection(ROOM, Pos(centerX, 0, 0), NORTH),
		StrongholdConnection(ROOM, Pos(centerX, 4, 0), NORTH),
		StrongholdConnection(ROOM, Pos(centerX, 0, maxZ), SOUTH),
		StrongholdConnection(ROOM, Pos(centerX, 4, maxZ), SOUTH),
		StrongholdConnection(ROOM, Pos(maxX, 0, centerZ), EAST),
		StrongholdConnection(ROOM, Pos(maxX, 4, centerZ), EAST),
		StrongholdConnection(ROOM, Pos(0, 0, centerZ), WEST),
		StrongholdConnection(ROOM, Pos(0, 4, centerZ), WEST)
	)
	
	override fun generate(world: IStructureWorld, instance: Instance){
		super.generate(world, instance)
		
		val rand = world.rand
		
		val clusterPos = if (rand.nextBoolean())
			Pos(centerX - 2, if (rand.nextBoolean()) 2 else 6, centerZ - 2)
		else
			Pos(centerX + 2, if (rand.nextBoolean()) 2 else 6, centerZ + 2)
		
		world.addTrigger(clusterPos, EnergyClusterStructureTrigger(STRONGHOLD.generate(world.rand)))
	}
}

