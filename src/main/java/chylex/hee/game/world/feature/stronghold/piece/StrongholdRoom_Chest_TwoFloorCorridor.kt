package chylex.hee.game.world.feature.stronghold.piece
import chylex.hee.game.world.feature.stronghold.StrongholdPieceType.ROOM
import chylex.hee.game.world.feature.stronghold.StrongholdPieces
import chylex.hee.game.world.feature.stronghold.connection.StrongholdRoomConnection
import chylex.hee.game.world.structure.IStructureWorld
import chylex.hee.game.world.structure.piece.IStructurePieceConnection
import chylex.hee.game.world.structure.trigger.LootChestStructureTrigger
import chylex.hee.system.util.Pos
import net.minecraft.util.EnumFacing.EAST
import net.minecraft.util.EnumFacing.WEST

class StrongholdRoom_Chest_TwoFloorCorridor(file: String) : StrongholdAbstractPieceFromFile(file, ROOM){
	override val connections = arrayOf<IStructurePieceConnection>(
		StrongholdRoomConnection(Pos(maxX, 0, centerZ), EAST),
		StrongholdRoomConnection(Pos(0, 0, centerZ), WEST)
	)
	
	override fun generate(world: IStructureWorld, instance: Instance){
		super.generate(world, instance)
		world.addTrigger(Pos(centerX, 6, centerZ), LootChestStructureTrigger(StrongholdPieces.LOOT_GENERIC, world.rand.nextLong()))
	}
}
