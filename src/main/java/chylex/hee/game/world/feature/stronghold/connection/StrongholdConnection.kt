package chylex.hee.game.world.feature.stronghold.connection

import chylex.hee.game.world.structure.piece.IStructurePieceConnection
import chylex.hee.game.world.structure.piece.IStructurePieceConnection.AlignmentType.ODD
import net.minecraft.util.Direction
import net.minecraft.util.math.BlockPos

class StrongholdConnection(override val type: StrongholdConnectionType, override val offset: BlockPos, override val facing: Direction) : IStructurePieceConnection {
	override val alignment = ODD
}
