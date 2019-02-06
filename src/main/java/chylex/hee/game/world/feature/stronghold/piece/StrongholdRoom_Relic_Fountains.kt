package chylex.hee.game.world.feature.stronghold.piece
import chylex.hee.game.world.feature.stronghold.connection.StrongholdRoomConnection
import chylex.hee.game.world.structure.piece.IStructurePieceConnection
import chylex.hee.system.util.Pos
import net.minecraft.item.ItemStack
import net.minecraft.util.EnumFacing.SOUTH

class StrongholdRoom_Relic_Fountains(file: String, relicItem: ItemStack) : StrongholdRoom_Relic(file, relicItem){
	override val connections = arrayOf<IStructurePieceConnection>(
		StrongholdRoomConnection(Pos(centerX, 1, maxZ), SOUTH)
	)
	
	override val lootChestPos = Pos(centerX, 3, 1)
}
