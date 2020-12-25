package chylex.hee.game.world.structure.piece

import net.minecraft.util.Direction
import net.minecraft.util.math.BlockPos

interface IStructurePieceConnection {
	val type: IStructurePieceConnectionType
	val offset: BlockPos
	val facing: Direction
	val alignment: AlignmentType
	
	enum class AlignmentType {
		ODD, EVEN, EVEN_MIRRORED;
		
		val mirrored
			get() = when(this) {
				ODD           -> ODD
				EVEN          -> EVEN_MIRRORED
				EVEN_MIRRORED -> EVEN
			}
	}
}
