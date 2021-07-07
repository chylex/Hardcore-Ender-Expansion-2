package chylex.hee.game.world.generation.feature.stronghold.piece

import chylex.hee.game.world.generation.feature.stronghold.StrongholdPieceType
import chylex.hee.game.world.generation.feature.stronghold.StrongholdPieces
import chylex.hee.game.world.generation.feature.stronghold.connection.StrongholdConnection
import chylex.hee.game.world.generation.feature.stronghold.connection.StrongholdConnectionType.CORRIDOR
import chylex.hee.game.world.generation.structure.IStructureWorld
import chylex.hee.game.world.generation.structure.piece.IStructurePieceConnection
import chylex.hee.game.world.generation.trigger.LootChestStructureTrigger
import chylex.hee.util.math.Pos
import net.minecraft.util.Direction.EAST
import net.minecraft.util.Direction.WEST

class StrongholdCorridor_Chest_Single(file: String) : StrongholdAbstractPieceFromFile(file, StrongholdPieceType.CORRIDOR) {
	override val connections = arrayOf<IStructurePieceConnection>(
		StrongholdConnection(CORRIDOR, Pos(maxX, 0, centerZ), EAST),
		StrongholdConnection(CORRIDOR, Pos(0, 0, centerZ), WEST)
	)
	
	override fun generate(world: IStructureWorld, instance: Instance) {
		super.generate(world, instance)
		world.addTrigger(Pos(centerX, 2, centerZ - 2), LootChestStructureTrigger(StrongholdPieces.LOOT_GENERIC, world.rand.nextLong()))
	}
}
