package chylex.hee.game.world.feature.stronghold.piece
import chylex.hee.game.world.feature.stronghold.StrongholdPieceType
import chylex.hee.game.world.feature.stronghold.StrongholdPieces
import chylex.hee.game.world.feature.stronghold.connection.StrongholdConnection
import chylex.hee.game.world.feature.stronghold.connection.StrongholdConnectionType.DEAD_END
import chylex.hee.game.world.structure.IStructureWorld
import chylex.hee.game.world.structure.piece.IStructurePieceConnection
import chylex.hee.game.world.structure.trigger.LootChestStructureTrigger
import chylex.hee.system.util.Pos
import net.minecraft.util.EnumFacing.SOUTH

class StrongholdRoom_DeadEnd_Waterfalls(file: String) : StrongholdAbstractPieceFromFile(file, StrongholdPieceType.OTHER){
	override val connections = arrayOf<IStructurePieceConnection>(
		StrongholdConnection(DEAD_END, Pos(centerX, 1, maxZ), SOUTH)
	)
	
	override fun generate(world: IStructureWorld, instance: Instance){
		super.generate(world, instance)
		
		val rand = world.rand
		
		world.addTrigger(Pos(centerX, 3, 1), LootChestStructureTrigger(StrongholdPieces.LOOT_GENERIC, rand.nextLong()))
		world.addTrigger(Pos(centerX, 1, 1), LootChestStructureTrigger(StrongholdPieces.LOOT_GENERIC, rand.nextLong()))
	}
}
