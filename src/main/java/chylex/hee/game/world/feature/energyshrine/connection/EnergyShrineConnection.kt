package chylex.hee.game.world.feature.energyshrine.connection
import chylex.hee.game.world.structure.piece.IStructurePieceConnection
import chylex.hee.game.world.structure.piece.IStructurePieceConnection.AlignmentType.EVEN
import net.minecraft.util.EnumFacing
import net.minecraft.util.math.BlockPos

class EnergyShrineConnection(override val type: EnergyShrineConnectionType, override val offset: BlockPos, override val facing: EnumFacing) : IStructurePieceConnection{
	override val alignment = EVEN
}
