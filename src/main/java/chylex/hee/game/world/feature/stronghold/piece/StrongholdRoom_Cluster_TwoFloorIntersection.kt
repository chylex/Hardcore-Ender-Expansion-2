package chylex.hee.game.world.feature.stronghold.piece
import chylex.hee.game.mechanics.energy.IClusterGenerator.Companion.STRONGHOLD
import chylex.hee.game.world.feature.stronghold.connection.StrongholdRoomConnection
import chylex.hee.game.world.structure.IStructureWorld
import chylex.hee.game.world.structure.piece.IStructurePieceConnection
import chylex.hee.game.world.structure.trigger.EnergyClusterStructureTrigger
import chylex.hee.system.util.Pos
import net.minecraft.util.EnumFacing.EAST
import net.minecraft.util.EnumFacing.NORTH
import net.minecraft.util.EnumFacing.SOUTH
import net.minecraft.util.EnumFacing.WEST

class StrongholdRoom_Cluster_TwoFloorIntersection(file: String) : StrongholdAbstractPieceFromFile(file){
	override val connections = arrayOf<IStructurePieceConnection>(
		StrongholdRoomConnection(Pos(centerX, 0, 0), NORTH),
		StrongholdRoomConnection(Pos(centerX, 4, 0), NORTH),
		StrongholdRoomConnection(Pos(centerX, 0, maxZ), SOUTH),
		StrongholdRoomConnection(Pos(centerX, 4, maxZ), SOUTH),
		StrongholdRoomConnection(Pos(maxX, 0, centerZ), EAST),
		StrongholdRoomConnection(Pos(maxX, 4, centerZ), EAST),
		StrongholdRoomConnection(Pos(0, 0, centerZ), WEST),
		StrongholdRoomConnection(Pos(0, 4, centerZ), WEST)
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

