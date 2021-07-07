package chylex.hee.game.world.generation.feature.tombdungeon.connection

import chylex.hee.game.world.generation.structure.piece.IStructurePieceConnection
import chylex.hee.game.world.generation.structure.piece.IStructurePieceConnection.AlignmentType.ODD
import net.minecraft.util.Direction
import net.minecraft.util.math.BlockPos

class TombDungeonConnection(override val type: TombDungeonConnectionType, override val offset: BlockPos, override val facing: Direction) : IStructurePieceConnection {
	override val alignment = ODD
}
