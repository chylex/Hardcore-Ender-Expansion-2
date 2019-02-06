package chylex.hee.game.world.feature.stronghold.piece
import chylex.hee.game.mechanics.energy.IClusterGenerator.Companion.STRONGHOLD
import chylex.hee.game.world.structure.IStructureWorld
import chylex.hee.game.world.structure.trigger.EnergyClusterStructureTrigger
import chylex.hee.system.util.Pos

class StrongholdRoom_Cluster_Pillar(file: String) : StrongholdAbstractPieceFromFile(file){
	override fun generate(world: IStructureWorld, instance: Instance){
		super.generate(world, instance)
		world.addTrigger(Pos(centerX, 2, centerZ), EnergyClusterStructureTrigger(STRONGHOLD.generate(world.rand)))
	}
}
