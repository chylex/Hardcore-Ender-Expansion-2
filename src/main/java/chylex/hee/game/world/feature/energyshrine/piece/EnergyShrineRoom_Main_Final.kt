package chylex.hee.game.world.feature.energyshrine.piece

import chylex.hee.game.mechanics.energy.IClusterGenerator.Companion.ENERGY_SHRINE
import chylex.hee.game.world.Pos
import chylex.hee.game.world.feature.energyshrine.EnergyShrinePieces
import chylex.hee.game.world.feature.energyshrine.connection.EnergyShrineConnection
import chylex.hee.game.world.feature.energyshrine.connection.EnergyShrineConnectionType.TERMINAL
import chylex.hee.game.world.structure.IStructurePieceFromFile
import chylex.hee.game.world.structure.IStructurePieceFromFile.Delegate
import chylex.hee.game.world.structure.IStructureWorld
import chylex.hee.game.world.structure.piece.IStructurePieceConnection
import chylex.hee.game.world.structure.trigger.EnergyClusterStructureTrigger
import chylex.hee.system.migration.Facing.EAST
import chylex.hee.system.migration.Facing.SOUTH
import chylex.hee.system.migration.Facing.WEST

class EnergyShrineRoom_Main_Final(file: String) : EnergyShrineAbstractPiece(), IStructurePieceFromFile by Delegate("energyshrine/$file", EnergyShrinePieces.PALETTE) {
	override val connections = arrayOf<IStructurePieceConnection>(
		EnergyShrineConnection(TERMINAL, Pos(2, 0, maxZ), SOUTH),
		EnergyShrineConnection(TERMINAL, Pos(maxX - 1, 0, maxZ), SOUTH),
		EnergyShrineConnection(TERMINAL, Pos(maxX, 0, maxZ - 2), EAST),
		EnergyShrineConnection(TERMINAL, Pos(0, 0, maxZ - 1), WEST)
	)
	
	override fun generate(world: IStructureWorld, instance: Instance) {
		super.generate(world, instance)
		generator.generate(world)
		
		world.addTrigger(Pos(centerX, 2, 3), EnergyClusterStructureTrigger(ENERGY_SHRINE.generate(world.rand)))
	}
}
