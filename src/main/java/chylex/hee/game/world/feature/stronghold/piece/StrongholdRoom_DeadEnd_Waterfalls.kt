package chylex.hee.game.world.feature.stronghold.piece
import chylex.hee.game.world.feature.stronghold.StrongholdPieces
import chylex.hee.game.world.feature.stronghold.connection.StrongholdDeadEndConnection
import chylex.hee.game.world.structure.IStructureWorld
import chylex.hee.game.world.structure.piece.IStructurePieceConnection
import chylex.hee.game.world.structure.trigger.LootChestStructureTrigger
import chylex.hee.system.util.Pos
import net.minecraft.util.EnumFacing.SOUTH

class StrongholdRoom_DeadEnd_Waterfalls(file: String) : StrongholdAbstractPieceFromFile(file){
	override val connections = arrayOf<IStructurePieceConnection>(
		StrongholdDeadEndConnection(Pos(centerX, 1, maxZ), SOUTH)
	)
	
	override fun generate(world: IStructureWorld, instance: Instance){
		super.generate(world, instance)
		
		val rand = world.rand
		
		world.addTrigger(Pos(centerX, 3, 1), LootChestStructureTrigger(StrongholdPieces.LOOT_GENERIC, rand.nextLong()))
		world.addTrigger(Pos(centerX, 1, 1), LootChestStructureTrigger(StrongholdPieces.LOOT_GENERIC, rand.nextLong()))
	}
}
