package chylex.hee.game.world.generation.feature.stronghold.piece

import chylex.hee.game.world.generation.feature.stronghold.StrongholdPieceType
import chylex.hee.game.world.generation.feature.stronghold.StrongholdPieces
import chylex.hee.game.world.generation.feature.stronghold.connection.StrongholdConnection
import chylex.hee.game.world.generation.feature.stronghold.connection.StrongholdConnectionType.DEAD_END
import chylex.hee.game.world.generation.structure.IStructureWorld
import chylex.hee.game.world.generation.structure.piece.IStructurePieceConnection
import chylex.hee.game.world.generation.trigger.LootChestStructureTrigger
import chylex.hee.util.math.Pos
import net.minecraft.util.Direction.SOUTH

class StrongholdRoom_DeadEnd_Waterfalls(file: String) : StrongholdAbstractPieceFromFile(file, StrongholdPieceType.OTHER) {
	override val connections = arrayOf<IStructurePieceConnection>(
		StrongholdConnection(DEAD_END, Pos(centerX, 1, maxZ), SOUTH)
	)
	
	override fun generate(world: IStructureWorld, instance: Instance) {
		super.generate(world, instance)
		
		val rand = world.rand
		
		world.addTrigger(Pos(centerX, 3, 1), LootChestStructureTrigger(StrongholdPieces.LOOT_GENERIC, rand.nextLong()))
		world.addTrigger(Pos(centerX, 1, 1), LootChestStructureTrigger(StrongholdPieces.LOOT_GENERIC, rand.nextLong()))
	}
}
