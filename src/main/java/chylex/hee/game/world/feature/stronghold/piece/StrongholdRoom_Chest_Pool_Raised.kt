package chylex.hee.game.world.feature.stronghold.piece

import chylex.hee.game.world.Pos
import chylex.hee.game.world.feature.stronghold.StrongholdPieceType
import chylex.hee.game.world.feature.stronghold.StrongholdPieces
import chylex.hee.game.world.structure.IStructureWorld
import chylex.hee.game.world.structure.trigger.LootChestStructureTrigger

class StrongholdRoom_Chest_Pool_Raised(file: String) : StrongholdAbstractPieceFromFile(file, StrongholdPieceType.ROOM) {
	override fun generate(world: IStructureWorld, instance: Instance) {
		super.generate(world, instance)
		world.addTrigger(Pos(centerX, 2, centerZ), LootChestStructureTrigger(StrongholdPieces.LOOT_GENERIC, world.rand.nextLong()))
	}
}
