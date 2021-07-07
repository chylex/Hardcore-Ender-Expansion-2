package chylex.hee.game.world.generation.feature.stronghold.piece

import chylex.hee.game.mechanics.energy.IClusterGenerator.Companion.STRONGHOLD
import chylex.hee.game.world.generation.feature.stronghold.StrongholdPieceType
import chylex.hee.game.world.generation.structure.IStructureWorld
import chylex.hee.game.world.generation.trigger.EnergyClusterStructureTrigger
import chylex.hee.util.math.Pos

class StrongholdRoom_Cluster_Floating(file: String) : StrongholdAbstractPieceFromFile(file, StrongholdPieceType.ROOM) {
	override fun generate(world: IStructureWorld, instance: Instance) {
		super.generate(world, instance)
		world.addTrigger(Pos(centerX, 3, centerZ), EnergyClusterStructureTrigger(STRONGHOLD.generate(world.rand)))
	}
}

