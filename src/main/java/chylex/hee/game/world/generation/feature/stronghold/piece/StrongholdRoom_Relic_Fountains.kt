package chylex.hee.game.world.generation.feature.stronghold.piece

import chylex.hee.game.world.generation.feature.stronghold.connection.StrongholdConnection
import chylex.hee.game.world.generation.feature.stronghold.connection.StrongholdConnectionType.ROOM
import chylex.hee.game.world.generation.structure.piece.IStructurePieceConnection
import chylex.hee.util.math.Pos
import net.minecraft.item.ItemStack
import net.minecraft.util.Direction.SOUTH

class StrongholdRoom_Relic_Fountains(file: String, relicItem: ItemStack) : StrongholdRoom_Relic(file, relicItem) {
	override val connections = arrayOf<IStructurePieceConnection>(
		StrongholdConnection(ROOM, Pos(centerX, 1, maxZ), SOUTH)
	)
	
	override val lootChestPos = Pos(centerX, 3, 1)
}
