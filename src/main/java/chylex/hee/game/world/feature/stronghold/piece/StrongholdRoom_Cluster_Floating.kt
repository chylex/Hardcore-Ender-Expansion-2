package chylex.hee.game.world.feature.stronghold.piece
import chylex.hee.game.mechanics.energy.IClusterGenerator.Companion.STRONGHOLD
import chylex.hee.game.world.feature.stronghold.StrongholdPieceType.ROOM
import chylex.hee.game.world.structure.IStructureWorld
import chylex.hee.game.world.structure.trigger.EnergyClusterStructureTrigger
import chylex.hee.system.util.Pos

class StrongholdRoom_Cluster_Floating(file: String) : StrongholdAbstractPieceFromFile(file, ROOM){
	override fun generate(world: IStructureWorld, instance: Instance){
		super.generate(world, instance)
		world.addTrigger(Pos(centerX, 3, centerZ), EnergyClusterStructureTrigger(STRONGHOLD.generate(world.rand)))
	}
}

