package chylex.hee.game.world.generation.feature.energyshrine.piece

import chylex.hee.game.mechanics.energy.IClusterGenerator.Companion.ENERGY_SHRINE
import chylex.hee.game.world.generation.feature.energyshrine.EnergyShrinePieces
import chylex.hee.game.world.generation.feature.energyshrine.connection.EnergyShrineConnection
import chylex.hee.game.world.generation.feature.energyshrine.connection.EnergyShrineConnectionType.TERMINAL
import chylex.hee.game.world.generation.structure.IStructureWorld
import chylex.hee.game.world.generation.structure.file.IStructurePieceFromFile
import chylex.hee.game.world.generation.structure.file.IStructurePieceFromFile.Delegate
import chylex.hee.game.world.generation.structure.piece.IStructurePieceConnection
import chylex.hee.game.world.generation.trigger.EnergyClusterStructureTrigger
import chylex.hee.util.math.Pos
import net.minecraft.util.Direction.EAST
import net.minecraft.util.Direction.SOUTH
import net.minecraft.util.Direction.WEST

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
