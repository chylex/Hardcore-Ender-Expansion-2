package chylex.hee.game.world.generation.feature.energyshrine.connection

import chylex.hee.game.world.generation.feature.energyshrine.connection.EnergyShrineConnectionType.STAIR_MIDDLE
import chylex.hee.game.world.generation.structure.piece.IStructurePieceConnection
import chylex.hee.game.world.generation.structure.piece.IStructurePieceConnection.AlignmentType.EVEN
import chylex.hee.game.world.generation.structure.piece.IStructurePieceConnection.AlignmentType.ODD
import net.minecraft.util.Direction
import net.minecraft.util.math.BlockPos

class EnergyShrineConnection(override val type: EnergyShrineConnectionType, override val offset: BlockPos, override val facing: Direction) : IStructurePieceConnection {
	override val alignment = if (type == STAIR_MIDDLE) ODD else EVEN
}
