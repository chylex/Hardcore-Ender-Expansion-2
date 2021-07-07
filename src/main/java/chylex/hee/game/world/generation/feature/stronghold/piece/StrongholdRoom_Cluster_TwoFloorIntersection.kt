package chylex.hee.game.world.generation.feature.stronghold.piece

import chylex.hee.game.mechanics.energy.IClusterGenerator.Companion.STRONGHOLD
import chylex.hee.game.world.generation.feature.stronghold.StrongholdPieceType
import chylex.hee.game.world.generation.feature.stronghold.connection.StrongholdConnection
import chylex.hee.game.world.generation.feature.stronghold.connection.StrongholdConnectionType.ROOM
import chylex.hee.game.world.generation.structure.IStructureWorld
import chylex.hee.game.world.generation.structure.piece.IStructurePieceConnection
import chylex.hee.game.world.generation.trigger.EnergyClusterStructureTrigger
import chylex.hee.util.math.Pos
import net.minecraft.util.Direction.EAST
import net.minecraft.util.Direction.NORTH
import net.minecraft.util.Direction.SOUTH
import net.minecraft.util.Direction.WEST

class StrongholdRoom_Cluster_TwoFloorIntersection(file: String) : StrongholdAbstractPieceFromFile(file, StrongholdPieceType.ROOM) {
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
	
	override fun generate(world: IStructureWorld, instance: Instance) {
		super.generate(world, instance)
		
		val rand = world.rand
		
		val clusterPos = if (rand.nextBoolean())
			Pos(centerX - 2, if (rand.nextBoolean()) 2 else 6, centerZ - 2)
		else
			Pos(centerX + 2, if (rand.nextBoolean()) 2 else 6, centerZ + 2)
		
		world.addTrigger(clusterPos, EnergyClusterStructureTrigger(STRONGHOLD.generate(world.rand)))
	}
}

