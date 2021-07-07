package chylex.hee.game.world.generation.structure

import chylex.hee.game.world.generation.structure.palette.Palette
import chylex.hee.game.world.generation.structure.piece.IStructureBuilder
import chylex.hee.game.world.generation.structure.piece.StructurePiece
import chylex.hee.util.math.PosXZ
import chylex.hee.util.math.Size
import net.minecraft.util.math.BlockPos
import net.minecraft.world.server.ServerWorld

@Suppress("PropertyName")
interface IStructureDescription {
	val STRUCTURE_SIZE: Size
	
	val STRUCTURE_BUILDER: IStructureBuilder<*>
	val STRUCTURE_LOCATOR: (ServerWorld, PosXZ) -> BlockPos?
	
	val PALETTE: Palette
	val ALL_PIECES: Array<out StructurePiece<*>>
	
	companion object {
		val NULL_LOCATOR: (ServerWorld, PosXZ) -> BlockPos? = { _, _ -> null }
	}
}
